package ghostd.boot.mapper;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.ContentVersionStrategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class StaticResourceResolverTest {
    private StaticResourceResolver resolver;

    private CacheManager cacheManager;

    @Before
    public void setUp() {
        ResourceProperties resourceProperties = new ResourceProperties();
        resourceProperties.setStaticLocations(new String[]{"classpath:/ghostd/boot/mapper/"});

        ResourceProperties.Content content = resourceProperties.getChain().getStrategy().getContent();
        content.setEnabled(true);
        content.setPaths(new String[]{"/**/libs*"});

        ResourceProperties.Fixed fixed = resourceProperties.getChain().getStrategy().getFixed();
        fixed.setEnabled(true);
        fixed.setPaths(new String[]{"/**/app*"});
        fixed.setVersion("v1.2.3");

        cacheManager = new ConcurrentMapCacheManager();
        ResourceLoader resourceLoader = new StaticApplicationContext();

        resolver = new StaticResourceResolver(resourceProperties, cacheManager);
        resolver.setResourceLoader(resourceLoader);
        resolver.afterPropertiesSet();
    }

    @Test
    public void resolve_shouldReturnNull_whenResourceDoesNotExist() {
        assertNull(resolver.resolve("/does/not/exist.js"));
    }

    @Test
    public void resolve_shouldReturnResource_whenResourceExists() {
        assertNotNull(resolver.resolve("app.js"));
    }

    @Test
    public void resolve_shouldCacheResource_whenResourceExists() {
        // We feed the cache
        Cache cache = cacheManager.getCache(resolver.getClass().getCanonicalName());
        cache.put(CachingResourceResolver.RESOLVED_RESOURCE_CACHE_KEY_PREFIX + "app.js",
                new DescriptiveResource("my cached resource"));

        System.out.println();
        Resource resource = resolver.resolve("app.js");
        assertNotNull(resource);
        assertEquals("my cached resource", resource.getDescription());
    }

    @Test
    public void resolve_shouldReturnResource_whenResourceExistsWithFixedVersion() {
        assertNotNull(resolver.resolve("v1.2.3/app.js"));
    }

    @Test
    public void resolve_shouldReturnResource_whenResourceExistsWithContentVersion() {
        ContentVersionStrategy strategy = new ContentVersionStrategy();
        String version = strategy.getResourceVersion(new ClassPathResource("ghostd/boot/mapper/libs.js"));

        assertNotNull(resolver.resolve("libs-" + version + ".js"));
    }
}
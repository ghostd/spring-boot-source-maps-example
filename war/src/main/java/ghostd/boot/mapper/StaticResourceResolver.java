package ghostd.boot.mapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.ExposeDefaultResourceResolverChain;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses the {@link org.springframework.boot.autoconfigure.web.ResourceProperties ResourceProperties}
 * to find the embed resources.
 */
@Component
public class StaticResourceResolver implements ResourceLoaderAware, InitializingBean {
    private final ResourceProperties resourceProperties;
    private final CacheManager cacheManager;

    private final List<ResourceResolver> resolvers = new ArrayList<>();
    private final List<Resource> locations = new ArrayList<>();

    private ResourceLoader resourceLoader;

    @Autowired
    public StaticResourceResolver(ResourceProperties resourceProperties, CacheManager cacheManager) {
        this.resourceProperties = resourceProperties;
        this.cacheManager = cacheManager;
    }

    public Resource resolve(String path) {
        return new ExposeDefaultResourceResolverChain(resolvers).resolveResource(null, path, locations);
    }

    @Override
    public void afterPropertiesSet() {
        resolvers.add(new CachingResourceResolver(cacheManager, this.getClass().getCanonicalName()));

        resolvers.add(getVersionResourceResolver(resourceProperties.getChain().getStrategy()));

        for (String location : resourceProperties.getStaticLocations()) {
            locations.add(resourceLoader.getResource(location));
        }
        PathResourceResolver pathResourceResolver = new PathResourceResolver();
        pathResourceResolver.setAllowedLocations(locations.toArray(new Resource[locations.size()]));
        resolvers.add(pathResourceResolver);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private ResourceResolver getVersionResourceResolver(ResourceProperties.Strategy properties) {
        VersionResourceResolver resolver = new VersionResourceResolver();
        if (properties.getFixed().isEnabled()) {
            String version = properties.getFixed().getVersion();
            String[] paths = properties.getFixed().getPaths();
            resolver.addFixedVersionStrategy(version, paths);
        }
        if (properties.getContent().isEnabled()) {
            String[] paths = properties.getContent().getPaths();
            resolver.addContentVersionStrategy(paths);
        }
        return resolver;
    }
}

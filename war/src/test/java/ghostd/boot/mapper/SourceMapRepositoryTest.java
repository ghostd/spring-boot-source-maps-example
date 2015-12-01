package ghostd.boot.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class SourceMapRepositoryTest {
    private SourceMapRepository sourceMapRepository;

    @Mock
    private StaticResourceResolver resourceResolver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sourceMapRepository = new SourceMapRepository(resourceResolver);
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenUriIsInvalid() {
        assertNull(sourceMapRepository.getSourceMap("invalid>/uri/path"));
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenUriIsNotHandled() {
        // The current implementation only accepts 'localhost'... so check with something else
        assertNull(sourceMapRepository.getSourceMap("http://www.example.com/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenSourceMapLocationIsRemote() {
        when(resourceResolver.resolve("path/script.js"))
                .thenReturn(createResourceWithSourceMap("http://remote.example.com/path/source.map"));

        assertNull(sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnDecodedSourceMap_whenSourceMapIsBase64Inlined() {
        final String inline = "data:text/plain;base64,bXkgc291cmNlIG1hcA==";
        when(resourceResolver.resolve("path/script.js"))
                .thenReturn(createResourceWithSourceMap(inline));

        assertEquals("my source map", sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenSourceMapIsInlinedButNotBase64() {
        final String inline = "data:text/plain;other-encoding,other data";
        when(resourceResolver.resolve("path/script.js"))
                .thenReturn(createResourceWithSourceMap(inline));

        assertNull(sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenGeneratedSourceDoesNotExist() {
        when(resourceResolver.resolve("path/script.js"))
                .thenReturn(new ClassPathResource("does/not/exist.js"));

        assertNull(sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnNull_whenExternalSourceMapDoesNotExist() {
        final String external = "script.js.map";
        when(resourceResolver.resolve("script.js"))
                .thenReturn(createResourceWithSourceMap(external));
        when(resourceResolver.resolve("script.js.map"))
                .thenReturn(new ClassPathResource("does/not/exist.js.map"));

        assertNull(sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnSourceMap_whenExternalSourceMapInADifferentDirectory() {
        final String external = "../maps/script.js.map";
        when(resourceResolver.resolve("path/script.js"))
                .thenReturn(createResourceWithSourceMap(external));
        when(resourceResolver.resolve("maps/script.js.map"))
                .thenReturn(createResource("my source map"));

        assertEquals("my source map", sourceMapRepository.getSourceMap("http://localhost/path/script.js"));
    }

    @Test
    public void getSourceMap_shouldReturnSourceMap_whenExternalSourceMapInTheSameDirectory() {
        final String external = "script.js.map";
        when(resourceResolver.resolve("script.js"))
                .thenReturn(createResourceWithSourceMap(external));
        when(resourceResolver.resolve("script.js.map"))
                .thenReturn(createResource("my source map"));

        assertEquals("my source map", sourceMapRepository.getSourceMap("http://localhost/script.js"));
    }

    private Resource createResourceWithSourceMap(String sourceMapLocation) {
        return createResource("function js(){}//# sourceMappingURL=" + sourceMapLocation + "\n");
    }

    private Resource createResource(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }
}

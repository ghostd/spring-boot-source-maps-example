package ghostd.boot.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class SourceMapRepository {
    private static final String SOURCE_MAPPING_URL_PREFIX = "//# sourceMappingURL=";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StaticResourceResolver staticResourceResolver;

    @Autowired
    public SourceMapRepository(StaticResourceResolver staticResourceResolver) {
        this.staticResourceResolver = staticResourceResolver;
    }

    @Cacheable("source-map")
    public String getSourceMap(String generatedSourceUri) {
        try {
            URI uri = new URI(generatedSourceUri);
            if (handleGeneratedSource(uri)) {
                return retrieveSourceMap(uri);
            }
        } catch (URISyntaxException e) {
            logger.debug("Error while getting a source map", e);
        }

        return null;
    }

    private boolean handleGeneratedSource(URI uri) {
        // FIXME this should be in properties
        return "localhost".equalsIgnoreCase(uri.getHost());
    }

    private String retrieveSourceMap(URI generatedSourceUri) {
        String sourceMapLocation = extractSourceMapLocation(generatedSourceUri);

        if (!StringUtils.hasText(sourceMapLocation)) {
            return null;
        }

        if (sourceMapLocation.startsWith("http:") || sourceMapLocation.startsWith("https:")) {
            // We ignore the remote source maps
            return null;
        }

        String sourceMap = null;
        if (sourceMapLocation.startsWith("data:")) {
            /*
             * We have an inline source map:
             * data:application/json;base64,eyJ2Z...
             */
            return decodeInlineSourceMap(sourceMapLocation);
        }

        // We consider the location is a local file path (without scheme)
        try {
            Path sourcePath = Paths.get(extractRelativePath(generatedSourceUri));
            Path sourceMapPath = sourcePath.resolveSibling(sourceMapLocation).normalize();
            sourceMap = resourceToString(staticResourceResolver.resolve(sourceMapPath.toString()));
        } catch (FileSystemNotFoundException fileSystemNotFoundException) {
            logger.debug("We cannot find the source map", fileSystemNotFoundException);
        }

        return sourceMap;
    }

    private String decodeInlineSourceMap(String inlineSourceMap) {
        String sourceMap = null;

        int dataIndex = inlineSourceMap.indexOf(',');
        int encodingIndex = inlineSourceMap.indexOf(';');
        if (encodingIndex != -1 && encodingIndex < dataIndex
                && "base64".equals(inlineSourceMap.substring(encodingIndex + 1, dataIndex))) {
            String inline = inlineSourceMap.substring(dataIndex + 1);
            sourceMap = new String(Base64.getMimeDecoder().decode(inline), StandardCharsets.UTF_8);
        }

        return sourceMap;
    }

    private String extractSourceMapLocation(URI sourceUri) {
        String sourceMapLocation = null;

        String sourcePath = extractRelativePath(sourceUri);

        Resource source = staticResourceResolver.resolve(sourcePath);

        String content = resourceToString(source);
        if (content != null) {
            int index = content.lastIndexOf(SOURCE_MAPPING_URL_PREFIX);
            if (index != -1) {
                sourceMapLocation = content.substring(index + SOURCE_MAPPING_URL_PREFIX.length()).trim();
            }
        }

        return sourceMapLocation;
    }

    private String resourceToString(Resource resource) {
        String content = null;
        if (resource != null) {
            try {
                content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("Error while reading the resource", e);
            }
        }
        return content;
    }

    private String extractRelativePath(URI uri) {
        return uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
    }
}

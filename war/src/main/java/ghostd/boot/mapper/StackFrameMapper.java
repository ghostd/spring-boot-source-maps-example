package ghostd.boot.mapper;

import com.google.debugging.sourcemap.SourceMapConsumerFactory;
import com.google.debugging.sourcemap.SourceMapParseException;
import com.google.debugging.sourcemap.SourceMapping;
import com.google.debugging.sourcemap.proto.Mapping;
import ghostd.boot.exception.StackFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * This class tries to map the stack frames against source maps.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE - 100)
public class StackFrameMapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SourceMapRepository sourceMapRepository;

    @Autowired
    public StackFrameMapper(SourceMapRepository sourceMapRepository) {
        this.sourceMapRepository = sourceMapRepository;
    }

    public StackFrame map(StackFrame stackFrame) {
        try {
            String sourceMap = sourceMapRepository.getSourceMap(stackFrame.getFileName());

            if (StringUtils.hasText(sourceMap)) {
                SourceMapping sourceMapping = SourceMapConsumerFactory.parse(sourceMap);
                Mapping.OriginalMapping originalMapping =
                        sourceMapping.getMappingForLine(stackFrame.getLineNumber(), stackFrame.getColumnNumber());

                StackFrame originalFrame = new StackFrame();
                originalFrame.setFunctionName(stackFrame.getFunctionName());
                originalFrame.setSource(stackFrame.getSource());
                originalFrame.setFileName(originalMapping.getOriginalFile());
                originalFrame.setColumnNumber(originalMapping.getColumnPosition());
                originalFrame.setLineNumber(originalMapping.getLineNumber());
                return originalFrame;
            }
        } catch (SourceMapParseException e) {
            // We could ignore it, but we log it for debugging purpose
            logger.debug("Error while handling a stack frame", e);
        }

        return stackFrame;
    }
}

package ghostd.boot.controller;

import ghostd.boot.exception.ClientException;
import ghostd.boot.mapper.StackFrameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.joining;

/**
 * Receives the JS exception from the client to log them.
 */
@Controller
public class CrashReporterController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StackFrameMapper stackFrameMapper;

    @Autowired
    public CrashReporterController(StackFrameMapper stackFrameMapper) {
        this.stackFrameMapper = stackFrameMapper;
    }

    @RequestMapping(path = "/api/crash-reporter", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public String logException(@RequestBody ClientException clientException, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            userAgent = "unknown";
        }

        String msg = "Client JS exception {}: {}\n";
        msg += " with user agent: {}\n";
        msg += " with data: {}\n";

        if (!clientException.getParsedStack().isEmpty()) {
            String mappedStackFrames = clientException.getParsedStack()
                    .stream()
                    .map(stackFrameMapper::map)
                    .map(Object::toString)
                    .collect(joining("\n  "));

            msg += " with stack:\n  " + mappedStackFrames;
        } else if (!clientException.getRawStack().isEmpty()) {
            msg += " with raw stack:\n" + clientException.getRawStack();
        } else {
            msg += " without stack";
        }

        // In a production environment, we should sanitize these data before logging them
        logger.error(msg, clientException.getName(), clientException.getMessage(), userAgent, clientException.getAdditionalData());

        return "";
    }
}

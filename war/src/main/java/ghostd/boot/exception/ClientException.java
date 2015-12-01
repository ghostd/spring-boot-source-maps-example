package ghostd.boot.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientException {
    private String name;
    private String message;
    private String rawStack;
    private List<StackFrame> parsedStack;
    private Map<String, String> additionalData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawStack() {
        return rawStack;
    }

    public void setRawStack(String rawStack) {
        this.rawStack = rawStack;
    }

    public List<StackFrame> getParsedStack() {
        if (parsedStack == null) {
            parsedStack = new ArrayList<>();
        }
        return parsedStack;
    }

    public void setParsedStack(List<StackFrame> parsedStack) {
        this.parsedStack = parsedStack;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }
}

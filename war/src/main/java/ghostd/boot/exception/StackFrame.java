package ghostd.boot.exception;

import org.springframework.util.StringUtils;

/**
 * This class represents a Java version of stackframe.js
 */
public class StackFrame {
    private String functionName;
    private String fileName;
    private Integer lineNumber;
    private Integer columnNumber;
    private String source;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (StringUtils.hasText(this.functionName)) {
            stringBuilder.append(this.functionName);
        } else {
            stringBuilder.append("{anonymous}");
        }

        if (StringUtils.hasText(this.fileName)) {
            stringBuilder.append('@');
            stringBuilder.append(this.fileName);
        }

        if (this.lineNumber != null) {
            stringBuilder.append(':');
            stringBuilder.append(this.lineNumber);
        }

        if (this.columnNumber != null) {
            stringBuilder.append(':');
            stringBuilder.append(this.columnNumber);
        }

        return stringBuilder.toString();
    }
}

package org.artyomcool.ravenproxy.data;

import java.util.List;

public class SentryException {

    private String type;
    private String value;
    private String module;

    private StackTrace stacktrace;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public List<SentryStackFrame> getStackTrace() {
        if (stacktrace == null) {
            return null;
        }
        return stacktrace.frames;
    }

    public void setStackTrace(List<SentryStackFrame> stacktrace) {
        if (this.stacktrace == null) {
            this.stacktrace = new StackTrace();
        }
        this.stacktrace.frames = stacktrace;
    }

    private static class StackTrace {
        private List<SentryStackFrame> frames;
    }
}

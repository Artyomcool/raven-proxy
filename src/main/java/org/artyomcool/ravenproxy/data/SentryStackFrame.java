package org.artyomcool.ravenproxy.data;

public class SentryStackFrame {

    private String module;
    private String function;
    private Integer lineno;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Integer getLineNumber() {
        return lineno;
    }

    public void setLineNumber(Integer lineno) {
        this.lineno = lineno;
    }
}

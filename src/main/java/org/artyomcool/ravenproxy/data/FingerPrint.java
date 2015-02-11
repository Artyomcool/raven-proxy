package org.artyomcool.ravenproxy.data;

import java.io.File;

public class FingerPrint {

    private final String app;
    private final String version;

    public FingerPrint(String app, String version) {
        this.app = app;
        this.version = version;
    }

    public String getApp() {
        return app;
    }

    public String getVersion() {
        return version;
    }

    public String toFileName() {
        return "mappings" + File.separator + app + File.separator + version;
    }

    @Override
    public String toString() {
        return "FingerPrint{" +
                "app='" + app + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

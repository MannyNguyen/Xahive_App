package ca.xahive.app.bl.utils;

import java.io.File;

public class S3TaskParams {
    private File file;
    private String key;

    public S3TaskParams(File file, String key) {
        this.file = file;
        this.key = key;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

package ca.xahive.app.bl.utils;

import java.io.File;

public class S3TaskResult {
    private File file = null;
    private String errorMessage = null;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

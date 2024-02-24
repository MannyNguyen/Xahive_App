package ca.xahive.app.bl.local;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ca.xahive.app.ui.activities.TabBarManagerActivity;

public class LocalFileOpenWorker extends AsyncTask<Void,Void,Void> {
    private File file;
    private LocalFileOpenSuccess onSuccess;
    private Runnable onFail;
    private boolean successful;

    public LocalFileOpenWorker(String filename, LocalFileOpenSuccess onSuccess, Runnable onFail) {
        this.file = new File(TabBarManagerActivity.getContext().getCacheDir(), filename);
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    public LocalFileOpenWorker(File file, LocalFileOpenSuccess onSuccess, Runnable onFail) {
        this.file = file;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            int size = (int)this.file.length();

            byte[] data = new byte[size];
            FileInputStream fis = new FileInputStream(this.file);
            int bytesRead = fis.read(data);

            if (size == bytesRead) {
                onSuccess.setData(data);
                successful = true;
            }
            else {
                successful = false;
            }

        }
        catch (IOException e) {
            e.printStackTrace();

            successful = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (successful) {
            this.onSuccess.run();
        }
        else {
            this.onFail.run();
        }
    }

    public static class LocalFileOpenSuccess implements Runnable {
        private byte[] data;

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {

        }
    }
}

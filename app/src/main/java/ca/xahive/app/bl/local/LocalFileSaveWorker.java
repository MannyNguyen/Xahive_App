package ca.xahive.app.bl.local;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.xahive.app.ui.activities.TabBarManagerActivity;

public class LocalFileSaveWorker extends AsyncTask<Void,Void,Void> {
    private File file;
    private byte[] data;
    private Runnable onSuccess;
    private Runnable onFail;
    private boolean successful;

    public LocalFileSaveWorker(String filename, byte[] data, Runnable onSuccess, Runnable onFail) {
        this.file = new File(TabBarManagerActivity.getContext().getCacheDir(), filename);
        this.data = data;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    public LocalFileSaveWorker(File file, byte[] data, Runnable onSuccess, Runnable onFail) {
        this.file = file;
        this.data = data;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(this.file));

            writer.write(this.data);
            writer.flush();
            writer.close();

            successful = true;
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
}

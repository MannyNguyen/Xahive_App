package ca.xahive.app.bl.local;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.services.s3.model.GetObjectRequest;

import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.S3TaskParams;
import ca.xahive.app.bl.utils.S3TaskResult;


public class AvatarLoader extends AsyncTask<S3TaskParams, Void, S3TaskResult> {
    @Override
    protected S3TaskResult doInBackground(S3TaskParams... taskParams) {
        S3TaskParams taskParam = taskParams[0];

        TransferManager transferManager = Model.getInstance().getS3Manager();
        GetObjectRequest getObjectRequest = new GetObjectRequest(Config.S3_FILE_BUCKET, taskParam.getKey());
        Download download = null;

        try {
            download = transferManager.download(getObjectRequest, taskParam.getFile());
            download.waitForCompletion();
        } catch (Exception e) {
            // nothing to do.
        }

        boolean transferOk = (download != null && download.getState() == Transfer.TransferState.Completed);

        S3TaskResult result = new S3TaskResult();
        result.setFile(taskParam.getFile());
        result.setErrorMessage(transferOk ? "" : null); // Error string not important.

        return result;
    }

}

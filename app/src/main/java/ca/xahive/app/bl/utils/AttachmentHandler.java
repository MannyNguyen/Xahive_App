package ca.xahive.app.bl.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.security.acl.Acl;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AttachmentHandler {

    public static void showExceedsFileSizeDialog(Context context, boolean isBuzz) {

        double fileSize = isBuzz ? Helpers.maxBuzzFileSize() : Helpers.maxPersonalMessageFileSize();
        String fileSizeStr = Helpers.humanReadableFileSize(fileSize);

        String msg = String.format(context.getString(R.string.exceeds_filesize_limit), fileSizeStr);

        SimpleAlertDialog.showMessageWithOkButton(
                context,
                context.getString(R.string.error),
                msg,
                null
        );
    }

    public static void showUploadFailedDialog(Context context) {
        SimpleAlertDialog.showMessageWithOkButton(
                context,
                context.getString(R.string.error),
                context.getString(R.string.attach_upload_failed),
                null
        );
    }

    public static void showDownloadFailedDialog(Context context, String message) {
        SimpleAlertDialog.showMessageWithOkButton(
                context,
                context.getString(R.string.error),
                message,
                null
        );
    }


    public static void showFileLocalWithPath(Context context, String filename) {

        String msg = String.format(context.getString(R.string.download_complete_msg), filename);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        File file =  Helpers.fileFromPath( filename);

        //determine the mime type - we can expand the helpers later
        intent.setDataAndType(Uri.fromFile(file), Helpers.getDownloadedFileMime(filename));
        context.startActivity(intent);
    }

    public static void showDownloadCompleted(Context context, String filename) {

        String msg = String.format(context.getString(R.string.download_complete_msg), filename);

        //2014-07-11 Davoodinator
        //This is now been changed to trigger opening the file when it finishes downloading.
        //context has the file name
        //we just need to figure out the directory name and we are set
        //old and busted:
        //SimpleAlertDialog.showMessageWithOkButton(context,"",msg, null);
        //new and not busted yet:
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        //where is the file located.   this should probably be a helper. function.
        //File file = new File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())+"/"+filename);
        File file =  Helpers.getDownloadedFileNamed(context, filename);

        //determine the mime type - we can expand the helpers later
        intent.setDataAndType(Uri.fromFile(file), Helpers.getDownloadedFileMime(filename));
        context.startActivity(intent);
    }

    public void uploadAttachment(Activity activity, File file, String key, Runnable onSuccess, Runnable onFailure) {
        new S3PutObjectTask(activity, onSuccess, onFailure).execute(new S3TaskParams(file, key));
        //TODO: make it so that if we uploaded something, link to where THAT resource is so that we don't have to download "back" our own file later in order to quickly open it
    }


    public void downloadAttachment(final Activity activity, final Message message, final Runnable onSuccess) {

        Runnable onDownload = new Runnable() {
            @Override
            public void run() {
                if (Helpers.isExternalStorageWritable()) {

                    String key = String.format(Config.ATTACHMENT_FILE_NAME, message.getAttachmentId());
                    File file = Helpers.getDownloadedFileNamed(activity,message.getFilename());

                    new S3GetObjectTask(activity, onSuccess).execute(new S3TaskParams(file, key));
                }
                else {
                    AttachmentHandler.showDownloadFailedDialog(activity, activity.getString(R.string.external_storage_not_writable));
                }
            }
        };

        SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(
                activity,
                activity.getString(R.string.download_attachment_prompt),
                (message.getFilename() + " (" + Helpers.humanReadableFileSize(message.getFilesize()) + ")"),
                activity.getString(R.string.no),
                activity.getString(R.string.yes),
                null,
                onDownload
        );

    }

    private class S3GetObjectTask extends AsyncTask<S3TaskParams, Integer, S3TaskResult> {

        Activity activity;
        ProgressDialog dialog;
        Runnable onSuccess;

        public S3GetObjectTask(Activity activity, Runnable onSuccess) {
            super();
            this.activity = activity;
            this.onSuccess = onSuccess;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(activity.getString(R.string.downloading));
            dialog.setMax(100);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }


        @Override
        protected S3TaskResult doInBackground(S3TaskParams... params) {

            S3TaskResult result = new S3TaskResult();

            result.setFile(params[0].getFile());

            try {

                TransferManager tm = Model.getInstance().getS3Manager();
                GetObjectRequest gor = new GetObjectRequest(Config.S3_FILE_BUCKET, params[0].getKey());

                final Download myDownload = tm.download(gor, params[0].getFile());


                while(!myDownload.isDone()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setProgress((int) myDownload.getProgress().getPercentTransferred());
                        }
                    });
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                result.setErrorMessage(e.getMessage());
                return result;
            }

        }


        @Override
        protected void onPostExecute(S3TaskResult result) {
            dialog.dismiss();
            if (result.getErrorMessage() != null) {
                AttachmentHandler.showDownloadFailedDialog(
                        activity,
                        activity.getString(R.string.attach_download_failed)
                );
            } else {
                onSuccess.run();
            }
        }
    }

    private class S3PutObjectTask extends AsyncTask<S3TaskParams, Integer, S3TaskResult> {

        Activity activity;
        Runnable onSuccess;
        Runnable onFailure;
        ProgressDialog dialog;

        public S3PutObjectTask(Activity activity, Runnable onSuccess, Runnable onFailure) {
            super();
            this.activity = activity;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(activity.getString(R.string.uploading));
            dialog.setMax(100);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }


        @Override
        protected S3TaskResult doInBackground(S3TaskParams... params) {

            S3TaskResult result = new S3TaskResult();

            result.setFile(params[0].getFile());

            try {
                TransferManager tm = Model.getInstance().getS3Manager();
                ObjectMetadata metadata = new ObjectMetadata();
                FileInputStream in = new FileInputStream( params[0].getFile());
                metadata.setContentLength((params[0].getFile()).length());


                AccessControlList acl = new AccessControlList();
               // acl.grantPermission(new CanonicalGrantee("d25639fbe9c19cd30a4c0f43fbf00e2d3f96400a9aa8dabfbbebe1906Example"), Permission.ReadAcp);
                acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
                acl.grantPermission(GroupGrantee.AllUsers, Permission.ReadAcp);

                //acl.grantPermission(new EmailAddressGrantee("user@email.com"), Permission.WriteAcp);

                PutObjectRequest por = new PutObjectRequest("xahive-test", params[0].getKey(), in, metadata).withAccessControlList(acl);

                // PutObjectRequest por = new PutObjectRequest(Config.S3_FILE_BUCKET, params[0].getKey(), in, metadata).withAccessControlList(acl);
                final Upload myUpload = tm.upload(por);

                myUpload.addProgressListener(new ProgressListener() {
                    @Override
                    public void progressChanged(ProgressEvent progressEvent) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setProgress((int) myUpload.getProgress().getPercentTransferred());
                            }
                        });
                    }
                });

                myUpload.waitForUploadResult(); // void call to sync.

                if (myUpload.getState() != Transfer.TransferState.Completed) {
                    result.setErrorMessage(activity.getString(R.string.attach_upload_failed));
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                result.setErrorMessage(e.getMessage());
                return result;
            }

        }


        @Override
        protected void onPostExecute(S3TaskResult result) {
             Log.v("nameee", result.getFile().getName());
             dialog.dismiss();
             if (result.getErrorMessage() != null) {
                onFailure.run();
            } else {
                onSuccess.run();
            }
        }
    }

}

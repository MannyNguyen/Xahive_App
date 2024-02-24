package ca.xahive.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.utils.Helpers;

public class SimpleAlertDialog {

    public static void showMessageWithCancelAndAcceptButtons(Context context, String title, String message,
                                                             String cancelText, String acceptText,
                                                             final Runnable onCancel, final Runnable onAccept) {
        new AlertDialog.Builder(context)
                .setTitle((title != null)?title:"")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(acceptText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if(onAccept != null) {
                            onAccept.run();
                        }
                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if(onCancel != null) {
                            onCancel.run();
                        }
                    }
                })
                .show();
    }

    public static void showMessageWithEditText(Context context, String title, String message,
                                               String cancelText, String acceptText,
                                               final EditText editText,
                                               final Runnable onCancel, final Runnable onAccept) {
        new AlertDialog.Builder(context)
                .setTitle((title != null) ? title : "")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(acceptText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Helpers.hideSoftKeyboardForEditText(editText);
                        dialog.dismiss();
                        if (onAccept != null) {
                            onAccept.run();
                        }
                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Helpers.hideSoftKeyboardForEditText(editText);
                        dialog.cancel();
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    }
                })
                .setView(editText)
                .show();
    }


    public static void showMessageWithOkButton(Context context, String title, String message, final Runnable callback) {
        new AlertDialog.Builder(context)
                .setTitle((title != null)?title:context.getString(R.string.message))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (callback != null) {
                            callback.run();
                        }
                    }
                }).show();
    }

    public static void showErrorWithOkButton(Context context, ModelError error, final Runnable callback) {
        error.setHandled(true);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.error))
                .setMessage(error.getReadableCode())
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (callback != null) {
                            callback.run();
                        }
                    }
                }).show();
    }

    public static ProgressDialog createBusyIndicator(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage( (message != null) ? message : context.getString(R.string.please_wait) );
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}

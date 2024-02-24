package ca.xahive.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.utils.Helpers;

public class FileDecryptionDialog extends EncryptionDialog {

    public String getDecryptionPassword() {
        return Helpers.extractStringFromTextView(getEditText());
    }

    public EditText getEditText() {
        return (EditText)(this.getDialog().findViewById(R.id.fileDecryptionPasswordField));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.xa_file_decryption_dialog, null))
                .setPositiveButton(getString(R.string.decrypt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        FileDecryptionDialog.this.getDialog().dismiss();
                        try {
                            encryptionDialogListener.onDialogPositiveClick(FileDecryptionDialog.this);
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FileDecryptionDialog.this.getDialog().cancel();
                        encryptionDialogListener.onDialogNegativeClick(FileDecryptionDialog.this);

                    }
                });
        return builder.create();
    }

}

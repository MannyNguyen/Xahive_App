package ca.xahive.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.utils.Helpers;

public class PerformDecryptionDialog extends EncryptionDialog {


    private CheckBox getSaveCheckbox() {
        return (CheckBox)(this.getDialog().findViewById(R.id.saveCheckbox));
    }

    private CheckBox getApplyAllCheckbox() {
        return (CheckBox)(this.getDialog().findViewById(R.id.applyAllCheckbox));
    }

    public String getDecryptionPassword() {
        return Helpers.extractStringFromTextView(getEditText());
    }

    public EditText getEditText() {
        return (EditText)(this.getDialog().findViewById(R.id.decryptionPasswordField));
    }

    public boolean shouldSaveContent() {
        return getSaveCheckbox().isChecked();
    }

    public boolean shouldApplyAll() {
        return getApplyAllCheckbox().isChecked();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View content = inflater.inflate(R.layout.xa_decryption_dialog, null);
        ((CheckBox) content.findViewById(R.id.saveCheckbox)).setChecked(UserDefaults.getDecryptSave());
        ((CheckBox) content.findViewById(R.id.applyAllCheckbox)).setChecked(UserDefaults.getApplyPasswordToAll());

        builder.setView(content)
                .setPositiveButton(getString(R.string.decrypt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            encryptionDialogListener.onDialogPositiveClick(PerformDecryptionDialog.this);
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
                        PerformDecryptionDialog.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        encryptionDialogListener.onDialogNegativeClick(PerformDecryptionDialog.this);
                        PerformDecryptionDialog.this.getDialog().cancel();
                    }
                });



        return builder.create();
    }
}

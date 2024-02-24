package ca.xahive.app.ui.dialogs;


import android.app.DialogFragment;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EncryptionDialog extends DialogFragment {

    public interface EncryptionDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException;
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    EncryptionDialogListener encryptionDialogListener;

    public void setEncryptionDialogListener(EncryptionDialogListener listener) {
        this.encryptionDialogListener = listener;
    }

}

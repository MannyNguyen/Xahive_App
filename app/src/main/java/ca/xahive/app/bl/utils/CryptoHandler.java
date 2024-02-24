package ca.xahive.app.bl.utils;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Attachment;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.PasswordCacheContext;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.Buzz;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.ui.dialogs.EncryptionDialog;
import ca.xahive.app.ui.dialogs.FileDecryptionDialog;
import ca.xahive.app.ui.dialogs.PerformDecryptionDialog;
import ca.xahive.app.ui.dialogs.PerformEncryptionDialog;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;


public class CryptoHandler implements EncryptionDialog.EncryptionDialogListener {
    private static final String ENCRYPTION_DIALOG_TAG = "encryption_dialog";
    private static final String DECRYPTION_DIALOG_TAG = "decryption_dialog";
    private static final String FILE_DECRYPTION_DIALOG_TAG = "file_decryption_dialog";

    private Message messageToDecrypt;

    public interface EncryptionDelegate {
        public abstract void encryptionPasswordSet(String encryptionPassword);
        public abstract void attachmentFileUpdated(File attachmentFile);
    }


    public interface DecryptionDelegate {
        public abstract void messageDecryptionComplete(boolean success);
    }


    private EncryptionDelegate encryptionDelegate;
    private DecryptionDelegate decryptionDelegate;
    private Activity activity;
    private Attachment attachmentToDownload;


    private PerformDecryptionDialog decryptionDialog;
    private PerformEncryptionDialog encryptionDialog;
    private FileDecryptionDialog fileDecryptionDialog;

    private PerformDecryptionDialog getDecryptionDialog() {
        if(decryptionDialog == null) {
            decryptionDialog = new PerformDecryptionDialog();
            decryptionDialog.setEncryptionDialogListener(this);
        }

        return decryptionDialog;
    }

    private PerformEncryptionDialog getEncryptionDialog() {
        if(encryptionDialog == null) {
            encryptionDialog = new PerformEncryptionDialog();
            encryptionDialog.setEncryptionDialogListener(this);
        }
        return encryptionDialog;
    }


    private FileDecryptionDialog getFileDecryptionDialog() {
        if(fileDecryptionDialog == null) {
            fileDecryptionDialog = new FileDecryptionDialog();
            fileDecryptionDialog.setEncryptionDialogListener(this);
        }
        return fileDecryptionDialog;
    }


    public static void showEncryptionErrorDialog(Context context) {

        SimpleAlertDialog.showMessageWithOkButton(context,context.getString(R.string.error),context.getString(R.string.encrypt_msg_error), null);
    }

    public static void showDecryptionErrorDialog(Context context) {

        SimpleAlertDialog.showMessageWithOkButton(context,context.getString(R.string.error),context.getString(R.string.decrypt_msg_error), null);
    }


    public CryptoHandler(EncryptionDelegate encryptionDelegate, DecryptionDelegate decryptionDelegate, Activity activity) {

        this.encryptionDelegate = encryptionDelegate;
        this.decryptionDelegate = decryptionDelegate;
        this.activity = activity;

        if((this.encryptionDelegate == null && this.decryptionDelegate == null) || this.activity == null){
            throw new AssertionError("CryptoHandler -> Must set at least one delegate and an Activity");
        }

    }

    public void promptForEncryptionPassword(String currentEncryptionPassword) {

        Runnable onRemove = new Runnable() {
            @Override
            public void run() {
                encryptionDelegate.encryptionPasswordSet(null);
            }
        };

        if(Helpers.stringIsNotNullAndMeetsMinLength(currentEncryptionPassword, 1)){
            SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(activity,
                    activity.getString(R.string.remove_encryption),
                    null,
                    activity.getString(R.string.cancel),
                    activity.getString(R.string.remove),
                    null,
                    onRemove);
        } else {
            getEncryptionDialog().show(activity.getFragmentManager(), ENCRYPTION_DIALOG_TAG);
        }

    }

    public void decryptMessage(Message messageToDecrypt) {
        this.messageToDecrypt = messageToDecrypt;
        String secret ="";
        if(messageToDecrypt.fromUserId== Model.getInstance().getCurrentUser().getUserId())
        {
           /**
            try {
               // secret = Crypto.RSADecrypt(UserDefaults.getPublicKey(),messageToDecrypt.refKey) ;

            }  catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }**/
        }
        String decryptedString="";
       /**
        try {
            //  decryptedString = .de(UserDefaults.getPrivateKey(), secret );
            decryptionDelegate.messageDecryptionComplete(true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }**/
        this.messageToDecrypt.content = decryptedString;
        //messageToDecrypt.content = "sdsds";

        //Log.v("decryptedString", decryptedString);

        decryptionDelegate.messageDecryptionComplete(true);
        getDecryptionDialog().show(activity.getFragmentManager(), DECRYPTION_DIALOG_TAG);
    }

    private void  decryptAttachment(String password, Context mContext) {

        if(Helpers.stringIsNotNullAndMeetsMinLength(password, 1)) {

            byte[] encryptedFile = Helpers.fileFromBase64ToByteArray(Helpers.getDownloadedFileNamed(mContext, attachmentToDownload.getFilename()));

            //Helpers.clearFile(attachmentToDownload.getAttachmentKey());
            Log.v("fileName ", attachmentToDownload.getFilename());
            Log.v("password ", password);
            Log.v("fileSize ",String.valueOf(encryptedFile.length) );

            byte[] decryptedData ;
            decryptedData = Crypto.decryptData(password, encryptedFile);

            if(decryptedData == null) {
               // getFileDecryptionDialog().show(activity.getFragmentManager(), FILE_DECRYPTION_DIALOG_TAG);
                return;
            }

            EncryptableFile encFile = new EncryptableFile();
            encFile.setPreparedData(decryptedData);
            byte[] originalData = encFile.getOriginalData();

            if (originalData == null) {
                //getFileDecryptionDialog().show(activity.getFragmentManager(), FILE_DECRYPTION_DIALOG_TAG);
                return;
            }
            //Helpers.clearFile(encFile.getFileName());

            File decryptedFile = Helpers.createFileWithDataAndFilenameFromBase64(originalData, encFile.getFileName(), mContext);

            //File decryptedFile = Helpers.createFileWithDataAndFilename(originalData, encFile.getFileName(),mContext);
            Log.v("decryptedFile",decryptedFile.getName());

            Log.v("decryptedFile",String.valueOf(decryptedFile.length()));

            if(decryptedFile == null) {
                SimpleAlertDialog.showMessageWithOkButton(
                        mContext,
                        mContext.getString(R.string.error),
                        mContext. getString(R.string.attach_decrypt_failed),null

                 );
               // getFileDecryptionDialog().show(activity.getFragmentManager(), FILE_DECRYPTION_DIALOG_TAG);
            } else {
                AttachmentHandler.showDownloadCompleted(activity, decryptedFile.getName());
               // Model.getInstance().getPasswordCache().setPasswordForIdentifierInContext(password, attachmentToDownload.getAttachmentId(), PasswordCacheContext.ATTACHMENT);
                //Helpers.getDownloadedFileNamed(mContext,attachmentToDownload.getFilename()).delete();
                this.attachmentToDownload = null;
            }


        } else {
            getFileDecryptionDialog().show(activity.getFragmentManager(), FILE_DECRYPTION_DIALOG_TAG);
        }
    }

    public void decryptAttachment(int attachmentId, String fileName, double fileSize, String password ,Context mContext) {

        this.attachmentToDownload = new Attachment();
        attachmentToDownload.setAttachmentId(attachmentId);
        attachmentToDownload.setFilename(fileName);
        attachmentToDownload.setFilesize(fileSize);
        //String password = Model.getInstance().getPasswordCache().getPasswordForIdentifierInContext(attachmentId, PasswordCacheContext.ATTACHMENT);
        decryptAttachment(password, mContext);

    }


    public boolean encryptAttachment(File attachmentFile, String encryptionPassword, Context mContext) {

        boolean success = false;
         if(Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1)) {
            EncryptableFile encFile = new EncryptableFile();
            encFile.setFileName(attachmentFile.getName());
            encFile.setOriginalData(Helpers.fileToByteArray(attachmentFile));
            byte[] preparedData = encFile.getPreparedData();

            if (preparedData != null) {

                byte[] outData = Crypto.encryptData(encryptionPassword, preparedData);
                if (outData != null) {
                     File encryptedFile = Helpers.createFileWithEncryptedData(outData, mContext);
                     if(encryptedFile != null) {
                        encryptionDelegate.attachmentFileUpdated(encryptedFile);
                        success = true;
                    }
                    else
                        success = false;
                } else { success = false; }
            } else { success = false; }
        }

        if(!success) {
            CryptoHandler.showEncryptionErrorDialog(activity);
            encryptionDelegate.attachmentFileUpdated(null);
        }

        return success;

    }
   public static String decryptionMessageWithPassword(String messageContent,String decryptionPassword) {
      if(messageContent!=null && decryptionPassword!=null) {
          String dencrpytedContent = null;

          byte[] dataFromB64 = Helpers.dataFromB64String(messageContent);

          byte[] outData = Crypto.decryptData(decryptionPassword, dataFromB64);


          EncryptableString encString = new EncryptableString();
          encString.setPreparedData(outData);
          dencrpytedContent = encString.getOriginalString();
          // byte[] preparedData = encString.setPreparedData();


          return dencrpytedContent;
      }
       else
          return  messageContent;
   }
    public String encryptMessageWithPassword(String messageContent, String encryptionPassword) {

        String encrpytedContent = null;

        EncryptableString encString = new EncryptableString();
        encString.setOriginalString(messageContent);
        byte[] preparedData = encString.getPreparedData();

        if(preparedData != null) {
            byte[] outData = Crypto.encryptData(encryptionPassword, preparedData);
            if(outData != null) {
                encrpytedContent = Helpers.b64EncodedString(outData);
            }
        }

        if(encrpytedContent == null) {
            CryptoHandler.showEncryptionErrorDialog(activity);
            encryptionDelegate.encryptionPasswordSet(null);
        }

        return encrpytedContent;

    }
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;
        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system   entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("RSA");
        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public void onDialogPositiveClick(DialogFragment dialog) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {


        if(dialog == this.decryptionDialog) {

            UserDefaults.setDecryptSave(decryptionDialog.shouldSaveContent());
            UserDefaults.setApplyPasswordToAll(decryptionDialog.shouldApplyAll());

            Log.v("getDecryptionPassword", decryptionDialog.getDecryptionPassword());

            Log.v("messageToDecrypt",String.valueOf(messageToDecrypt.getFromUserId() ));

            if(UserDefaults.getApplyPasswordToAll()) {
                PasswordCacheContext cacheContext = (messageToDecrypt instanceof PersonalMessage) ? PasswordCacheContext.PRIVATE : PasswordCacheContext.BUZZ;
                Model.getInstance().getPasswordCache().setPasswordForIdentifierInContext
                        (decryptionDialog.getDecryptionPassword(), this.messageToDecrypt.getFromUserId(), cacheContext);
            }
             String decryptedString="";
            // decryptedString = Crypto.RSADecrypt(UserDefaults.getPublicKey(), messageToDecrypt.content );
            //String decryptedString = this.messageToDecrypt.decryptedContentWithPassword(decryptionDialog.getDecryptionPassword());
            //String decryptedString = this.messageToDecrypt.decryptedContentWithPassword(messageToDecrypt.contentKey);

            /*8
            Log.v("messageToDecrypt",String.valueOf(decryptedString));
            //decryptedString = this.messageToDecrypt.decryptedContentWithPassword(messageToDecrypt.contentKey);

            try {
                decryptedString = this.messageToDecrypt.decryptedContentMessage(decryptionDialog.getDecryptionPassword());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }**/

            Log.v("messageToDecrypt1",String.valueOf(decryptedString));

            if(Helpers.stringIsNotNullAndMeetsMinLength(decryptedString, 1)) {

                Model.getInstance().getDecryptedMessageStore().setDecryptedTextForMessageId(decryptedString, messageToDecrypt.getMessageId());

                if(this.messageToDecrypt.getAttachmentId() > 0) {
                    Model.getInstance().getPasswordCache().setPasswordForIdentifierInContext(
                            decryptionDialog.getDecryptionPassword(),
                            messageToDecrypt.getAttachmentId(),
                            PasswordCacheContext.ATTACHMENT
                    );
                }

                decryptionDelegate.messageDecryptionComplete(true);

            } else {
                CryptoHandler.showDecryptionErrorDialog(activity);
                decryptionDelegate.messageDecryptionComplete(false);
            }

            if(messageToDecrypt instanceof Buzz) {
                Helpers.hideSoftKeyboardForEditText(decryptionDialog.getEditText());
            }

            this.messageToDecrypt = null;
            this.decryptionDialog = null;


        } else if (dialog == this.encryptionDialog) {
            UserDefaults.setDecryptSave(encryptionDialog.shouldSaveContent());

            if(Helpers.stringIsNotNullAndMeetsMinLength(encryptionDialog.getEncryptionPassword(), 1)) {
                encryptionDelegate.encryptionPasswordSet(encryptionDialog.getEncryptionPassword());
            } else {
                encryptionDelegate.encryptionPasswordSet(null);
            }

            encryptionDialog = null;

        } else if (dialog == this.fileDecryptionDialog) {

            Helpers.hideSoftKeyboardForEditText(fileDecryptionDialog.getEditText());
            String password = fileDecryptionDialog.getDecryptionPassword();
            fileDecryptionDialog = null;
            decryptAttachment(password,dialog.getContext());
        }

    }


    public void onDialogNegativeClick(DialogFragment dialog) {


        if(dialog == this.decryptionDialog) {
            if(messageToDecrypt instanceof Buzz) { Helpers.hideSoftKeyboardForEditText(decryptionDialog.getEditText()); }
            this.messageToDecrypt = null;
        } else if(dialog == this.encryptionDialog) {
            encryptionDelegate.encryptionPasswordSet(null);
            encryptionDialog = null;
        } else if (dialog == this.fileDecryptionDialog) {
            Helpers.hideSoftKeyboardForEditText(fileDecryptionDialog.getEditText());
          //  Helpers.getDownloadedFileNamed(attachmentToDownload.getFilename()).delete();
          //  attachmentToDownload = null;
          //  fileDecryptionDialog = null;
        }

    }

}





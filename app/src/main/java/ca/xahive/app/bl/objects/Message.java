package ca.xahive.app.bl.objects;

import android.util.Log;

import org.spongycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import IAPUtils.Base64DecoderException;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.PasswordCacheContext;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.EncryptableString;
import ca.xahive.app.bl.utils.Helpers;

public class Message extends BaseModelObject {
    public int messageId;
    public int fromUserId;
    //private Date date;
    public int toUserId;
    private Date date;


    public String content;
    public boolean isEncrypted;
    public int attachmentId;
    public String filename;
    public  String toDeviceId;

    public  String fromDeviceId;
    private double filesize;
    public String refKey;
    public String contentKey;
    public int  toHiveId;

    public void setToDeviceId(String deviceId)
    {
        toDeviceId  = deviceId;
    }
    public enum EncryptionState {
        XAHMessageStateNotEncrypted,
        XAHMessageStateEncrypted,
        XAHMessageStateDecrypted;
    }

    public EncryptionState getEncryptionState() {
        if(!this.isEncrypted) {
            return EncryptionState.XAHMessageStateNotEncrypted;
        } else if ( Model.getInstance().getDecryptedMessageStore().hasDecryptedTextForMessageId(messageId)) {
            return EncryptionState.XAHMessageStateDecrypted;
        } else {
            return EncryptionState.XAHMessageStateEncrypted;
        }
    }
    public String getAttachmentKey() {

        String key;

        if(this.attachmentId > 0) {
            key = String.format(Config.ATTACHMENT_FILE_NAME, this.attachmentId);
        } else {
            key = "";
        }

        return key;
    }
    public String getReadableContent() {
        String readableContent = null;

        switch (this.getEncryptionState()) {

            case XAHMessageStateDecrypted: {
                readableContent = Model.getInstance().getDecryptedMessageStore().decryptedTextForMessageId(messageId);
                break;
            }

            case XAHMessageStateNotEncrypted: {
                readableContent = getContent();
                //readableContent = Helpers.stringFromB64(getContent());
                break;
            }
            case XAHMessageStateEncrypted: {
                readableContent = "";
                break;
            }

            default: {
                break;
            }
        }

        return readableContent;
    }

    public void setReadableContent(String readableContent) {
        if (this.isEncrypted()) {
            if (true) throw new AssertionError("Should never set readableContent on an encrypted message.");
        }
        else {
            this.setContent(Helpers.b64EncodedString(readableContent.getBytes()));
        }
    }

    public String decryptedContentWithContext(PasswordCacheContext context) {
        String savedPassword = Model.getInstance().getPasswordCache().getPasswordForIdentifierInContext(this.fromUserId, context);
        return (savedPassword != null) ? decryptedContentWithPassword(savedPassword) : null;
    }

    public String decryptedContentWithPassword(String password) {
        byte[] mBytes = new byte[0];
        try {
            mBytes=  Crypto.decrypt((UserDefaults.getPrivateKeyAsString()).getBytes() ,this.contentKey.getBytes("utf-8")
                     );
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] encryptedContent = (Helpers.dataFromB64String(this.content));

        byte[] decryptedData = Crypto.decryptData(password, encryptedContent);

        if(decryptedData == null) {
            return null;
        }

        EncryptableString encString = new EncryptableString();
        encString.setPreparedData(decryptedData);
        String originalString = encString.getOriginalString();
        return originalString;
    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";

    private static final String ALGO_AES       = "AES";
    public static String myDecrypt(String encryptedData, String initialVectorString, String secretKey) {
        String decryptedData = null;
        try {
            String md5Key = md5(secretKey);
            SecretKeySpec skeySpec = new SecretKeySpec(md5Key.getBytes(), ALGO_AES);

            byte[] iv = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
            IvParameterSpec initialVector = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, initialVector);
            byte[] encryptedByteArray = new byte[0];
            try {
              encryptedByteArray = IAPUtils.Base64.decode(encryptedData);
              //  encryptedByteArray = android.util.Base64.decode(IAPUtils.Base64.decode(encryptedData), android.util.Base64.DEFAULT);
            } catch (Base64DecoderException e) {
                e.printStackTrace();
            }
            byte[] decryptedByteArray = cipher.doFinal(encryptedByteArray);
            decryptedData = new String(decryptedByteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return decryptedData;
    }


    private static final String ALGO_MD5       = "MD5";
    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGO_MD5);
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return String.format("%032x", number);
    }
    Cipher cipher;
    SecretKeySpec skeySpec;
    public byte[] decrypt (byte[] ciphertext) throws Exception{
        //returns byte array decrypted with key
        //cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        IvParameterSpec ivSpec = new IvParameterSpec(hexStringToByteArray("00000000000000000000000000000000"));

        cipher.init(Cipher.DECRYPT_MODE, skeySpec , ivSpec);

        byte[] plaintext = cipher.doFinal(ciphertext);

        return plaintext;
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public void Encryption(String passphrase) {
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = passphrase.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] thedigest = md.digest(bytesOfMessage);
        skeySpec = new SecretKeySpec(thedigest, "AES");


        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public double getFilesize() {
        return filesize;
    }

    public void setFilesize(double filesize) {
        this.filesize = filesize;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

}

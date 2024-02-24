package ca.xahive.app.bl.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * Created by trantung on 10/30/15.
 */
public class SimpleCrypto {

    private static byte[] ivTemp = "0000000000000000".getBytes();

    public static String encryptTest(String content, PublicKey key) throws Exception {
        // Encrypt where jo is input, and query is output and ENCRPYTION_KEy is key
        byte[] input = Base64.encode(content.getBytes(), Base64.DEFAULT);

        //MessageDigest md = MessageDigest.getInstance("MD5");
        //byte[] thedigest = md.digest(key.getBytes("UTF-8"));
        //SecretKeySpec skc = new SecretKeySpec(key.getBytes(), "RSA");
         Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        //byte[] iv = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};

        cipher.init(Cipher.ENCRYPT_MODE, key);

      //  byte[] cipherText = new byte[cipher.getOutputSize(255)];
        //int ctLength = cipher.update(input, 0, 256, cipherText, 0);
        byte[] cipherText = cipher.doFinal(input);
        return  new String (cipherText);
        //String query = Base64.encodeToString(cipherText, Base64.DEFAULT);
    }


    private  static  String iv = "fedcba9876543210";//Dummy iv (CHANGE IT!)
    private static IvParameterSpec ivspec;
    private static SecretKeySpec keyspec;
    private static Cipher cipher;

    private String SecretKey = "0123456789abcdef";//Dummy secretKey (CHANGE IT!)

    public static void doKey(String key)
    {
        ivspec = new IvParameterSpec(iv.getBytes());

        key = padRight(key,8);

        Log.d("hi",key);

        keyspec = new SecretKeySpec(key.getBytes(), "AES");

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String text,String key) throws Exception
    {
        if(text == null || text.length() == 0)
            throw new Exception("Empty string");

        doKey(key);

        byte[] encrypted = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

            encrypted = cipher.doFinal(padString(text).getBytes());
        } catch (Exception e)
        {
            throw new Exception("[encrypt] " + e.getMessage());
        }

        return encrypted;
    }

    public static byte[] decrypt(String code,String key) throws Exception
    {
        if(code == null || code.length() == 0)
            throw new Exception("Empty string");

        byte[] decrypted = null;

        doKey(key);

        try {
            cipher.init(Cipher.DECRYPT_MODE, Helpers.getPrivateKeyWithPemFormat(key));

            decrypted = cipher.doFinal( code.getBytes());
        } catch (Exception e)
        {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }



    public static String bytesToHex(byte[] data)
    {
        if (data==null)
        {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if ((data[i]&0xFF)<16)
                str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
        }
        return str;
    }


    public static byte[] hexToBytes(String str) {
        if (str==null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i=0; i<len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
            }
            return buffer;
        }
    }



    private static String padString(String source)
    {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++)
        {
            source += paddingChar;
        }

        return source;
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
}


package ca.xahive.app.bl.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import IAPUtils.Utils;

public class Crypto {
    private static final String ENCTYPE = "AES";

    /**
     * public static byte[] encryptData(String password, FileOutputStream fileOutputStream,FileInputStream fisTemp) {
     * <p/>
     * // byte[] data
     * <p/>
     * byte[] seed = getSeed(password);
     * SecretKeySpec skeySpec = null;
     * try {
     * skeySpec = new SecretKeySpec(getRawKey(seed), ENCTYPE);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * Cipher cipher = null;
     * try {
     * cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
     * byte[] iv = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
     * IvParameterSpec ivp = new IvParameterSpec(iv);
     * cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivp);
     * <p/>
     * byte[] b = new byte[1024];
     * <p/>
     * CipherOutputStream cos;
     * ByteArrayOutputStream bos = new ByteArrayOutputStream();
     * <p/>
     * //FileOutputStream fos;
     * cos = new CipherOutputStream(fileOutputStream, cipher);
     * FileInputStream fis = fisTemp ;
     * int i = fis.read(b);
     * while (i != -1) {
     * cos.write(b, 0, i);
     * i = fis.read(b);
     * bos.write(b,0,i);
     * <p/>
     * }
     * cos.flush();
     * cos.close();
     * fis.close();
     * <p/>
     * <p/>
     * byte[] bytes = bos.toByteArray();
     * return  bytes;
     * //byte[] encrypted = cipher.doFinal(clear);
     * //return encrypted;
     * } catch (NoSuchAlgorithmException e) {
     * e.printStackTrace();
     * } catch (NoSuchPaddingException e) {
     * e.printStackTrace();
     * }   catch (InvalidAlgorithmParameterException e) {
     * e.printStackTrace();
     * } catch (InvalidKeyException e) {
     * e.printStackTrace();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * return null ;
     * /**
     * byte[] output = null;
     * <p/>
     * try {
     * byte[] seed = getSeed(password);
     * output = Crypto.encrypt(seed, data);
     * } catch (BadPaddingException paddingE) {
     * return output;
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * <p/>
     * return output;
     * }
     **/
    public static byte[] encryptData(String password, byte[] data) {
        byte[] output = null;

        try {
            byte[] seed = getSeed(password);
            output = Crypto.encrypt(seed, data);
        } catch (BadPaddingException paddingE) {
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    public static byte[] decryptData(String password, byte[] data) {
        byte[] output = null;

        try {
            byte[] seed = getSeed(password);
            output = Crypto.decrypt(seed, data);
        } catch (BadPaddingException paddingE) {
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * public static byte[]  decryptDataFile(String password, byte[] data ) {
     * // byte[] data
     * byte[] output = null;
     * <p/>
     * try {
     * FileOutputStream out = new FileOutputStream(data);
     * out.write(data);
     * out.close();
     * FileInputStream fis= new FileInputStream("");
     * <p/>
     * byte[] seed = getSeed(password);
     * SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(seed), ENCTYPE);
     * Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
     * byte[] iv = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
     * IvParameterSpec ivp = new IvParameterSpec(iv);
     * cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivp);
     * //byte[] decrypted = cipher.doFinal(encrypted, 0, encrypted.length);
     * //return decrypted;
     * <p/>
     * CipherInputStream cis = new CipherInputStream(fis, cipher);
     * int b;
     * byte[] d = new byte[1024];
     * ByteArrayOutputStream bos = new ByteArrayOutputStream();
     * <p/>
     * while((b = cis.read(d)) != -1) {
     * bos.write(d,0,b );
     * //fos.write(d, 0, b);
     * }
     * //fos.flush();
     * //fos.close();
     * cis.close();
     * return  bos.toByteArray();
     * // output = Crypto.decrypt(seed, data);
     * // } catch (BadPaddingException paddingE) {
     * //  Log.v("sssss",paddingE.getMessage());
     * //return output;
     * }
     * catch (Exception e) {
     * e.printStackTrace();
     * }
     * <p/>
     * return output;
     * }
     **/


    public static byte[] getRawKey(byte[] seed) throws Exception {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(seed);
        byte raw[] = digest.digest();

        return raw;
    }

    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(raw), ENCTYPE);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        byte[] iv = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        IvParameterSpec ivp = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivp);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(raw), ENCTYPE);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        byte[] iv = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        IvParameterSpec ivp = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivp);
        byte[] decrypted = cipher.doFinal(encrypted, 0, encrypted.length);
        return decrypted;
    }

    private static byte[] getSeed(String password) {
        return String.format("%s", password).getBytes();
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }


    private static String RSA_CONFIGURATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static String RSA_PROVIDER = "BC";

    public static String decryptRsa(Key key, String base64cypherText) throws Exception {
        Cipher c = Cipher.getInstance(RSA_CONFIGURATION, RSA_PROVIDER);
        c.init(Cipher.DECRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT));
        byte[] decodedBytes = c.doFinal(Base64.decode(base64cypherText.getBytes("UTF-8"), Base64.DEFAULT));
        String clearText = new String(decodedBytes);
        return clearText;
    }

    private static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }


    private static Charset PLAIN_TEXT_ENCODING = Charset.forName("UTF-8");

    /**
     * Decrypts a byte[] using RSA with the private key
     *
     * @return byte[] of the decrypted message
     * @throws Exception
     **/
    public static String RSADecrypt(String ciphertext, String privatePem)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        if (ciphertext.length() == 0) return null;
        byte[] dec = Utils.base64StringToByteArray(ciphertext);

        cipher.init(Cipher.DECRYPT_MODE, Helpers.getPrivateKeyWithPemFormat(privatePem));
        byte[] decrypted = new byte[0];
        // try {
        decrypted = cipher.doFinal(dec);

        return new String(decrypted, PLAIN_TEXT_ENCODING);
    }


    public static String RSAEncrypt(String plaintext, String pemPublic)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        if (plaintext.length() == 0) return null;
        //  try {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, Helpers.getPublicKeyFromPemFormat(pemPublic, false));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        byte[] encrypted = new byte[0];
        try {
            encrypted = cipher.doFinal(plaintext.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //return new String(encrypted);
        return Utils.byteArrayToBase64String(encrypted);
        // return  new String(encrypted);

    }

    /**
     * Strips the headers from the public key
     *
     * @throws Exception
     */
    public static String stripPublicKeyHeaders(String key) {
        //strip the headers from the key string
        StringBuilder strippedKey = new StringBuilder();
        String lines[] = key.split("\n");
        for (String line : lines) {
            if (!line.contains("BEGIN PUBLIC KEY") && !line.contains("END PUBLIC KEY")) {
                strippedKey.append(line.trim());
            }
        }
        return strippedKey.toString().trim();
    }


    /**
     * @return RSA cipher with OAEP padding
     * @throws Exception
     */
    public static Cipher getRSACipher() throws Exception {
        return Cipher.getInstance(TRANSFORMATION);
    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
}
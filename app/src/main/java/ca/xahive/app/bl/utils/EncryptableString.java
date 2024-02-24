package ca.xahive.app.bl.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptableString {
    private final int HASH_SIZE = 16;
    private final int HEADER_SIZE = HASH_SIZE;
    private final String EMPTY_HASH = "d41d8cd98f00b204e9800998ecf8427e";

    private String originalString;
    private byte[] preparedData;

    public String getOriginalString() {
        if (originalString == null && preparedData != null) {
            originalString = this.buildString(this.preparedData);
        }
        return originalString;
    }

    public void setOriginalString(String originalString) {
        this.originalString = originalString;
        this.preparedData = null;
    }

    public byte[] getPreparedData() {
        if (preparedData == null && originalString != null) {
            preparedData = this.buildPreparedData(originalString);
        }
        return preparedData;
    }

    public void setPreparedData(byte[] preparedData) {
        this.preparedData = preparedData;
        this.originalString = null;
    }

    private byte[] buildPreparedData(String string) {
        byte[] out = null;
        byte[] stringBytes = string.getBytes();

        try {
            byte[] hash = Crypto.getRawKey(string.getBytes());
            out = new byte[hash.length + stringBytes.length];
            System.arraycopy(hash, 0, out, 0, hash.length);
            System.arraycopy(stringBytes, 0, out, hash.length, stringBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    private String buildString(byte[] bytes) {
        byte[] hashComponent = new byte[HASH_SIZE];
        byte[] stringComponent = new byte[bytes.length-HASH_SIZE];
        String out = null;

        if (bytes.length < HEADER_SIZE) {
            return null;
        }

        System.arraycopy(bytes, 0, hashComponent, 0, HASH_SIZE);
        System.arraycopy(bytes, HASH_SIZE, stringComponent, 0, (bytes.length-HASH_SIZE));

        String hexOfHash = Crypto.toHex(hashComponent);

        if (bytes.length == HEADER_SIZE && hexOfHash.equalsIgnoreCase(EMPTY_HASH)) {
            out = "";
        }
        else {
            // an actual string in this case.
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(stringComponent);
                byte md5hash[] = digest.digest();
                String md5Hex = Crypto.toHex(md5hash);
                if (hexOfHash.equals(md5Hex)) {
                    out = new String(stringComponent);
                }
                else {
                    XADebug.d("Encryptable string FAILED hash test.");
                    out = null;
                }

            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return out;
    }


}

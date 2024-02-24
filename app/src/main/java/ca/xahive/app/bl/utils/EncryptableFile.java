package ca.xahive.app.bl.utils;

import java.util.Arrays;

/**
 * Created by Iversoft on 2014-05-13.
 */
public class EncryptableFile {
    private String fileName;
    private String mimeType;
    private byte[] originalData;
    private byte[] preparedData;

    private static final int kXAHEncryptableFileHeaderSize = 384;
    private static final int kXAHEncryptableFileHashOffset = 0;
    private static final int kXAHEncryptableFileHashLength = 16;
    private static final int kXAHEncryptableFileMimeOffset = 16;
    private static final int kXAHEncryptableFileMimeLength = 110;
    private static final int kXAHEncryptableFileNameOffset = 128;
    private static final int kXAHEncryptableFileNameLength = 255;

    //property getters and setters
    public String getFileName() {
        if (fileName == null) {
            fileName = extractEncryptableFileName(getPreparedData());
        }

        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return "";
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getOriginalData() {
        if (originalData == null
                && preparedData != null) {
            if (preparedData.length > kXAHEncryptableFileHeaderSize) {
                byte[] originalDataByteArray = extractEncryptableFileData(preparedData);
                byte[] hashFromHeader = extractEncryptableFileHash(preparedData);

                try {
                    byte[] testHash = Crypto.getRawKey(originalDataByteArray);

                    if (Arrays.equals(testHash, hashFromHeader)) {
                        this.originalData = originalDataByteArray;
                        XADebug.d("Decrypted file appears to be valid.");
                    } else {
                        XADebug.d("Decrypted file appears to be invalid (wrong password?).");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return originalData;
    }

    public void setOriginalData(byte[] originalData) {
        this.originalData = originalData;
        this.preparedData = null;
    }

    public byte[] getPreparedData() {
        if (preparedData == null
                && originalData != null) {
            preparedData = prepareEncryptableFileData(getFileName(), getMimeType(), getOriginalData());
        }

        return preparedData;
    }

    public void setPreparedData(byte[] preparedData) {
        this.preparedData = preparedData;
        this.fileName = null;
        this.mimeType = null;
        this.originalData = null;
    }

    //methods
    public byte[] prepareEncryptableFileData(String fileName, String mimeType, byte[] originalData) {
        byte[] preparedData = new byte[originalData.length + kXAHEncryptableFileHeaderSize];

        try {
            byte[] hashArray = Crypto.getRawKey(originalData);
            byte[] mimeArray = mimeType.getBytes();
            byte[] fileNameArray = fileName.getBytes();

            for (int i = kXAHEncryptableFileHashOffset; i < preparedData.length; i++) {
                if (i >= kXAHEncryptableFileHashOffset && i < kXAHEncryptableFileHashLength) {
                    if (hashArray.length > 0
                            && i < hashArray.length) {
                        preparedData[i] = hashArray[i];
                    } else {
                        preparedData[i] = 0;
                    }
                } else if (i >= kXAHEncryptableFileMimeOffset && i < kXAHEncryptableFileMimeOffset + kXAHEncryptableFileMimeLength) {
                    if (mimeArray.length > 0
                            && i - kXAHEncryptableFileMimeOffset < mimeArray.length) {
                        preparedData[i] = mimeArray[i - kXAHEncryptableFileMimeOffset];
                    } else {
                        preparedData[i] = 0;
                    }
                } else if (i >= kXAHEncryptableFileNameOffset && i < kXAHEncryptableFileNameOffset + kXAHEncryptableFileNameLength) {
                    if (fileNameArray.length > 0
                            && i - kXAHEncryptableFileNameOffset < fileNameArray.length) {
                        preparedData[i] = fileNameArray[i - kXAHEncryptableFileNameOffset];
                    } else {
                        preparedData[i] = 0;
                    }
                } else if (i >= kXAHEncryptableFileHeaderSize) {
                    if (preparedData.length > 0
                            && i - kXAHEncryptableFileHeaderSize < originalData.length) {
                        preparedData[i] = originalData[i - kXAHEncryptableFileHeaderSize];
                    } else {
                        preparedData[i] = 0;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return preparedData;
    }

    public byte[] extractEncryptableFileHash(byte[] data) {
        if(data.length < kXAHEncryptableFileHeaderSize){
            return null;
        }

        byte[] hash = new byte[kXAHEncryptableFileHashLength];
        System.arraycopy(data, kXAHEncryptableFileHashOffset, hash, 0, kXAHEncryptableFileHashLength);

        return hash;
    }

    public String extractEncryptableFileName(byte[] data) {
        if(data.length < kXAHEncryptableFileHeaderSize){
            return null;
        }

        String fileName = "";
        byte[] nameBytes = new byte[kXAHEncryptableFileNameLength];
        System.arraycopy(data, kXAHEncryptableFileNameOffset, nameBytes, 0, kXAHEncryptableFileNameLength);

        for (int idx = 0; idx < kXAHEncryptableFileNameLength; idx++) {
            char character = (char)nameBytes[idx];

            if (character == '\0') {
                break;
            } else {
                fileName += character;
            }
        }

        return fileName;
    }

    public String extractEncryptableFileMime(byte[] data) {
        if(data.length < kXAHEncryptableFileHeaderSize){
            return null;
        }

        String mime = "";
        byte[] mimeBytes = new byte[kXAHEncryptableFileMimeLength];
        System.arraycopy(data, kXAHEncryptableFileMimeOffset, mimeBytes, 0, kXAHEncryptableFileMimeLength);

        for (int idx = 0; idx < kXAHEncryptableFileMimeLength; idx++) {
            char character = (char)mimeBytes[idx];

            if (character == '\0') {
                break;
            } else {
                mime += character;
            }
        }

        return mime;
    }

    public byte[] extractEncryptableFileData(byte[] data) {
        if(data.length < kXAHEncryptableFileHeaderSize){
            return null;
        }
        else if(data.length == kXAHEncryptableFileHeaderSize){
            return data;
        }

        byte[] output = new byte[data.length - kXAHEncryptableFileHeaderSize];
        System.arraycopy(data, kXAHEncryptableFileHeaderSize, output, 0, data.length - kXAHEncryptableFileHeaderSize);

        return output;
    }
}

package ca.xahive.app.bl.local;

import android.util.SparseArray;

import ca.xahive.app.bl.utils.Helpers;


public class DecryptedMessageMemoryCache implements DecryptedMessageCacheInterface {
    private SparseArray<String> passwordCache;

    public SparseArray<String> getPasswordCache() {
        if (passwordCache == null) {
            passwordCache = new SparseArray<String>();
        }
        return passwordCache;
    }

    @Override
    public boolean hasDecryptedTextForMessageId(int messageId) {
        return Helpers.stringIsNotNullAndMeetsMinLength(decryptedTextForMessageId(messageId), 1);
    }

    @Override
    public String decryptedTextForMessageId(int messageId) {
        return getPasswordCache().get(messageId);
    }

    @Override
    public void setDecryptedTextForMessageId(String text, int messageId) {
        if (text != null) {
            getPasswordCache().put(messageId, text);
        }
        else {
            getPasswordCache().delete(messageId);
        }
    }
}

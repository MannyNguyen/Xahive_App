package ca.xahive.app.bl.local;

public interface DecryptedMessageCacheInterface {
    public abstract boolean hasDecryptedTextForMessageId(int messageId);
    public abstract String decryptedTextForMessageId(int messageId);
    public abstract void setDecryptedTextForMessageId(String text, int messageId);
}

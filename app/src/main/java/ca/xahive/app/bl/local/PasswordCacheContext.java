package ca.xahive.app.bl.local;

public enum PasswordCacheContext {
    BUZZ(0),
    PRIVATE(1),
    ATTACHMENT(2);

    private final int value;

    PasswordCacheContext(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

package ca.xahive.app.bl.objects;

public enum ModelEvent {
    INITIAL(0),
    USER_CHANGED(1),
    LOGIN_FAILED(2),
    SIGNIFICANT_DIST_CHANGE(3),
    LOGIN_USER_UPDATE_FAILED(4),
    DISABLED_ADS(5),
    CONTACT_UPDATE(6),
    ADD_CONTACT_UPDATE(7),

    CONTACT_UPDATE_FAILED(8),
    CREATE_HIVE(9),

    MESSAGE_UPDATE(77);

    private final int value;

    ModelEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

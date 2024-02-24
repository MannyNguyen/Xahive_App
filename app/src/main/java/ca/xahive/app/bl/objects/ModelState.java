package ca.xahive.app.bl.objects;

public enum ModelState {
    STALE(0),
    PENDING(1),
    CURRENT(2),
    ERROR(3);

    private final int value;

    ModelState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

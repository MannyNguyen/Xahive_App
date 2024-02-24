package ca.xahive.app.bl.error;

/**
 * Generic application error
 */
public class ApplicationError extends Exception {

    private int code;

    public static final int NO_NETWORK = 1;

    public static final int AUTHORIZATION_CANCELED = 2;

    public static final int REAUTHORIZATION_NEEDED = 190;

    public ApplicationError() {
    }

    public ApplicationError(String detailMessage) {
        this(0, detailMessage);
    }

    public ApplicationError(Throwable throwable) {
        this(0, throwable);
    }

    public ApplicationError(int code, String detailMessage) {
        super(detailMessage);
        this.code = code;
    }

    public ApplicationError(int code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

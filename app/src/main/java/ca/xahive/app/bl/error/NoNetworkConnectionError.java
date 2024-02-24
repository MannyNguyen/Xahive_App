package ca.xahive.app.bl.error;

/**
 * No network connection error
 */
public class NoNetworkConnectionError extends ApplicationError {
    public NoNetworkConnectionError() {
        super(ApplicationError.NO_NETWORK, "No network connection");
    }
}

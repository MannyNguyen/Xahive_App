package ca.xahive.app.bl.api.callback;


import ca.xahive.app.bl.error.ApplicationError;

/**
 * Callback interface for result handling
 */
public interface ResultCallback<T,X> {

    public void onSuccess(T request, X result);

    public void onError(ApplicationError error);
}

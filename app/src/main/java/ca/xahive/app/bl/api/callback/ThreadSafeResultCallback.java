package ca.xahive.app.bl.api.callback;

import android.os.Handler;
import android.os.Looper;

import ca.xahive.app.bl.error.ApplicationError;


/**
 * Thread safe result callback
 */
public abstract class ThreadSafeResultCallback<T, X> implements ResultCallback<T, X> {

    private Handler handler;

    public ThreadSafeResultCallback(Looper looper) {
        handler = new Handler(looper);
    }

    @Override
    public void onSuccess(final T request, final X result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doOnSuccess(request, result);
            }
        });
    }

    @Override
    public void onError(final ApplicationError error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doOnError(error);
            }
        });
    }

    public abstract void doOnSuccess(T request, X result);

    public abstract void doOnError(ApplicationError error);
}

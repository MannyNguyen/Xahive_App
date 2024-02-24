package ca.xahive.app.bl.api.callback;

/**
 * Created by VanQuan on 30/08/2014.
 */
public abstract class CallBackFromXahive<T, X> {
    public abstract void done(T request, X result);
}

package ca.xahive.app.bl.api.callback;


import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelObject;

public interface APICallbackInterface {
    public abstract void onStart();
    public abstract void onSuccess(ModelObject modelObject);
    public abstract void onFail(ModelError error);
    public abstract void onComplete();
}

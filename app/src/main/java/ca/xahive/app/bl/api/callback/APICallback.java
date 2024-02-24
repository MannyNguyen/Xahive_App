package ca.xahive.app.bl.api.callback;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.utils.XADebug;

public class APICallback implements APICallbackInterface {
    @Override
    public void onStart() {
    }

    @Override
    public void onSuccess(ModelObject modelObject) {

        Model.getInstance().requestReturned(null);
    }

    @Override
    public void onFail(ModelError error) {
        Model.getInstance().requestReturned(error);
        XADebug.d("Response Error: " + error.getCode());
    }

    @Override
    public void onComplete() {

    }
}

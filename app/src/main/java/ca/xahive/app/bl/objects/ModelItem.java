package ca.xahive.app.bl.objects;

import android.util.Log;

import java.util.Date;

import ca.xahive.app.bl.local.InstantObservable;

public class ModelItem extends InstantObservable {
    protected Date stateChangeTime;
    protected ModelState state;
    protected ModelError error;
    protected ModelObject data;

    public ModelItem() {
        super();
        state = ModelState.STALE;
        stateChangeTime = new Date(0);
    }

    public long getAge() {
        return (new Date()).getTime() - stateChangeTime.getTime();
    }

    public ModelState getState() {
        return state;
    }

    public void setState(ModelState state) {
        this.state = state;

        if (state != ModelState.ERROR) {
            setError(null);
        }

        stateChangeTime = new Date();

        setChanged();
        notifyObservers();
    }

    public ModelError getError() {
        return error;
    }

    public void setError(ModelError error) {
        this.error = error;

        if (error != null) {
            setState(ModelState.ERROR);
        }
    }

    public ModelObject getData() {
        return data;
    }

    public void setData(ModelObject data) {
        this.data = data;


        if (data == null) {
            Log.v("data","nulll");
            setError(new ModelError(ModelError.INTERNAL_ERROR, null));
        }
        else {
            Log.v("data","CURRENT");
            setState(ModelState.CURRENT);
        }
    }


    public void setPendingIfError() {
        if (getState() == ModelState.ERROR) {
            setState(ModelState.PENDING);
        }
    }
}

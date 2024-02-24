package ca.xahive.app.bl.objects;

import org.json.JSONObject;

import ca.xahive.app.bl.local.InstantObservable;
import ca.xahive.app.bl.utils.XADebug;

public class BaseModelObject extends InstantObservable implements ModelObject {
    protected boolean clean;

    @Override
    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        XADebug.d(this.getClass().toString() + " Needs to implement setValuesWithJSON.");
    }

    @Override
    public JSONObject getJSONRepresentation() {
        XADebug.d(this.getClass().toString() + " Needs to implement getJSONRepresentation.");
        return null;
    }

    @Override
    public String getKeyPath() {
        XADebug.d(this.getClass().toString() + " Needs to implement Key Path.");
        return null;
    }

}

package ca.xahive.app.bl.objects;

import org.json.JSONObject;

public interface IModelObject {
    public abstract boolean isClean();
    public abstract void setValuesWithJSON(Object jsonObject);
    public abstract JSONObject getJSONRepresentation();
    public abstract String getKeyPath();
}

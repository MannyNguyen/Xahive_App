package ca.xahive.app.bl.objects;

import org.json.JSONObject;

public class HiveListItem extends BaseModelObject {
    public int hiveId;
    public String hiveName;
    public boolean isDefault;
    public String publicKey;
    public int getHiveId() { return hiveId; }

    public void setHiveId(int hiveId) { this.hiveId = hiveId; }

    public String getName() { return hiveName; }

    public void setName(String name) { this.hiveName = name; }

    public boolean isDefault() { return isDefault; }

    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = (JSONObject)jsonObject;

        setHiveId( jsObj.optInt("hiveId", 0) );
        setName(jsObj.optString("name", null));
        setDefault(jsObj.optBoolean("isDefault", false));

        setClean(getHiveId() != 0);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null; // We don't send these.
    }

    @Override
    public String getKeyPath() {
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + ": " + this.getName() + " " + this.isDefault();
    }
}

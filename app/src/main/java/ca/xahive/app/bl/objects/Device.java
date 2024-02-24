package ca.xahive.app.bl.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Iversoft on 2014-09-09.
 */
public class Device extends BaseModelObject {
    private int userId;
    private String deviceId;
    private String platform;
    private boolean shouldDelete;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isShouldDelete() {
        return shouldDelete;
    }

    public void setShouldDelete(boolean shouldDelete) {
        this.shouldDelete = shouldDelete;
    }

    @Override
    public String getKeyPath() {
        return "device";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = null;

        try {
            jsObj = (JSONObject) jsonObject;
        }
        catch (ClassCastException e) {
            JSONArray jsonArray = (JSONArray) jsonObject;
            jsObj = jsonArray.optJSONObject(0);
        }

        if (jsObj != null) {
            setUserId(jsObj.optInt("userId", 0));
            setDeviceId(jsObj.optString("deviceId", null));
            setPlatform(jsObj.optString("platform", null));
            setShouldDelete(jsObj.optBoolean("shouldDelete", false));
        }

        setClean(true);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();

            jsObj.put("userId", getUserId());
            jsObj.put("deviceId", getDeviceId());
            jsObj.put("platform", getPlatform());
            jsObj.put("shouldDelete", isShouldDelete());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //helper methods
    public static Device deviceForUserIdWithDeviceId(int userId, String deviceId, String platform, boolean shouldDelete){
        Device device = new Device();
        device.setUserId(userId);
        device.setDeviceId(deviceId);
        device.setPlatform(platform);
        device.setShouldDelete(shouldDelete);
        return device;
    }
}

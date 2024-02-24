package ca.xahive.app.bl.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.xahive.app.bl.local.Model;


public class HiveBasedSettings extends BaseModelObject {
    private boolean isHoneycombAutoFollow;
    private boolean isAnonBuzzAllowed;
    private boolean isNonContactBuzzAllowed;
    private double userListenMult;
    private double userBuzzMult;
    private double maxBuzzDistance;
    private double maxListenDistance;
    private double maxBuzzFileSize;
    private double maxMessageFileSize;
    private boolean isCurrentHiveSettings;

    public boolean isHoneycombAutoFollow() {
        return isHoneycombAutoFollow;
    }

    public void setHoneycombAutoFollow(boolean isHoneycombAutoFollow) {
        this.isHoneycombAutoFollow = isHoneycombAutoFollow;
        update();
    }

    public boolean isAnonBuzzAllowed() {
        return isAnonBuzzAllowed;
    }

    public void setAnonBuzzAllowed(boolean isAnonBuzzAllowed) {
        this.isAnonBuzzAllowed = isAnonBuzzAllowed;
        update();
    }

    public boolean isNonContactBuzzAllowed() {
        return isNonContactBuzzAllowed;
    }

    public void setNonContactBuzzAllowed(boolean isNonContactBuzzAllowed) {
        this.isNonContactBuzzAllowed = isNonContactBuzzAllowed;
        update();
    }

    public double getUserListenMult() {
        return userListenMult;
    }

    public void setUserListenMult(double userListenMult) {
        if (userListenMult >= 0.0 && userListenMult <= 1.0) {
            this.userListenMult = userListenMult;
            update();
        }
    }

    public double getUserBuzzMult() {
        return userBuzzMult;
    }

    public void setUserBuzzMult(double userBuzzMult) {
        if (userBuzzMult >= 0.0 && userBuzzMult <= 1.0) {
            this.userBuzzMult = userBuzzMult;
            update();
        }
    }

    public double getMaxBuzzDistance() {
        return maxBuzzDistance;
    }

    public void setMaxBuzzDistance(double maxBuzzDistance) {
        this.maxBuzzDistance = maxBuzzDistance;
    }

    public double getMaxListenDistance() {
        return maxListenDistance;
    }

    public void setMaxListenDistance(double maxListenDistance) {
        this.maxListenDistance = maxListenDistance;
    }

    public double getMaxBuzzFileSize() {
        return maxBuzzFileSize;
    }

    public void setMaxBuzzFileSize(double maxBuzzFileSize) {
        this.maxBuzzFileSize = maxBuzzFileSize;
    }

    public double getMaxMessageFileSize() {
        return maxMessageFileSize;
    }

    public void setMaxMessageFileSize(double maxMessageFileSize) {
        this.maxMessageFileSize = maxMessageFileSize;
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONArray jsArr = (JSONArray) jsonObject;

        try {
            JSONObject jsObj = jsArr.getJSONObject(0);

            setHoneycombAutoFollow(jsObj.optBoolean("isHoneycombAutoFollow", false));
            setUserListenMult(jsObj.optDouble("userListenMult", 0.0));
            setUserBuzzMult(jsObj.optDouble("userBuzzMult", 0.0));
            setMaxBuzzDistance(jsObj.optDouble("maxBuzzDistance", 0.0));
            setMaxListenDistance(jsObj.optDouble("maxListenDistance", 0.0));
            setMaxMessageFileSize(jsObj.optDouble("maxMessageFileSize", 0.0));
            setMaxBuzzFileSize(jsObj.optDouble("maxBuzzFileSize", 0.0));
            setAnonBuzzAllowed(jsObj.optBoolean("isAnonBuzzAllowed", false));
            setNonContactBuzzAllowed(jsObj.optBoolean("isNonContactBuzzAllowed", false));

            setClean(true); // No way to know.
        } catch (JSONException e) {
            e.printStackTrace();
            setClean(false);
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();
            jsObj.put("isHoneycombAutoFollow", isHoneycombAutoFollow());
            jsObj.put("userListenMult", getUserListenMult());
            jsObj.put("userBuzzMult", getUserBuzzMult());
            jsObj.put("maxBuzzDistance", getMaxBuzzDistance());
            jsObj.put("maxListenDistance", getMaxListenDistance());
            jsObj.put("maxMessageFileSize", getMaxMessageFileSize());
            jsObj.put("maxBuzzFileSize", getMaxBuzzFileSize());
            jsObj.put("isAnonBuzzAllowed", isAnonBuzzAllowed());
            jsObj.put("isNonContactBuzzAllowed", isNonContactBuzzAllowed());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getKeyPath() {
        return "hiveSetting";
    }

    public void setIsCurrentHiveSettings(boolean isCurrentHiveSettings) {
        this.isCurrentHiveSettings = isCurrentHiveSettings;
    }

    public boolean isCurrentHiveSettings() {
        return isCurrentHiveSettings;
    }

    private void update() {
        if (isCurrentHiveSettings()) {
          // Model.getInstance().updateHiveSettings();
        }
    }
}

package ca.xahive.app.bl.objects;

import org.json.JSONException;
import org.json.JSONObject;

import ca.xahive.app.bl.utils.ModelUtils;

public class Buzz extends Message {
    private int buzzId;
    private String honeycomb;
    private double latitude;
    private double longitude;
    private double broadcastDist;

    public int getBuzzId() {
        return buzzId;
    }

    public void setBuzzId(int buzzId) {
        this.buzzId = buzzId;
    }

    public String getHoneycomb() {
        return honeycomb;
    }

    public void setHoneycomb(String honeycomb) {
        this.honeycomb = honeycomb;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getBroadcastDist() {
        return broadcastDist;
    }

    public void setBroadcastDist(double broadcastDist) {
        this.broadcastDist = broadcastDist;
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {

        JSONObject jsObj = (JSONObject)jsonObject;

        setBuzzId( jsObj.optInt("messageId", 0) );
        setMessageId( jsObj.optInt("messageId", 0) );
        setFromUserId( jsObj.optInt("fromUserId", 0) );
        setDate( ModelUtils.dateFromJSON(jsObj, "date") );
        setContent( jsObj.optString("content", null) );
        setHoneycomb( jsObj.optString("honeycomb", null) );
        setLatitude( jsObj.optDouble("latitude", 0.0) );
        setLongitude( jsObj.optDouble("longitude", 0.0) );
        setBroadcastDist(jsObj.optDouble("broadcastDist", 0.0));
        setEncrypted( jsObj.optBoolean("isEncrypted", false) );
        setAttachmentId(jsObj.optInt("attachmentId", 0));
        setFilename(jsObj.optString("filename", null));
        setFilesize(jsObj.optDouble("filesize", 0.0));

        setClean(getMessageId() != 0);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();

            jsObj.put("buzzId", getBuzzId());
            jsObj.put("messageId", getMessageId());
            jsObj.put("fromUserId", getFromUserId());
            jsObj.put("date", getDate());
            jsObj.put("content", getContent());
            jsObj.put("honeycomb", getHoneycomb());
            jsObj.put("latitude", getLatitude());
            jsObj.put("longitude", getLongitude());
            jsObj.put("broadcastDist", getBroadcastDist());
            jsObj.put("isEncrypted", isEncrypted());
            jsObj.put("attachmentId", getAttachmentId());
            jsObj.put("filename", getFilename());
            jsObj.put("filesize", getFilesize());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getKeyPath() {
        return "buzz";
    }

}

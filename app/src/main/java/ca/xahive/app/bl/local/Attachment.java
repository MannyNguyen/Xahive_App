package ca.xahive.app.bl.local;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.xahive.app.bl.objects.BaseModelObject;
import ca.xahive.app.bl.utils.Config;

public class Attachment extends BaseModelObject {
    private int attachmentId;
    private String filename;
    private double filesize;

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public double getFilesize() {
        return filesize;
    }

    public void setFilesize(double filesize) {
        this.filesize = filesize;
    }

    public String getAttachmentKey() {

        String key;

        if(this.attachmentId > 0) {
            key = String.format(Config.ATTACHMENT_FILE_NAME, this.attachmentId);
        } else {
            key = "";
        }

        return key;
    }

    public Attachment () {
        super ();
    }

    public Attachment (String name, double size) {

        super();
        setFilename(name);
        setFilesize(size);
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONArray jsArr = (JSONArray) jsonObject;
        try {
            JSONObject jsObj = jsArr.getJSONObject(0);

            setAttachmentId(jsObj.optInt("attachmentId", 0));
            setFilename(jsObj.optString("filename", null));
            setFilesize(jsObj.optDouble("filesize", 0.0));

            setClean(getAttachmentId() > 0);

        } catch (JSONException e) {
            e.printStackTrace();
            setClean(false);
        }

    }


    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();
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
        return "attachment";
    }


}

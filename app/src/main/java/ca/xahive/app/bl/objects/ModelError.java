package ca.xahive.app.bl.objects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelError extends BaseModelObject {
    public static final String UNSPECIFIED_ERROR = "UNSPECIFIED_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String CONNECT_ERROR = "CONNECT_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String LOCATION_ERROR = "LOCATION_ERROR";
    public static final String SEND_MSG_ERROR = "SEND_MSG_ERROR";

    private String code;
    private String message;
    private boolean handled;

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        // TODO: intercept and localize
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public String getReadableCode() {
        return ModelErrorMap.get(getCode());
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public boolean isReachabilityError() {
        return (getCode().equals(ModelError.CONNECT_ERROR));
    }

    public ModelError() {
        super();
    }

    public ModelError(String code, String message) {
        super();
        setCode(code);
        setMessage(message);
    }

    public ModelError(String code) {
        super();
        setCode(code);
    }

    @Override
    public String getKeyPath() {
        return "error";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jso = (JSONObject) jsonObject;
        try {
            if (jso.has("code")) {
                applyValuesFromJSON(jso);
            } else if (jso.has("error")) {
                jso = jso.getJSONObject("error");
                applyValuesFromJSON(jso);
            } else {
                setCode("Unspecified error");
            }
            setClean(true);
        } catch (Exception e) {
            try {
                applyValuesFromJSON(null);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            setClean(false);
        }
    }

    private void applyValuesFromJSON(JSONObject jsonObject) throws JSONException {
        setCode(jsonObject.optString("code", null));
        setMessage(jsonObject.optString("message", null));
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + ": " + code;
    }
}


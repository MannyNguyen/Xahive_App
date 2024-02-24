package ca.xahive.app.bl.utils;

import android.content.Context;
import android.util.TypedValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ca.xahive.app.bl.objects.ModelObject;

public class ModelUtils {
    public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000Z'";

    public static Date dateFromJSON(JSONObject jsObj, String key) {
        String timestamp = jsObj.optString(key, "1969-12-31T23:59:59.000Z");

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = format.parse(timestamp);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(0);
        }
    }

    public static boolean booleanFromJSONInteger(JSONObject jsObj, String key) {
        int intValue = 0;

        try {
            intValue = jsObj.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (intValue != 0);
    }

    public static JSONObject JSONObjectInKeyPath(JSONObject existing, String keyPath) {
        if (keyPath == null) {
            return existing;
        }

        JSONObject jsOut = new JSONObject();

        try {
            jsOut.put(keyPath, existing);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsOut;
    }

    public static ArrayList arrayListFromJSON(JSONObject parentJSON, String key, Class objClass) {
        if (parentJSON == null) {
            XADebug.d("JSONObject is null, returning nothing");
            return new ArrayList();
        }

        try {
            JSONArray arr = parentJSON.getJSONArray(key);
            return arrayListFromJSONArray(arr, objClass);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList arrayListFromJSONArray(JSONArray jsArray, Class objClass) {
        ArrayList array = new ArrayList();

        if (jsArray == null) {
            XADebug.d("JSONArray is null");
            return null;
        }

        for (int i = 0 ; i < jsArray.length() ; i++) {
            try {
                JSONObject jsObj = jsArray.getJSONObject(i);
                ModelObject obj = (ModelObject)objClass.newInstance();
                obj.setValuesWithJSON(jsObj);
                array.add(obj);

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    public static int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}

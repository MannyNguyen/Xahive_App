package ca.xahive.app.webservice;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import ca.xahive.app.bl.Enums.API_Method;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.utils.XADebug;

/**
 * Created by Tung on 10/01/2015.
 */
public class CallBackDone extends AsyncTask<List<Object>, String, JSONObject> {

    private OnTaskComplete onTaskComplete;
    private String method;

    public interface OnTaskComplete {
        public void setMyTaskComplete(JSONObject result);

        public void onStart();

        public void onSuccess(ModelObject modelObject);

        public void onFail(ModelError error);

        public void onComplete();
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }

    public CallBackDone() {

    }

    public CallBackDone(Context context) {

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected JSONObject doInBackground(List<Object>... params) {

        //Generating random number between:
        //params[0] is url
        //params[1] is method
        JSONParser json = new JSONParser();

        String url = params[0].get(0).toString();
        String method = params[0].get(1).toString();
        Object param = (Object) params[0].get(2);

        this.method = method;
        JSONObject result = null;
        if (this.method == API_Method.POST) {
            result = json.makeHttpRequest(url, method, param);
        } else {
            result = json.getHttpRequest(url, method, (List<NameValuePair>) param);
        }
        //message
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        try {
            if (result.has("error") || result.has("code")) {

                onTaskComplete.setMyTaskComplete(result);
                onTaskComplete.onFail(processErrorResponse(result));
            } else {
                /**
                 Object modelJson = result.get(modelObj.getKeyPath());

                 modelObj.setValuesWithJSON(modelJson);

                 boolean clean = modelObj.isClean();

                 if (clean) {
                 modelObject = modelObj;
                 }
                 onTaskComplete.onSuccess(modelObject);**/

                onTaskComplete.setMyTaskComplete(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Class dataClassForPostResponse() {
        XADebug.d(this.getClass().toString() + " Need to implement dataClassForPostResponse");
        return null;
    }

    public Class dataClassForGetResponse() {
        XADebug.d(this.getClass().toString() + " Need to implement dataClassForGetResponse");
        return null;
    }


    private ModelError processErrorResponse(Object modelJSON) {
        ModelError error = new ModelError();
        error.setValuesWithJSON(modelJSON);

        if (!error.isClean()) {
            error = new ModelError(ModelError.UNSPECIFIED_ERROR, "");
        }
        return error;
        //processErrorResponse(error);
    }


}

package ca.xahive.app.bl.api.callback;

import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.XADebug;

public class APIConnection extends AsyncTask<Void, Void, Void> {
    protected Runnable callback;
    protected Hashtable<String,Object> params;
    protected String urlString;
    protected JSONObject responseJSON;
    private ModelError error;
   // private int hiveID = Model.getInstance().getHiveId();
    private  int hiveID =1;
    public int getHiveID() {
        return hiveID;
    }

    public void setHiveID(int hiveID) {
        this.hiveID = hiveID;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUrlString() {
        String urlFormat = "%s%s?hive=%d&latitude=%s&longitude=%s&client=%s&token=%s";
        String tokenStr = Model.getInstance().getCurrentUser().getToken();
        tokenStr = (tokenStr != null) ? tokenStr : "";

        Location currLocation = Model.getInstance().getLocationListener().getCurrentLocation();

        String url = String.format(
                urlFormat,
                Config.BASEURL,
                this.urlString,
                getHiveID(),
                currLocation.getLatitude(),
                currLocation.getLongitude(),
                Config.CLIENTID,
                tokenStr
        );

        return url;
    }

    protected void setResponseJSON(JSONObject responseJSON) {
        this.responseJSON = responseJSON;
    }

    public JSONObject getResponseJSON() {
        return responseJSON;
    }

    public ModelError getError() {
        return error;
    }

    public void setError(ModelError error) {
        if (error != null) {
            error.setClean(true);
        }
        this.error = error;
    }

    protected void fetch() {
        setError(null);
    }

    protected void readStream(BufferedInputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();

        try {
            String data;
            while ( (data = reader.readLine()) != null ) {
                sb.append(data);
            }

            data = sb.toString();
            XADebug.d("Got this response data back: " + data);

            setResponseJSON( new JSONObject(data) );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void readErrorStream(BufferedInputStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(err));
        StringBuilder sb = new StringBuilder();

        try {
            String data;
            while ( (data = reader.readLine()) != null ) {
                sb.append(data);
            }

            XADebug.d("Got this error response data back: " + sb.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        this.fetch();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.run();
    }
}

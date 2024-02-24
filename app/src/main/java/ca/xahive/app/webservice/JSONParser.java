package ca.xahive.app.webservice;

import android.util.Log;


import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

/**
 * Created by Tung on 09/01/2015.
 */
public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod

    public JSONObject makeHttpRequest(String url, String method,Object param) {

        // Making HTTP request
        try {
            // check for request method
            if(method.equals("POST")){
                Log.v("Create Response", "POST");
                // request method is POST
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                Gson gson = new Gson();
                StringEntity se = new StringEntity(gson.toJson(param));
                Log.v("testlog", "param---" + gson.toJson(param));

                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);
               // Log.v("testlog", "param" + param.toString());
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
               /**
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();**/
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        Log.v("Testlog",json);
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
    public JSONObject makeHttpRequest(String url ) {

        // Making HTTP request
       // JSONArray jsonArray =null;
        try {

            // check for request method
            /**   if(method == "POST"){
             // request method is POST
             // defaultHttpClient

             DefaultHttpClient httpClient = new DefaultHttpClient();
             HttpPost httpPost = new HttpPost(url);

             httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));


             if(Constants.cookieStore==null)
             {
             Constants.cookieStore = new BasicCookieStore();
             }
             HttpContext localContext = new BasicHttpContext();
             localContext.setAttribute(ClientContext.COOKIE_STORE, Constants.cookieStore);
             HttpResponse httpResponse = httpClient.execute(httpPost,localContext);
             HttpEntity httpEntity = httpResponse.getEntity();
             is = httpEntity.getContent();

             }else if(method == "GET"){**/
            // request method is GET
            DefaultHttpClient httpClient = new DefaultHttpClient();
             //   String paramString = URLEncodedUtils.format(params, "utf-8");
             // url += "?" + paramString;
            //   Log.v("paramString", paramString);
            HttpGet httpGet = new HttpGet(url);
            // CookieStore cookieStore = new BasicCookieStore();
            //  Cookie cookie = new BasicClientCookie("loginToken", "22222");

            // cookieStore.addCookie(cookie);
            /**   if(Constants.cookieStore==null)
             {
             Constants.cookieStore = new BasicCookieStore();
             }**/
            HttpContext localContext = new BasicHttpContext();
            // localContext.setAttribute(ClientContext.COOKIE_STORE, Constants.cookieStore);
            HttpResponse httpResponse = httpClient.execute(httpGet,localContext);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
            //  }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            //sb.append("{\"result\":[");
            while ((line = reader.readLine()) != null) {
               // Log.v("Lineee" , line);
                sb.append(line + "\n");
            }
            //sb.append("]}");
            is.close();
            json = sb.toString();

        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);

           // Log.v("JSONJSONJSONJSON", jObj.toString().substring(0,100));
          //  jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
      //  Log.v("resultttttt1222" ,jObj.toString());
        return jObj;

    }


    public JSONObject getHttpRequest(String url, String method,
                                      List<NameValuePair> params) {
          try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            String paramString = URLEncodedUtils.format(params, "utf-8");
            url += "?" + paramString;
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}
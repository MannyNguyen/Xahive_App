package ca.xahive.app.bl.api.queries;



import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.xahive.app.bl.Enums.API_Method;
import ca.xahive.app.webservice.CallBackDone;

/**
 * Created by Tung on 24/01/2015.
 */
public class GetJsonAPI {

     public static void getQueries(String url,String method, Object jsonObjectToCallAPI,CallBackDone callBackDone)
     {
         List<Object> listParams = new ArrayList<Object>();
         listParams.add(url);
         listParams.add(method);
         listParams.add(jsonObjectToCallAPI);

         callBackDone.execute(listParams);
     }


}

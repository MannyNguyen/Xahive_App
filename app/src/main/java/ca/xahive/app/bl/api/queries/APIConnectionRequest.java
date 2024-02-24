package ca.xahive.app.bl.api.queries;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import ca.xahive.app.bl.Enums.API_Method;
import ca.xahive.app.bl.objects.api_object.ChangeAliasHiveRequest;
import ca.xahive.app.bl.objects.api_object.DeleteHiveRequest;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.webservice.CallBackDone;

/**
 * Created by trantung on 10/6/15.
 */
public class APIConnectionRequest {

    public static void API_Login_Anon(CallBackDone callBackDone, Object jsonObject) {
        Log.v("TestLog", Config.BASEURL + Config.API_LOGIN_ANON);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_LOGIN_ANON, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_Login(CallBackDone callBackDone, Object jsonObject) {
        Log.v("TestLog", Config.BASEURL + Config.API_LOGIN);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_LOGIN, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_Signup(CallBackDone callBackDone, Object jsonObject) {
        Log.v("TestLog", Config.BASEURL + Config.API_LOGIN);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_SIGNUP, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_GetRelationship(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_GET_RelationShip + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_GET_RelationShip, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_GetPrivateKeyRequest(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_GET_Private_keys + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_GET_Private_keys, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_PostPrivateKeyRequest(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_POST_Private_keys + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_POST_Private_keys + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_GetMessageList(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_GET_MessageList + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_GET_MessageList, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_GetUserPublicKey(CallBackDone callBackDone, String API_Params, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_GET_UserPublicKey + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + API_Params, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_GetHiveList(CallBackDone callBackDone, Object jsonObject) {
        Log.v("TestLog", Config.BASEURL + Config.API_GET_HiveList);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_GET_HiveList, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_GetConversationItem(CallBackDone callBackDone, String API_Params, Object jsonObject) {
        Log.v("TestLogConversion", Config.BASEURL + API_Params);
        //Config.API_GET_ConversationItem
        GetJsonAPI.getQueries(Config.BASEURL + API_Params, API_Method.GET, jsonObject, callBackDone);
    }

    public static void API_PostConversationItem(CallBackDone callBackDone, String API_Params, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + API_Params + "?client=www" + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_UpdateSetting(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_UpdateSetting + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_UpdateSetting + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_UpdatePassword(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_UpdatePassword + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_UpdatePassword + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_AddContact(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_AddContact + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_AddContact + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_AddHive(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_CreateHive + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_CreateHive + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_UpdateUserPubicKey(CallBackDone callBackDone, String API_Params, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_CreateHive + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + API_Params + "?client=www" + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_AttachmentUpload(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_Attachment + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_Attachment + "?client=www" + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_HiveChange(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("TestLog", Config.BASEURL + Config.API_HiveChange + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_HiveChange + "?client=www" + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_DelHive(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("API_DelHive", Config.BASEURL + Config.API_DelHive + "&token=" + token);
        GetJsonAPI.getQueries(Config.BASEURL + Config.API_DelHive + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }

    public static void API_ChangeAliasHive(CallBackDone callBackDone, Object jsonObject, String token) {
        Log.v("API_ChangeAliasHive", Config.BASEURL + Config.API_ChangeAliasHive + "&token=" + token);
        ChangeAliasHiveRequest delHive = (ChangeAliasHiveRequest) jsonObject;
        String method = Config.API_ChangeAliasHive.substring(0, 16) + delHive.getId() + Config.API_ChangeAliasHive.substring(16, Config.API_ChangeAliasHive.length());
        GetJsonAPI.getQueries(Config.BASEURL + method + "&token=" + token, API_Method.POST, jsonObject, callBackDone);
    }
}

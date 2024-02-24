package ca.xahive.app.bl.objects;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.EncryptableString;
import ca.xahive.app.bl.utils.Helpers;


public class CurrentUser extends BaseModelObject {

    public int id;
    public String alias;
    public String token;
    public String email;
    public String password;
    public String encryptedPassword;
    public boolean isMessageAllowed;
    public boolean isAnonMessageAllowed;
    public boolean isNonContactMessageAllowed;
    public  boolean isXahiveAdmin;
    public String avatar_url;
    public boolean isAnonymous;
    public boolean isCurrentUser;
    public int getUserId() {
        return id;
    }

    public void setUserId(int userId) {
        this.id = userId;
    }
   public String getAvatar()
   {
       if(avatar_url.equals(null))
           return  "";
       return  avatar_url;
   }
    public String getAlias() {
        if (this.isAnonymous()){
            return "Anonymous User";
        }
        else {
            return alias;
        }
    }

    public void setAlias(String alias) {
        this.alias = alias;
        update();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        update();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        setEncryptedPassword(null);
        update();
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public boolean isMessageAllowed() {
        return isMessageAllowed;
    }

    public void setMessageAllowed(boolean isMessageAllowed) {
        this.isMessageAllowed = isMessageAllowed;
        update();
    }

    public boolean isAnonMessageAllowed() {
        return isAnonMessageAllowed;
    }

    public void setAnonMessageAllowed(boolean isAnonMessageAllowed) {
        this.isAnonMessageAllowed = isAnonMessageAllowed;
        update();
    }

    public boolean isNonContactMessageAllowed() {
        return isNonContactMessageAllowed;
    }

    public void setNonContactMessageAllowed(boolean isNonContactMessageAllowed) {
        this.isNonContactMessageAllowed = isNonContactMessageAllowed;
        update();
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public String getAvatarKey() {
        return String.format("avatar_%d", this.getUserId());
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setIsCurrentUser(boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
    }

    // Helpers
    public boolean isAuthenticated() {
        return Helpers.stringIsNotNullAndMeetsMinLength(getToken(), 1);
    }

    public String getEncryptedPassword() {
        if (encryptedPassword == null && getPassword() != null) {
            EncryptableString encStr = new EncryptableString();
            encStr.setOriginalString(getPassword());
            byte[] preparedData = encStr.getPreparedData();
            byte[] outData = Crypto.encryptData(Config.PASSWORD_CRYPTO_KEY, preparedData);

            encryptedPassword = Helpers.b64EncodedString(outData);
        }

        return encryptedPassword;
    }

    private void update() {
        if (isCurrentUser()) {
            Model.getInstance().updateLoginUser();
        }
    }

    @Override
    public String getKeyPath() {
        return "user";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = null;

        try {
            jsObj = (JSONObject) jsonObject;
        }
        catch (ClassCastException e) {
            JSONArray jsonArray = (JSONArray) jsonObject;
            jsObj = jsonArray.optJSONObject(0);
        }

        if (jsObj != null) {
            setUserId(jsObj.optInt("id", 0));
            setAlias(jsObj.optString("alias", null));
            setToken(jsObj.optString("token", null));
            setEmail(jsObj.optString("email", null));
            setAnonymous(jsObj.optBoolean("isAnonymous", false));
            setMessageAllowed(jsObj.optBoolean("isMessageAllowed", false));
            setAnonMessageAllowed(jsObj.optBoolean("isAnonMessageAllowed", false));
            setNonContactMessageAllowed(jsObj.optBoolean("isNonContactMessageAllowed", false));
        }

        setClean(true);

    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();

            jsObj.put("id", getUserId());
            jsObj.put("alias", getAlias());
            jsObj.put("token", getToken());
            jsObj.put("email", getEmail());
            jsObj.put("encryptedPassword", getEncryptedPassword());
            jsObj.put("isAnonymous", isAnonymous());
            jsObj.put("isMessageAllowed", isMessageAllowed());
            jsObj.put("isAnonMessageAllowed", isAnonMessageAllowed());
            jsObj.put("isNonContactMessageAllowed", isNonContactMessageAllowed());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "Current user with ID: " + this.id + " email: " + this.email + " pass: " + this.password;
    }

    //helper methods
    public static CurrentUser userForLoginWithEmail(String email, String password){
        CurrentUser user = new CurrentUser();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

}

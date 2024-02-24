package ca.xahive.app.bl.objects.api_object;

/**
 * Created by trantung on 10/15/15.
 */
public class UserUpdateRequest {
    public String id;
    public String email;
    public String isAnonymous;
    public String encryptedPassword;
    public String isAnonMessageAllowed;
    public String isNonContactMessageAllowed;
    public String avatar_url;
    public String alias;
    public String isMessageAllowed;
}
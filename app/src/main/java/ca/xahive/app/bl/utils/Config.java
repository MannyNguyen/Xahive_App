package ca.xahive.app.bl.utils;

import com.amazonaws.regions.Regions;

public class Config {

    // PROD! //

    public static final boolean DEBUG = false;
    public static final boolean IS_IN_MOSCOW = DEBUG;

    // Must have trailing "/"
    public static final String BASEURL = "http://testapi.xahive.com/";

    public static final String API_LOGIN = "login?client=www";

    public static final String API_SIGNUP = "register?client=www";

    public static final String API_UpdateSetting = "updateLoginUser?client=www";

    public static final String API_AddContact = "relatedUsers?client=www";
    public static final String API_LOGIN_ANON= "anonymousLogin?client=www";

    public static final String API_UpdatePassword = "users?client=www";

    public static final String API_GET_HiveList = "userHivesList";

    public static final String API_GET_ConversationItem = "privateConversation";

    public static final String API_CreateHive = "createHive?client=www";

    public static final String API_HiveChange = "/hiveChange";

    public static final String API_DelHive="deleteHive?client=www";

    public static final String API_Attachment="attachment";

    public static final String API_ChangeAliasHive = "updateHive?hive=&client=www";

    public static final String API_GET_MessageList = "privateConversationList";

    public static final String API_GET_UserPublicKey = "publicKey";
    public static final String API_GET_RelationShip = "relatedUsers";


    public static final String API_POST_Private_keys = "private_key_requests?client=www";
    public static final String API_GET_Private_keys = "private_keys";

    public static final String CLIENTID = "android";

    public static final String PASSWORD_CRYPTO_KEY = "QsatGr55q9FuLDL";

    public static final int DEFAULT_HIVE_ID = 1;

    public static final int LOCATION_POLL_FREQUENCY = 15 * 1000; // milliseconds
    public static final float LOCATION_MIN_CHANGE = 10.0f; // metres

    public static final int MINALIASLENGTH = 1;
    public static final int MINPASSWDLENGTH = 6;

    public static final String TERMS_AND_CONDITIONS_URL = "https://www.xahive.com/terms";
    public static final String HELP_URL = "http://www.xahive.com/help";

    /* AWS & S3 Config */
    public static final String AWS_ACCESS_KEY_ID = "AKIAJ33FQ35AVKMTUZLQ";
    public static final String AWS_SECRET_KEY = "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";

    public static final String S3_FILE_BUCKET = "xahive-test";

    //public static final String S3_FILE_BUCKET = "xahive_prod";
    public static final String ATTACHMENT_FILE_NAME = "attachment_%d";

    public static final Regions S3_REGION = Regions.US_WEST_2;
}

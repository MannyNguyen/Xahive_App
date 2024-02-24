package ca.xahive.app.bl.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelErrorMap {
    private static final Map<String, String> errorMap;
    static {
        Map<String, String> aMap = new HashMap<String, String>();

        aMap.put("INVALID_SIGNUP", "Invalid signup information. Please enter a valid email, an alias, and a password.");
        aMap.put("INVALID_LOGIN", "Invalid login information, please try again.");
        aMap.put("AUTH_ERROR", "Login failed. Please confirm your email address and password and try again.");
        aMap.put("NO_SUCH_USER", "The username and password combination was not found.");
        aMap.put("EMAIL_TAKEN", "The email address you have entered is already in use by another user.");
        aMap.put("CONNECT_ERROR", "There was an error connecting to the Xahive service.");
        aMap.put(ModelError.INVALID_REQUEST, "The application sent an invalid request to the Xahive service.");
        aMap.put(ModelError.INTERNAL_ERROR, "An internal error has occurred.");
        aMap.put(ModelError.UNSPECIFIED_ERROR, "An unspecified error has occurred.");
        aMap.put(ModelError.LOCATION_ERROR, "Location services must be enabled to use this feature. Please allow Xahive to use the location services on your device.");
        aMap.put(ModelError.SEND_MSG_ERROR, "Sending message failed.");

        errorMap = Collections.unmodifiableMap(aMap);
    }
    public static String get(String code) {
        String value = errorMap.get(code);

        if (value == null) {
            value = "An unknown error has occurred. Please try your request again later.";
        }

        return value;
    }
}

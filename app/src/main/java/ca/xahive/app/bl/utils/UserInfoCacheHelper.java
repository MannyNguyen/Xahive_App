package ca.xahive.app.bl.utils;

import java.util.Observer;

import ca.xahive.app.bl.local.Model;


public class UserInfoCacheHelper {
    public static void changeUserObservation(Observer observer, int oldUserId, int newUserId) {
        if (oldUserId > 0) {
            Model.getInstance().getUserInfoCache().removeObserverForUserId(observer, oldUserId);
        }

        if (newUserId > 0) {
            Model.getInstance().getUserInfoCache().addObserverForUserId(observer, newUserId);
        }
    }
}

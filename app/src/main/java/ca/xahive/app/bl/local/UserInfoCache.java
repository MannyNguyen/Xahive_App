package ca.xahive.app.bl.local;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.utils.XADebug;


public class UserInfoCache extends Observable {

    private SparseArray<User> userInfoMap;
   // private SparseArray<UserInfoRequest> requestMap;
    private SparseArray<ArrayList<Observer>> observers;

    private SparseArray<User> getUserInfoMap() {
        if (userInfoMap == null) {
            userInfoMap = new SparseArray<User>();
        }
        return userInfoMap;
    }
   /**
    private SparseArray<UserInfoRequest> getRequestMap() {
        if (requestMap == null) {
            requestMap = new SparseArray<UserInfoRequest>();
        }
        return requestMap;
    }
**/
    private SparseArray<ArrayList<Observer>> getObservers() {
        if (observers == null) {
            observers = new SparseArray<ArrayList<Observer>>();
        }

        return observers;
    }

    private ArrayList<Observer> getObserversForUserId(int userId) {
        ArrayList<Observer> observersForUserId = getObservers().get(userId);

        if (observersForUserId == null) {
            observersForUserId = new ArrayList<Observer>();
            getObservers().put(userId, observersForUserId);
        }

        return observersForUserId;
    }

    private void fetchUserInfoForId(final int userId) {
        // Avoid dupe requests.
     //   if (getRequestMap().get(userId) != null) {
            return;
      //  }

         /**
        final UserInfoRequest userInfoRequest = new UserInfoRequest(userId);
        userInfoRequest.setCallback(new APICallback() {
            @Override
            public void onSuccess(ModelObject modelObject) {
                super.onSuccess(modelObject);
                onUserInfoReceived((User)modelObject, userId);
            }

            @Override
            public void onFail(ModelError error) {
                super.onFail(error);
                XADebug.d(String.format("Failed to retrieve user info for: %d", userId));
            }

            @Override
            public void onComplete() {
                super.onComplete();
                getRequestMap().remove(userId);
            }
        });

        getRequestMap().put(userId, userInfoRequest);

        userInfoRequest.grab();**/
    }

    private void onUserInfoReceived(User userInfo, int userId) {
        getUserInfoMap().put(userId, userInfo);
        notifyObserversForUserId(userId);
    }

    private void notifyObserversForUserId(int userId) {
        ArrayList<Observer> observers = getObserversForUserId(userId);

        for (Observer observer : observers) {
            observer.update(this, getUserInfoMap().get(userId));
        }
    }

    /* Public */

    public void addObserverForUserId(Observer observer, int userId) {
        ArrayList<Observer> observers = getObserversForUserId(userId);

        observers.add(observer);
    }

    public void removeObserverForUserId(Observer observer, int userId) {
        getObserversForUserId(userId).remove(observer);
    }

    public User userWithId(int userId) {
        if (userId == 0) {
            return new User(0); // Skip API
        }

        User existingUser = getUserInfoMap().get(userId);

        if (existingUser == null) {
            fetchUserInfoForId(userId);
            existingUser = new User(userId);
        }
        else if (existingUser.isOld()) {
            fetchUserInfoForId(userId);
        }

        return existingUser;
    }

}

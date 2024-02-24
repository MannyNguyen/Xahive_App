package ca.xahive.app.bl.local;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.bl.api.QueriesConstants;
import ca.xahive.app.bl.api.callback.APIConnection;
import ca.xahive.app.bl.api.queries.APIConnectionRequest;

import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.CurrentUser;
import ca.xahive.app.bl.objects.Device;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.objects.api_object.AddContactRequest;
import ca.xahive.app.bl.objects.api_object.ChangeAliasHiveRequest;
import ca.xahive.app.bl.objects.api_object.CreateHiveRequest;
import ca.xahive.app.bl.objects.api_object.DeleteHiveRequest;
import ca.xahive.app.bl.objects.api_object.HiveChangeRequest;
import ca.xahive.app.bl.objects.api_object.LoginRequest;
import ca.xahive.app.bl.objects.api_object.LoginRequestAnon;
import ca.xahive.app.bl.objects.api_object.LoginResponse;
import ca.xahive.app.bl.objects.api_object.PasswordUpdateRequest;
import ca.xahive.app.bl.objects.api_object.PublicKeyRequest;
import ca.xahive.app.bl.objects.api_object.RelationshipRequest;
import ca.xahive.app.bl.objects.api_object.RelationshipResponse;
import ca.xahive.app.bl.objects.api_object.UpdateSettingRequest;
import ca.xahive.app.bl.objects.api_object.UpdateSettingResponse;
import ca.xahive.app.bl.objects.api_object.UserLoginRequest;
import ca.xahive.app.bl.objects.api_object.UserLoginRequestAnon;
import ca.xahive.app.bl.objects.api_object.UserRelationShipsRequest;
import ca.xahive.app.bl.objects.api_object.UserUpdateRequest;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.webservice.CallBackDone;

/**
 * Created by trantung on 10/5/15.
 */
public class Model extends Observable implements Observer {
    private static Model _instance;
    private Context ctx;
    private String deviceID = "";
    public boolean isJustSignUp = false;
    public boolean isLoginAnon = false;
    private XahLocationListener locationListener;

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    private TransferManager s3Manager;
    /* Data */
    private UserRelationshipsModelItem userRelationships;
    private CurrentUser currentUser;

    private SparseArray<ConversationModelItem> conversationModelItems;
    private PasswordMemoryCache passwordCache;
    private int hiveId;
    private ModelItem userHivesList;
    private ModelItem conversationList;
    private DecryptedMessageMemoryCache decryptedMessageMemoryCache;

    private Date lastSeenConversationDate;
    private HiveSettingsModelItem hiveSettings;
    private UserInfoCache userInfoCache;
    /* Helpers */

    private boolean hasUser;
    private Parcelator parcelator;
    private boolean reachable;
    private ModelError loginError;
    private Bundle shareBundle;

    public boolean hasUser() {
        return (hasUser && currentUser != null);
    }

    private Model() { /* private to prevent external use */ }

    public static Model getInstance() {
        if (_instance == null) {
            _instance = new Model();
        }
        return _instance;
    }

    private SparseArray<ConversationModelItem> getConversationModelItems() {
        if (conversationModelItems == null) {
            conversationModelItems = new SparseArray<ConversationModelItem>();
        }
        return conversationModelItems;
    }


    public ConversationModelItem getConversationModelForConversation(Conversation convo) {
        if (convo.getConversationId() == 0) {
            ConversationModelItem conversationModel = new ConversationModelItem();
            conversationModel.setConversation(convo);

            return conversationModel;
        }

        SparseArray<ConversationModelItem> conversationModelItems = getConversationModelItems();
        ConversationModelItem convoModel = conversationModelItems.get(convo.getConversationId());

        if (convoModel == null) {
            convoModel = new ConversationModelItem();
            convoModel.setConversation(convo);
            conversationModelItems.put(convo.getConversationId(), convoModel);
        }

        return convoModel;
    }

    public void reportMessage(Message message) {
        //reportMessageRequest = new ReportMessageRequest(message);
        //reportMessageRequest.grab();
        reportMessageCallAPI(message);
    }

    private void reportMessageCallAPI(Message message) {

    }

    /**
     * private UserHivesListRequest getUserHivesListRequest() {
     * if (userHivesListRequest == null) {
     * userHivesListRequest = new UserHivesListRequest();
     * userHivesListRequest.setCallback(new APICallback() {
     *
     * @Override public void onStart() {
     * super.onStart();
     * getUserHivesList().setPendingIfError();
     * }
     * @Override public void onSuccess(ModelObject modelObject) {
     * super.onSuccess(modelObject);
     * getUserHivesList().setData(modelObject);
     * }
     * @Override public void onFail(ModelError error) {
     * super.onFail(error);
     * getUserHivesList().setError(error);
     * }
     * });
     * }
     * return userHivesListRequest;
     * }
     **/
    private void getUserHiveListAPI() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    //Log.v("testlog", "getUserHiveListAPI:= " + result.toString());
                    Gson gson = new Gson();
                    HivesList response = gson.fromJson(result.toString(), HivesList.class);

                    getUserHivesList().setData((ModelObject) response);
                    notifyObserversOfEvent(ModelEvent.CONTACT_UPDATE);
                }
            }

            @Override
            public void onStart() {
                getUserHivesList().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                getUserHivesList().setData(modelObject);

            }

            @Override
            public void onFail(ModelError error) {

                getUserHivesList().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", currentUser.getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetHiveList(callBackDone, params);
    }

    public ModelItem getUserHivesList() {
        if (userHivesList == null) {
            userHivesList = new ModelItem();
            reloadUserHivesList();
        }
        return userHivesList;
    }

    public void reloadUserHivesList() {
        if (hasUser()) {
            /**
             * GET API
             */
            getUserHiveListAPI();
        }
    }

    public void logoutCurrentUser() {
        //unregister the device that was previously registered for notifications
        /**
         if(getDeviceId() != null) {
         getRegisterDeviceRequest().post(Device.deviceForUserIdWithDeviceId(getCurrentUser().getUserId(),
         getDeviceId(),
         "android",
         true));
         }**/
        if (Model.getInstance().isLoginAnon) {
            UserDefaults.setPassword(null);
            UserDefaults.setUsername(null);
        }

        UserDefaults.setSavedHiveId(0);
        hasUser = false;
        currentUser = null;
        hiveId = 0;
        hiveSettings = null;
        //buzzListItem = null;
        //honeycombList = null;
        conversationList = null;
        userRelationships = null;
        //conversationModelItems = null;
        userInfoCache = null;
        decryptedMessageMemoryCache = null;
        //honeycombSaveWorker = null;

        //getLogoutRequest().grab();
    }

    public void setShareBundle(Bundle shareBundle) {

        this.shareBundle = shareBundle;
    }

    public void updateLoginUser() {
        getLoginUserUpdateCallAPI();
    }


    private void getLoginUserUpdateCallAPI() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    //Log.v("Testlog", String .valueOf(result));
                    UpdateSettingResponse response = gson.fromJson(result.toString(), UpdateSettingResponse.class);

                    if (response != null) {
                        if (response.user.size() > 0) {

                            getCurrentUser().setAlias(response.user.get(0).alias);
                            getCurrentUser().setEmail(response.user.get(0).email);
                        }
                    }
                    notifyObserversOfEvent(ModelEvent.USER_CHANGED);
                    update(getCurrentUser(), "USER_CHANGED");
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                CurrentUser user = (CurrentUser) modelObject;
                setCurrentUser(user);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());

                setLoginError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        UpdateSettingRequest updateSettingRequest = new UpdateSettingRequest();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.id = String.valueOf(currentUser.getUserId());
        userUpdateRequest.email = currentUser.getEmail();
        // userUpdateRequest.encryptedPassword = currentUser.getEncryptedPassword();
        userUpdateRequest.isAnonMessageAllowed = "0";
        userUpdateRequest.isAnonymous = "0";
        userUpdateRequest.avatar_url = "";
        userUpdateRequest.isNonContactMessageAllowed = String.valueOf(currentUser.isNonContactMessageAllowed());
        userUpdateRequest.alias = currentUser.getAlias();
        userUpdateRequest.isMessageAllowed = String.valueOf(currentUser.isMessageAllowed());
        updateSettingRequest.user = userUpdateRequest;
        APIConnectionRequest.API_UpdateSetting(callBackDone, updateSettingRequest, currentUser.getToken());
    }

    public void requestReturned(ModelError error) {
        setReachable(!(error != null && error.isReachabilityError()));
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public Bundle getShareBundle() {
        return shareBundle;
    }

    public void authenticateSavedUser() {
        hasUser = false;
        CurrentUser user = CurrentUser.userForLoginWithEmail(UserDefaults.getUsername(), UserDefaults.getPassword());
        authenticateUser(user);
    }

    private void setCurrentUser(CurrentUser currentUser) {

        /**
         XADebug.d("Setting current user: " + currentUser.toString());
         hasUser = true;
         this.currentUser.setUserId(currentUser.getUserId());
         this.currentUser.setAlias(currentUser.getAlias());
         this.currentUser.setEmail(currentUser.getEmail());
         this.currentUser.setAnonymous(currentUser.isAnonymous);
         this.currentUser.setMessageAllowed(currentUser.isMessageAllowed);
         this.currentUser.setAnonMessageAllowed(currentUser.isAnonMessageAllowed);
         this.currentUser.setNonContactMessageAllowed(currentUser.isNonContactMessageAllowed);
         this.currentUser.avatar_url = currentUser.avatar_url;
         this.currentUser.isXahiveAdmin = currentUser.isXahiveAdmin;
         this.currentUser.setToken(currentUser.getToken());**/
        XADebug.d("Setting current user: " + currentUser.toString());
        hasUser = true;
        getCurrentUser().setUserId(currentUser.getUserId());
        getCurrentUser().setAlias(currentUser.getAlias());
        getCurrentUser().setEmail(currentUser.getEmail());
        getCurrentUser().setAnonymous(currentUser.isAnonymous);
        getCurrentUser().setMessageAllowed(currentUser.isMessageAllowed);
        getCurrentUser().setAnonMessageAllowed(currentUser.isAnonMessageAllowed);
        getCurrentUser().setNonContactMessageAllowed(currentUser.isNonContactMessageAllowed);
        getCurrentUser().avatar_url = currentUser.avatar_url;
        getCurrentUser().isXahiveAdmin = currentUser.isXahiveAdmin;
        getCurrentUser().setToken(currentUser.getToken());


    }

    private void callAPILoginAnon() {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    LoginResponse response = gson.fromJson(result.toString(), LoginResponse.class);
                    Log.i("response.user", "" + response.user);
                    setCurrentUser(response.user);
                    if (UserDefaults.getUsername() != null) {
                        if (!UserDefaults.getUsername().equals(response.user.getEmail())) {
                            setCacheWithNewUser();
                        }
                    } else {
                        setCacheWithNewUser();
                    }
                    UserDefaults.setUsername(null);
                    UserDefaults.setPassword(null);
                    UserDefaults.setUserId(0);
                    notifyObserversOfEvent(ModelEvent.USER_CHANGED);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                CurrentUser user = (CurrentUser) modelObject;
                setCurrentUser(user);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());

                setLoginError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        LoginRequestAnon loginRequest = new LoginRequestAnon();
        UserLoginRequestAnon userLoginRequest = new UserLoginRequestAnon();
        userLoginRequest.isNonContactMessageAllowed = "0";
        userLoginRequest.isMessageAllowed = "0";
        userLoginRequest.isAnonMessageAllowed = "0";
        loginRequest.user = userLoginRequest;
        APIConnectionRequest.API_Login_Anon(callBackDone, loginRequest);
    }

    private void callAPILogin(final CurrentUser user) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    LoginResponse response = gson.fromJson(result.toString(), LoginResponse.class);
                    setCurrentUser(response.user);
                    if (UserDefaults.getUsername() != null) {
                        if (Model.getInstance().isJustSignUp) {
                            setCacheWithNewUser();
                        }
                    } else {
                        if (Model.getInstance().isJustSignUp) {
                            setCacheWithNewUser();
                        }
                    }
                    UserDefaults.setUsername(user.getAlias());
                    UserDefaults.setPassword(user.getPassword());
                    UserDefaults.setUserId(user.getUserId());
                    notifyObserversOfEvent(ModelEvent.USER_CHANGED);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                CurrentUser user = (CurrentUser) modelObject;
                setCurrentUser(user);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());

                setLoginError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        LoginRequest loginRequest = new LoginRequest();
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.email = user.getEmail();
        userLoginRequest.encryptedPassword = user.getEncryptedPassword();
        userLoginRequest.isAnonMessageAllowed = "0";
        userLoginRequest.isAnonymous = "0";
        loginRequest.user = userLoginRequest;
        APIConnectionRequest.API_Login(callBackDone, loginRequest);

    }

    public void setCacheWithNewUser() {
        try {
            UserDefaults.generateKeys();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        try {
            UserDefaults.generateKeys(Model.getInstance().getCurrentUser().getUserId(), "1");
            Model.getInstance().updateUserPublicKey();

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public UserRelationshipsModelItem getUserRelationships() {
        if (userRelationships == null) {
            userRelationships = new UserRelationshipsModelItem();
        }
        return userRelationships;
    }


    public int getHiveId() {
        if (hiveId == 0) {
            int savedHive = UserDefaults.getSavedHiveId();

            if (savedHive != 0) {
                hiveId = savedHive;
            } else {
                hiveId = Config.DEFAULT_HIVE_ID;
            }
        }

        return hiveId;
    }

    public void setHiveId(int hiveId) {
        this.hiveId = hiveId;

        UserDefaults.setSavedHiveId(hiveId);

        //honeycombList = null;
        //buzzListItem = null;

        //this.getHiveBasedSettingsRequest().grab();
    }

    public void reloadUserRelationships() {

        if (hasUser()) {
            getUserRelationshipsRequestAPI();
        }
    }

    public void reloadConversationList() {
        if (hasUser()) {
            getMessageListAPI();
        }

    }

    private void getMessageListAPI() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    ConversationList conversationList = gson.fromJson(result.toString(), ConversationList.class);
                    getConversationList().setData((ModelObject) conversationList);
                    updateLastSeenConversationDate();
                    notifyObserversOfEvent(ModelEvent.MESSAGE_UPDATE);

                }
            }

            @Override
            public void onStart() {
                getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                getConversationList().setData(modelObject);
                updateLastSeenConversationDate();
            }

            @Override
            public void onFail(ModelError error) {
                //super.onFail(error);
                getConversationList().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", currentUser.getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetMessageList(callBackDone, params, currentUser.getToken());

    }

    public void updateLastSeenConversationDate() {
        Date lastSeenDate = new Date();

        ConversationList conversationListObject = (ConversationList) getConversationList().getData();
        ArrayList<Conversation> conversationArrayList = conversationListObject.getConversations();

        if (conversationArrayList.size() > 0) {
            lastSeenDate = conversationArrayList.get(0).getLatestMessageDate(); //the first conversation in the arraylist is the latest
        } else {
            if (lastSeenConversationDate != null) {
                Long timestampFromDate = Helpers.timestampFromDate(lastSeenConversationDate);

                if (timestampFromDate > 86400L) {
                    timestampFromDate = timestampFromDate - 86400L;
                }

                lastSeenDate.setTime(timestampFromDate);
            }
        }

        setLastSeenConversationDate(lastSeenDate);
    }

    public Date getLastSeenConversationDate() {
        if (lastSeenConversationDate != null) {
            return lastSeenConversationDate;
        } else {
            lastSeenConversationDate = new Date();
            Long oneDayAgo = Helpers.timestampFromDate(lastSeenConversationDate) - 86400000;
            lastSeenConversationDate.setTime(oneDayAgo);
            return lastSeenConversationDate;
        }
    }

    public void setLastSeenConversationDate(Date lastSeenConversationDate) {
        this.lastSeenConversationDate = lastSeenConversationDate;
    }

    public ModelItem getConversationList() {
        if (conversationList == null) {
            conversationList = new ModelItem();
        }
        return conversationList;
    }

    private void getUserRelationshipsRequestAPI() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    RelationshipResponse response = gson.fromJson(result.toString(), RelationshipResponse.class);
                    // Log.v("testlog", "result1" + String.valueOf(response.relatedUser.size()));
                    UserRelationshipList list = new UserRelationshipList();
                    list.setUserRelationships(response.relatedUser);
                    getUserRelationships().setData((ModelObject) list);

                    notifyObserversOfEvent(ModelEvent.CONTACT_UPDATE);
                }
            }

            @Override
            public void onStart() {
                getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                getUserRelationships().setData(modelObject);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                getUserRelationships().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        /**
         RelationshipRequest loginRequest = new RelationshipRequest();
         UserRelationShipsRequest userRequest = new UserRelationShipsRequest();
         userRequest.email = currentUser.getEmail();
         userRequest.encryptedPassword = currentUser.getEncryptedPassword();
         userRequest.isAnonMessageAllowed = String.valueOf(currentUser.isAnonMessageAllowed());
         userRequest.isAnonymous =String.valueOf(currentUser.isAnonymous());
         userRequest.alias = currentUser.getAlias();
         loginRequest.user = userRequest;**/
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", currentUser.getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetRelationship(callBackDone, params, currentUser.getToken());

    }

    public void updateUserPublicKey() throws InvalidKeySpecException {
        // if(UserDefaults.getPublicKeyAsString()==null)
        // {
        if (UserDefaults.mContext != null)
            UserDefaults.generateKeys();

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Log.v("updateUserPublicKey", result.toString());
                    notifyHiveChange("1");
                    if (result.has("added")) {

                    }
                }
            }

            @Override
            public void onStart() {
                getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                getUserRelationships().setData(modelObject);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                getUserRelationships().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        Log.v("deviceID", deviceID);
        // List<NameValuePair> params = new ArrayList<>();
        PublicKeyRequest pubicKeyRequest = new PublicKeyRequest();
        pubicKeyRequest.publicKey = UserDefaults.getPublicKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1");
        pubicKeyRequest.deviceId = deviceID;
        pubicKeyRequest.userId = getCurrentUser().getUserId();
        pubicKeyRequest.hiveId = 1;

        /**
         params.add(new BasicNameValuePair("publicKey", UserDefaults.getPublicKeyAsString()));
         // UserDefaults.getPublicKeyAsString()
         params.add(new BasicNameValuePair("deviceId", deviceID));
         params.add(new BasicNameValuePair("userId",String.valueOf(getCurrentUser().getUserId())));
         params.add(new BasicNameValuePair("hiveId", "1"));**/
        String urlString = String.format("publicKey/%d",
                getCurrentUser().getUserId());
        APIConnectionRequest.API_UpdateUserPubicKey(callBackDone, urlString, pubicKeyRequest, currentUser.getToken());

        //  }
    }


    private void sendKeyToServer() {

    }

    private void notifyHiveChange(String hiveID) {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Log.v("notifyHiveChange", result.toString());

                }
            }

            @Override
            public void onStart() {
                getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                getUserRelationships().setData(modelObject);
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {

                getUserRelationships().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        HiveChangeRequest hiveChangeRequest = new HiveChangeRequest();
        hiveChangeRequest.setDeviceId(deviceID);
        hiveChangeRequest.setUserId(String.valueOf(getCurrentUser().getUserId()));
        hiveChangeRequest.setHiveId(hiveID);
        APIConnectionRequest.API_HiveChange(callBackDone, hiveChangeRequest, currentUser.getToken());

    }

    public void createHive(CreateHiveRequest hiveRequest) {
        createHiveCallAPI(hiveRequest);
    }

    private void createHiveCallAPI(CreateHiveRequest hiveRequest) {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    reloadUserHivesList();
                    notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                getUserHivesList();

                notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
            }

            @Override
            public void onFail(ModelError error) {
                setUpdateContactError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        APIConnectionRequest.API_AddHive(callBackDone, hiveRequest, currentUser.getToken());
    }

    public void updateUserRelationship(UserRelationship relationship) {
        // getUserRelationshipUpdateRequest().post(relationship);
        addContactCallAPI(relationship);
    }

    public void addContactCallAPI(UserRelationship relationship) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    UserRelationship response = gson.fromJson(result.toString(), UserRelationship.class);
                    getUserRelationships().addContact(response);
                    notifyObserversOfEvent(ModelEvent.ADD_CONTACT_UPDATE);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                getUserRelationships().addContact((UserRelationship) modelObject);
            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                setUpdateContactError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        AddContactRequest contactRequest = new AddContactRequest();
        contactRequest.relatedUser = relationship;
        APIConnectionRequest.API_AddContact(callBackDone, contactRequest, currentUser.getToken());
    }

    public void deleteHive(DeleteHiveRequest delHive) {
        deleteHiveCallAPI(delHive);
    }

    public void deleteHiveCallAPI(DeleteHiveRequest delHive) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    reloadUserHivesList();
                    notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                getUserHivesList();

                notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                setUpdateContactError(error);
            }

            @Override
            public void onComplete() {

            }
        });

        APIConnectionRequest.API_DelHive(callBackDone, delHive, currentUser.getToken());
    }

    public void changeAliasHive(ChangeAliasHiveRequest aliasHive) {
        changAliaseHiveCallAPI(aliasHive);
    }

    public void changAliaseHiveCallAPI(ChangeAliasHiveRequest aliasHive) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();
                    reloadUserHivesList();
                    notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                getUserHivesList();

                notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                setUpdateContactError(error);
            }

            @Override
            public void onComplete() {

            }
        });

        APIConnectionRequest.API_ChangeAliasHive(callBackDone, aliasHive, currentUser.getToken());
    }

    public TransferManager getS3Manager() {

        if (s3Manager == null) {

            BasicAWSCredentials credentials = new BasicAWSCredentials(Config.AWS_ACCESS_KEY_ID, Config.AWS_SECRET_KEY);

            AmazonS3Client s3 = new AmazonS3Client(credentials);
            s3.setRegion(Region.getRegion(Config.S3_REGION));

            s3Manager = new TransferManager(s3);

        }

        return s3Manager;
    }


    public void authenticateUser(CurrentUser user) {
        hasUser = false;

        setLoginError(null);

        callAPILogin(user);
        // getLoginRequest().post(user);
    }

    public void authenticateAnonymousUser() {
        hasUser = false;

        UserDefaults.setUsername(null);
        UserDefaults.setPassword(null);

        setLoginError(null);
        callAPILoginAnon();
        //getAnonymousLoginRequest().post(new CurrentUser());

    }

    public void notifyObserversOfEvent(ModelEvent evt) {
        setChanged();
        XADebug.d("Model notifying observers: " + evt);
        notifyObservers(evt);
    }


    public void setLoginError(ModelError loginError) {
        this.loginError = loginError;

        if (loginError != null) {
            UserDefaults.setPassword(null);
            XADebug.d("LOGIN FAILED");
            notifyObserversOfEvent(ModelEvent.LOGIN_FAILED);
        }
    }

    public void setUpdateContactError(ModelError loginError) {
        XADebug.d("CONTACT_UPDATE_FAILED");
        notifyObserversOfEvent(ModelEvent.CONTACT_UPDATE_FAILED);

    }

    public CurrentUser getCurrentUser() {
        if (currentUser == null) {
            currentUser = new CurrentUser();
        }
        return currentUser;
    }

    public HiveSettingsModelItem getHiveSettings() {
        if (hiveSettings == null) {
            hiveSettings = new HiveSettingsModelItem();
        }
        return hiveSettings;
    }

    public UserInfoCache getUserInfoCache() {
        if (userInfoCache == null) {
            userInfoCache = new UserInfoCache();
        }
        return userInfoCache;
    }

    /**
     * public void updateHiveSettings() {
     * getHiveBasedSettingsAPI();
     * // getHiveBasedSettingsRequest().post(getHiveSettings().getHiveSettingsData());
     * }
     * private void getHiveBasedSettingsAPI() {
     * <p/>
     * }
     **/
    public DecryptedMessageCacheInterface getDecryptedMessageStore() {

        DecryptedMessageCacheInterface store;

        if (UserDefaults.getDecryptSave()) {
            store = LocalStorage.getInstance();
        } else {
            store = getDecryptedMessageMemoryCache();
        }

        return store;
    }

    public ModelError getLoginError() {
        return loginError;
    }

    private DecryptedMessageMemoryCache getDecryptedMessageMemoryCache() {
        if (decryptedMessageMemoryCache == null) {
            decryptedMessageMemoryCache = new DecryptedMessageMemoryCache();
        }
        return decryptedMessageMemoryCache;
    }

    public PasswordMemoryCache getPasswordCache() {
        if (passwordCache == null) {
            passwordCache = new PasswordMemoryCache();
        }
        return passwordCache;
    }

    public static void startLocationManager(Context ctx) {
        Model model = getInstance();
        model.setCtx(ctx);

        LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Config.LOCATION_POLL_FREQUENCY,
                Config.LOCATION_MIN_CHANGE,
                model.getLocationListener()
        );

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    Config.LOCATION_POLL_FREQUENCY,
                    Config.LOCATION_MIN_CHANGE,
                    model.getLocationListener()
            );
        }
    }

    public XahLocationListener getLocationListener() {
        synchronized (this) {
            if (locationListener == null) {
                locationListener = new XahLocationListener();
                locationListener.addObserver(this);
            }
            return locationListener;
        }
    }

    private void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    public Parcelator getParcelator() {
        if (parcelator == null) {
            parcelator = new Parcelator();
        }

        return parcelator;
    }

}

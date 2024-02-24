package ca.xahive.app.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.util.ASN1Dump;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import IAPUtils.IabHelper;
import IAPUtils.IabResult;
import IAPUtils.Inventory;
import IAPUtils.Purchase;
import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.local.HivesList;
import ca.xahive.app.bl.local.TabBarManager;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.api_object.KeyRequest;
import ca.xahive.app.bl.objects.api_object.PublicKeyOfUserResponse;
import ca.xahive.app.bl.utils.CryptoHandler;
import ca.xahive.app.bl.utils.EncryptableString;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.RSAPublicKeyFromOpenSSL_PKCS1_PEM;
import ca.xahive.app.bl.utils.SimpleCrypto;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.gcm.GcmIntentService;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.ui.cells.MessageCellAttachEncryptionListener;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.fragments.AccountSettingsFragment;
import ca.xahive.app.ui.fragments.ContactListMainFragment;
import ca.xahive.app.ui.fragments.HiveListMainFragment;
import ca.xahive.app.ui.fragments.MessagesChatNewFragment;
import ca.xahive.app.ui.fragments.MessagesFragment;
import ca.xahive.app.ui.fragments.MessagesListMainFragment;
import ca.xahive.app.webservice.CallBackDone;


public class TabBarManagerActivity extends BaseActivity implements OnTabChangeListener, MessageCellAttachEncryptionListener {
    private static Context context;
    static FragmentTabHost tabHost;
    public static final int TAB_HEIGHT = 70;
    private int lastSelectedTab = Tabs.BUZZ_TAB.getValue();

    private boolean isAuthSavedUser;
    protected ProgressDialog busyIndicator;
    private int selectedConversationUser = -1;

    private static final String IAB_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiYJmFkOHgasSx+6lKFabLPUSTDUhV4wTu0taA38K3JV7WY7kKqLB1njl/0t1A0+5KuhdcZDiAcdFGhrYXw7LyPZb02cCzzWw0KNlfZ5/SEboLdqNAk7aW1t9YHfu1ZJrMXSVVOwRcoBSzFqvZd5u83ZjGifOXr2KcfDRnrR3La4ImsEEsI7dlU6jyKCVGCyOIDu/k59HeCsKySLCTKFuxznHqInzaHw3HlTwCgpXIIfF2oxuQOCIrq1u8ZW1Jv1LGGP3b8/4ZMmbwFv/QW9l/AUJh5h1pogQK3Ai9/7ZDJ37ziGX9aLyn7wcL9/Tnbd4+q7Wn/4LuqzUnm13SLVfjwIDAQAB";
    private static final String DISABLE_ADS_ITEM_SKU = "ca.xahive.app.disable.ads";
    private static final int DISABLE_ADS_ITEM_REQUEST_CODE = 1;
    private static final String DISABLE_ADS_PURCHASE_TOKEN = "disableAdsPurchaseToken";
    private IabHelper iabHelper;

    protected ProgressDialog getBusyIndicator() {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(this, getString(R.string.loading));
        }
        return busyIndicator;
    }

    @Override
    public void attachButtonPressed(Message message) {

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MessagesListMainFragment) {
                ((MessagesListMainFragment) fragment).attachButtonPressed(message);
                return;
            }
        }
    }

    @Override
    public void lockButtonPressed(Message message) {

    }

    public enum Tabs {
        BUZZ_TAB(0),
        HONEYCOMBS_TAB(1),
        MESSAGES_TAB(2),
        SETTINGS_TAB(3);

        private final int id;

        Tabs(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = TabBarManagerActivity.this;

        Model.startLocationManager(this);
        Model.getInstance().addObserver(this);

        setContentView(R.layout.activity_tabbar_manager);
        if (!Model.getInstance().isJustSignUp && !Model.getInstance().isLoginAnon ){
            checkUserPrivateKey();
        }

        setupTabs();

        try {
            iabHelper = new IabHelper(this, IAB_LICENSE_KEY);

            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        XADebug.d("Setup in-app billing successfully.");
                    } else {
                        XADebug.d("Failed to set up in-app billing.");
                    }
                }
            });
        } catch (Exception e) {
            XADebug.d("Failed to set up in-app billing.");
        }

    }

    public void nagationToMessageWithUserID(final int idContact) {
        tabHost.setCurrentTab(0);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MessagesListMainFragment) {
                ((MessagesListMainFragment) fragment).nagationChatView(idContact);
                return;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        UserDefaults.setContext(getApplicationContext());
        if (getIntent().getExtras() != null) {
            String tabToOpen = getIntent().getStringExtra(GcmIntentService.TAB_EXTRA);

            if (tabToOpen != null) {
                if (tabToOpen.equals(GcmIntentService.SHOW_BUZZES_TAB)) {
                    tabHost.setCurrentTab(Tabs.BUZZ_TAB.getValue());
                } else if (tabToOpen.equals(GcmIntentService.SHOW_MESSAGES_TAB)) {
                    tabHost.setCurrentTab(Tabs.MESSAGES_TAB.getValue());
                } else {
                    tabHost.setCurrentTab(lastSelectedTab);
                }
            } else {
                tabHost.setCurrentTab(lastSelectedTab);
            }

            getIntent().removeExtra(GcmIntentService.TAB_EXTRA);
        } else {
            System.out.println("No extras were found on intent.");
        }
        reloadAPI();
    }

    public void reloadAPI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Model.getInstance().getCurrentUser().isAuthenticated()) {
                    handleAttachment();
                } else {
                    if (UserDefaults.hasSavedUser()) {
                        isAuthSavedUser = true;
                        getBusyIndicator().show();
                        Model.getInstance().authenticateSavedUser();
                    } else {
                        showLoginView();
                    }
                }
            }
        });
    }

    private void handleAttachment() {
        if (selectedConversationUser != -1) {
            // ChatStarter.startChatWithUser(this, selectedConversationUser);
            selectedConversationUser = -1;
        } else if (Model.getInstance().getCurrentUser().isAuthenticated() && Model.getInstance().getShareBundle() != null) {
            // From SO, mostly.
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(getString(R.string.share_attachment_context_menu_title));
            builder.setMessage(getString(R.string.share_attachment_message));
            builder.setNegativeButton(getString(R.string.option_share_attachment_buzz),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            onShareAttachmentForBuzz();
                        }
                    }
            );

            builder.setNeutralButton(getString(R.string.option_share_attachment_personal_message),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            onShareAttachmentForPersonal();
                        }
                    }
            );

            builder.setPositiveButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            onShareAttachmentCancel();
                        }
                    }
            );

            builder.create().show();
        }
    }

    private void showLoginView() {

        tabHost.setCurrentTab(Tabs.BUZZ_TAB.getValue());

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void setupTabs() {

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        tabHost.setOnTabChangedListener(this);
        Context tabHostContext = tabHost.getContext();

        //setup Buzz tab
        View buzzTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) buzzTabView.findViewById(R.id.tabLabel)).setText(R.string.buzz_title);
        FontHelper.getInstance(this).setCustomFont((TextView) buzzTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) buzzTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.icon_buzz_tab);

        TabSpec buzzSpec = tabHost.newTabSpec(getString(R.string.buzz_title)).setIndicator(buzzTabView);
        // tabHost.addTab(buzzSpec, BuzzTab.class, null);

        //setup Messages tab
        View messageTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) messageTabView.findViewById(R.id.tabLabel)).setText(R.string.messages_title);
        FontHelper.getInstance(this).setCustomFont((TextView) messageTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) messageTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_messages_tab);

        TabSpec messageSpec = tabHost.newTabSpec(getString(R.string.messages_title)).setIndicator(messageTabView);
        tabHost.addTab(messageSpec, MessagesListMainFragment.getInstance().getClass(), null);


        //setup Contact tab
        View contactTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) contactTabView.findViewById(R.id.tabLabel)).setText(R.string.contacts_title);
        FontHelper.getInstance(this).setCustomFont((TextView) contactTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) contactTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_contact_tab);

        TabSpec contactSpec = tabHost.newTabSpec(getString(R.string.contacts_title)).setIndicator(contactTabView);
        tabHost.addTab(contactSpec, ContactListMainFragment.getInstance().getClass(), null);

        //setup Hive tab
        View hiveTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) hiveTabView.findViewById(R.id.tabLabel)).setText(R.string.hive_titile);
        FontHelper.getInstance(this).setCustomFont((TextView) hiveTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) hiveTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_hive_tab);

        TabSpec hiveSpec = tabHost.newTabSpec(getString(R.string.hive_titile)).setIndicator(hiveTabView);
        tabHost.addTab(hiveSpec, HiveListMainFragment.getInstance().getClass(), null);

        //setup Settings tab
        View settingsTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) settingsTabView.findViewById(R.id.tabLabel)).setText(R.string.settings_title);
        FontHelper.getInstance(this).setCustomFont((TextView) settingsTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) settingsTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_settings_tab);

        TabSpec settingsSpec = tabHost.newTabSpec(getString(R.string.settings_title)).setIndicator(settingsTabView);
        tabHost.addTab(settingsSpec, AccountSettingsFragment.getInstance().getClass(), null);
        TabBarManagerActivity.updateTabHeight(TAB_HEIGHT);
    }

    @Override
    public void onTabChanged(String tabId) {
        lastSelectedTab = tabHost.getCurrentTab();
        Helpers.hideSoftKeyboardInActivity(this);
    }

    public static void updateTabHeight(int height) {
        tabHost.getTabWidget().setDividerDrawable(null);
        for (int i = 0; i < TabBarManagerActivity.tabHost.getTabWidget().getTabCount(); i++) {
            TabBarManagerActivity.tabHost.getTabWidget().getChildAt(i).getLayoutParams().height
                    = (int) (height * TabBarManagerActivity.context.getResources().getDisplayMetrics().density);
        }
    }

    private void onShareAttachmentForPersonal() {
        // Intent intent = new Intent(this, StartChatListActivity.class);
        //startActivityForResult(intent, ChatStarter.CONVERSATION_REQUEST);
    }

    private void onShareAttachmentForBuzz() {
        //Intent intent = new Intent(this, BuzzPostActivity.class);
        // startActivity(intent);
    }

    private void onShareAttachmentCancel() {
        //Model.getInstance().setShareBundle(null);
    }

    @Override
    public void update(Observable observable, Object o) {
        /**
         if (isAuthSavedUser) {
         isAuthSavedUser = false;
         getBusyIndicator().dismiss();

         if (o == ModelEvent.USER_CHANGED) {
         // Hack to get the buzz list to load.
         Model.getInstance().getBuzzListItem().loadNewerBuzzes();

         handleAttachment();
         } else if (o == ModelEvent.LOGIN_FAILED) {
         showLoginView();
         }
         }**/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if (!iabHelper.handleActivityResult(requestCode, resultCode, data) && data != null) {
            // ChatStarter.openConversationScreenWithActivityResult(requestCode, resultCode, data, this);
        }
    }

    public void queryIABInventory() {
        if (!UserDefaults.getAdvertsDisabled()) {
            //workaround to prevent multiple async event crash on iabHelper
            if (iabHelper != null) {
                iabHelper.flagEndAsync();
                iabHelper.queryInventoryAsync(onIabGotInventoryListener);
            }
        }
    }

    IabHelper.QueryInventoryFinishedListener onIabGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                SimpleAlertDialog.showMessageWithOkButton(getContext(), getResources().getString(R.string.error), getString(R.string.failed_check_already_purchased), null);
            } else {
                if (inventory.hasPurchase(DISABLE_ADS_ITEM_SKU)) {
                    disableAds();
                } else {
                    //workaround to prevent multiple async event crash on iabHelper
                    if (iabHelper != null) {
                        iabHelper.flagEndAsync();

                        //start purchase flow for buying the disable ads item
                        iabHelper.launchPurchaseFlow(TabBarManagerActivity.this, DISABLE_ADS_ITEM_SKU, DISABLE_ADS_ITEM_REQUEST_CODE, onIabPurchasedFinshedListener, DISABLE_ADS_PURCHASE_TOKEN);
                    }
                }
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener onIabPurchasedFinshedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                SimpleAlertDialog.showMessageWithOkButton(getContext(), getResources().getString(R.string.error), getString(R.string.failed_google_play_purchase), null);
            } else if (purchase.getSku().equals(DISABLE_ADS_ITEM_SKU)) {
                disableAds();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         if (iabHelper != null) {
         iabHelper.dispose();
         iabHelper = null;
         }**/
    }

    public void disableAds() {
        UserDefaults.setAdvertsDisabled(true);
        // Model.getInstance().notifyObserversOfEvent(ModelEvent.DISABLED_ADS);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkUserPrivateKey();
            // handler.postDelayed(runnable, 5000);
        }
    };
    private Handler handler = new Handler();
    private boolean isRequesting = false;

    private void checkUserPrivateKey() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Log.v("testlog", "checkUserPrivateKey:= " + result.toString());
                    if (result.has("error")) {
                        if (!getBusyIndicator(R.string.requesting_key).isShowing() && !isRequesting) {

                            //updateKey();
                            Runnable onCancel = new Runnable() {
                                @Override
                                public void run() {
                                    TabBarManagerActivity.this.finish();
                                }
                            };

                            Runnable onAccept = new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(TabBarManagerActivity.this, SignupActivity.class);
                                    startActivity(intent);
                                    TabBarManagerActivity.this.finish();
                                }
                            };
                            callAPIGetPublicKeyOfUser(String.valueOf(Model.getInstance().getCurrentUser().getUserId()));
                            isRequesting = true;
                            showMessageWithEditText(R.string.requesting_key, R.string.logout, R.string.signup_text, onCancel, onAccept );
                            handler.postDelayed(runnable, 5000);

                        } else {
                            checkUserPrivateKey();
                        }

                    } else {
                        getBusyIndicator(R.string.requesting_key).dismiss();
                        try {
                            String privateKey = result.getString("private_key");
                            String keyTemp = decryptPrivateKeyWithPassword(privateKey, UserDefaults.getSecrectKey(Model.getInstance().getCurrentUser().getUserId(), "1"));
                            Log.v("keyTemppp", keyTemp);
                            PrivateKey privateKeyTemp = Helpers.getPrivateKeyWithPemFormat(keyTemp);
                            UserDefaults.savePrivateKeyFromWeb(Model.getInstance().getCurrentUser().getUserId(), "1", keyTemp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            @Override
            public void onStart() {

                //getUserHivesList().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                //getUserHivesList().setData(modelObject);

            }

            @Override
            public void onFail(ModelError error) {

                //getUserHivesList().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Model.getInstance().getCurrentUser().getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        params.add(new BasicNameValuePair("device_id", Helpers.getDeviceID(this)));
        params.add(new BasicNameValuePair("hive_id", "1"));

        APIConnectionRequest.API_GetPrivateKeyRequest(callBackDone, params, Model.getInstance().getCurrentUser().getToken());
    }

    private String decryptPrivateKeyWithPassword(String messageContent, String decryptionPassword) {
        if (messageContent != null && decryptionPassword != null) {
            String dencrpytedContent = null;

            String temp = Helpers.stringFromB64(messageContent);
            byte[] dataFromB64 = Helpers.dataFromB64String(temp);

            byte[] outData = Crypto.decryptData(decryptionPassword, dataFromB64);


            EncryptableString encString = new EncryptableString();
            encString.setPreparedData(outData);
            dencrpytedContent = encString.getOriginalString();
            // byte[] preparedData = encString.setPreparedData();


            return dencrpytedContent;
        } else
            return messageContent;
    }

    private void updateKey() {
        String deviceName = android.os.Build.MODEL;
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    if (result.has("error")) {

                    } else {
                        Gson gson = new Gson();
                        Log.v("testlog", "updateKeysss:= " + result.toString());

                    }

                }
            }

            @Override
            public void onStart() {

                //getUserHivesList().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                //getUserHivesList().setData(modelObject);

            }

            @Override
            public void onFail(ModelError error) {

                //getUserHivesList().setError(error);
            }

            @Override
            public void onComplete() {

            }
        });

        UUID uuid = UUID.randomUUID();

        String secret = uuid.toString();
        String refKey = "";
        try {
            refKey = Crypto.RSAEncrypt(secret, publicKeyAsString);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        KeyRequest keyRequest = new KeyRequest();
        keyRequest.device_id = Helpers.getDeviceID(this);
        keyRequest.device_name = deviceName;
        keyRequest.hive_id = "1";
        keyRequest.secret = refKey;
        //Log.v("device_name", deviceName);
        //Log.v("device_id", Helpers.getDeviceID(this));
        //Log.v("device_name_secret", refKey);
        UserDefaults.saveSecrectKey(Model.getInstance().getCurrentUser().getUserId(), "1", secret);
        APIConnectionRequest.API_PostPrivateKeyRequest(callBackDone, keyRequest, Model.getInstance().getCurrentUser().getToken());

    }

    private String publicKeyAsString = "";
    private PublicKey publicKey;


    private void callAPIGetPublicKeyOfUser(final String idUser) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {

                if (result != null) {

                    Gson gson = new Gson();
                    PublicKeyOfUserResponse userResponse = gson.fromJson(result.toString(), PublicKeyOfUserResponse.class);
                    try {
                        if (userResponse.pk.size() > 0) {
                            //toDeviceID = userResponse.pk.get(0).deviceId;
                            publicKeyAsString = userResponse.pk.get(0).publicKey;
                            publicKey = Helpers.getPublicKeyFromPemFormat(publicKeyAsString, false);
                            UserDefaults.savePubicKeyFromWeb(Model.getInstance().getCurrentUser().getUserId(), "1", publicKey);
                            updateKey();

                        } else {

                        }
                    } catch (Exception e) {
                    }
                } else {
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());

            }

            @Override
            public void onComplete() {

            }
        });
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Model.getInstance().getCurrentUser().getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetUserPublicKey(callBackDone, "publicKey/" + idUser, params, Model.getInstance().getCurrentUser().getToken());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (tabHost.getCurrentTab() == 0) {


            for (Fragment fragment : getSupportFragmentManager().getFragments()) {

                if (fragment instanceof MessagesListMainFragment) {

                    int count = ((MessagesListMainFragment) fragment).getFragmentManager().getBackStackEntryCount();

                    if (count == 0) {
                        super.onBackPressed();
                        //additional code
                    } else {
                        ((MessagesListMainFragment) fragment).getFragmentManager().popBackStack();
                    }
                    // ((MessagesListMainFragment)fragment).getFragmentManager();

                }
            }
        }
        //tabHost.setCurrentTab(Tabs.BUZZ_TAB.getValue());
    }

}

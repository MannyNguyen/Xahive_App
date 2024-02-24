package ca.xahive.app.bl.local;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import IAPUtils.IabHelper;
import IAPUtils.IabResult;
import IAPUtils.Inventory;
import IAPUtils.Purchase;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.gcm.GcmIntentService;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.ui.activities.BaseActivity;
import ca.xahive.app.ui.activities.LoginActivity;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

public class TabBarManager extends BaseActivity implements OnTabChangeListener, Observer  {
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

        context = TabBarManager.this;
        Model.startLocationManager(this);
        Model.getInstance().addObserver(this);

        setContentView(R.layout.activity_tabbar_manager);
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
        }
        catch (Exception e) {
            XADebug.d("Failed to set up in-app billing.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getIntent().getExtras() != null){
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
        }else{
            System.out.println("No extras were found on intent.");
        }

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

    private void handleAttachment() {
        if (selectedConversationUser != -1) {
           // ChatStarter.startChatWithUser(this, selectedConversationUser);
            selectedConversationUser = -1;
        }
        else if (Model.getInstance().getCurrentUser().isAuthenticated() && Model.getInstance().getShareBundle() != null) {
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

        //setup Honeycombs tab
        View honeyTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) honeyTabView.findViewById(R.id.tabLabel)).setText(R.string.honeycombs_title);
        FontHelper.getInstance(this).setCustomFont((TextView) honeyTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) honeyTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.icon_honeycombs_tab);

        TabSpec honeySpec = tabHost.newTabSpec(getString(R.string.honeycombs_title)).setIndicator(honeyTabView);
        //tabHost.addTab(honeySpec, HoneycombsTab.class, null);

        //setup Messages tab
        View messagesTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
        ((TextView) messagesTabView.findViewById(R.id.tabLabel)).setText(R.string.messages_title);
        FontHelper.getInstance(this).setCustomFont((TextView) messagesTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        ((ImageView) messagesTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_messages_tab);

        TabSpec messagesSpec = tabHost.newTabSpec(getString(R.string.messages_title)).setIndicator(messagesTabView);
        //tabHost.addTab(messagesSpec, MessagesTab.class, null);

        //setup Settings tab
//        View settingsTabView = LayoutInflater.from(tabHostContext).inflate(R.layout.xa_tab_layout, null);
//        ((TextView) settingsTabView.findViewById(R.id.tabLabel)).setText(R.string.settings_title);
//        FontHelper.getInstance(this).setCustomFont((TextView) settingsTabView.findViewById(R.id.tabLabel), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
//        ((ImageView) settingsTabView.findViewById(R.id.tabIcon)).setImageResource(R.drawable.style_icon_settings_tab);
//
//        TabSpec settingsSpec = tabHost.newTabSpec(getString(R.string.settings_title)).setIndicator(settingsTabView);
//        //tabHost.addTab(settingsSpec, SettingsTab.class, null);

        TabBarManager.updateTabHeight(TAB_HEIGHT);
    }

    @Override
    public void onTabChanged(String tabId) {
        lastSelectedTab = tabHost.getCurrentTab();
    }

    public static void updateTabHeight(int height) {
        for (int i = 0; i < TabBarManager.tabHost.getTabWidget().getTabCount(); i++) {
            TabBarManager.tabHost.getTabWidget().getChildAt(i).getLayoutParams().height
                    = (int) (height * TabBarManager.context.getResources().getDisplayMetrics().density);
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

        if (!iabHelper.handleActivityResult(requestCode, resultCode, data) && data != null) {
          //  ChatStarter.openConversationScreenWithActivityResult(requestCode, resultCode, data, this);
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
                        iabHelper.launchPurchaseFlow(TabBarManager.this, DISABLE_ADS_ITEM_SKU, DISABLE_ADS_ITEM_REQUEST_CODE, onIabPurchasedFinshedListener, DISABLE_ADS_PURCHASE_TOKEN);
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
        if (iabHelper != null) {
            iabHelper.dispose();
            iabHelper = null;
        }
    }

    public void disableAds(){
        UserDefaults.setAdvertsDisabled(true);
       // Model.getInstance().notifyObserversOfEvent(ModelEvent.DISABLED_ADS);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}

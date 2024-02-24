package ca.xahive.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.CurrentUser;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;

public class LoginActivity extends BaseActivity implements UserAgreedToTermsDelegate, View.OnClickListener, TextView.OnEditorActionListener{

    private boolean userAgreedToTerms = false;
    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FontHelper.getInstance(this).setCustomFont(getUsernameField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);
        FontHelper.getInstance(this).setCustomFont(getPasswordField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);

        FontHelper.getInstance(this).setCustomFont(getLoginButton(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(this).setCustomFont(getAnonLoginButton(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(this).setCustomFont(getForgotPasswordField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);

        getNaviga().updateRightButton("sign up", this);
        Model.getInstance().setDeviceID(Helpers.getDeviceID(this));
        getLoginButton().setOnClickListener(this);
        getSignupButton().setOnClickListener(this);
        getAnonLoginButton().setOnClickListener(this);
        getForgotPasswordField().setOnClickListener(this);
        getPasswordField().setOnEditorActionListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserDefaults.mContext == null)
            UserDefaults.setContext(getApplicationContext());
        if (Model.getInstance().isJustSignUp) {
            Model.getInstance().isLoginAnon = false;
            performAnonLogin();
        }

        if (userAgreedToTerms) {
            getBusyIndicator().show();
            Model.getInstance().authenticateAnonymousUser();
        } else {
            String savedUsername = Helpers.stringOrEmptyInt(UserDefaults.getUserId());
            String savedPassword = Helpers.stringOrEmptyString(UserDefaults.getPassword());

            getUsernameField().setText(savedUsername);
            getPasswordField().setText(savedPassword);
        }

    }

    @Override
    public void userAgreedToTerms(boolean agreed) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.loginButton):
                Model.getInstance().isLoginAnon = false;
                validateLogin();
                break;
            case (R.id.right_nav_button):
                presentSignupPage();
                break;
            case (R.id.forgotField):
                presentForgotPassword();
                break;
            case (R.id.anonLoginButton):
                Model.getInstance().isLoginAnon = true;
                performAnonLogin();
                break;

        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            Model.getInstance().isLoginAnon = false;
            validateLogin();
        }
        return false;
    }

    private void presentForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void presentSignupPage() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void validateLogin() {
        String username = Helpers.extractStringFromTextView(getUsernameField());
        String password = Helpers.extractStringFromTextView(getPasswordField());
        // boolean usernameOk = android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches();
        boolean usernameOk = true;
        boolean passwordOk = Helpers.stringIsNotNullAndMeetsMinLength(password, Config.MINPASSWDLENGTH);
        if (usernameOk && passwordOk) {
            performLogin();
        } else {
            SimpleAlertDialog.showMessageWithOkButton(
                    this,
                    getString(R.string.missing_information),
                    getString(R.string.missing_login_info_msg),
                    null
            );
        }
    }

    private void performAnonLogin() {
        ParceledInteger parceledInteger = Model.getInstance().getParcelator().createParcelForObject(this);

        Intent intent = new Intent(this, TermsAndConditionsActivity.class);
        intent.putExtra(Parcelator.PARCELATOR_KEY, parceledInteger);
        startActivityForResult(intent, REQUEST_CODE);

    }

    private void performLogin() {
        getBusyIndicator().show();

        Model.getInstance().getCurrentUser().setEmail(Helpers.extractStringFromTextView(getUsernameField()));
        Model.getInstance().getCurrentUser().setPassword(Helpers.extractStringFromTextView(getPasswordField()));

        Model.getInstance().authenticateUser(Model.getInstance().getCurrentUser());
    }

    private void performLoginAnon() {
        getBusyIndicator().show();
        Model.getInstance().authenticateAnonymousUser();
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.USER_CHANGED) {
            onUserAuthChanged();
        } else if (evt == ModelEvent.LOGIN_FAILED) {
            onUserLoginFailed();
        }
    }

    protected void onUserAuthChanged() {
        getBusyIndicator().dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        if (Model.getInstance().getCurrentUser().isAuthenticated()) {
            Helpers.hideSoftKeyboardInActivity(this);
            finish();
        } else {
            // Should never get here.
        }
    }

    private void onUserLoginFailed() {
        getBusyIndicator().dismiss();

        ModelError loginError = Model.getInstance().getLoginError();

        if (loginError != null && !loginError.isHandled()) {
            SimpleAlertDialog.showErrorWithOkButton(this, loginError, null);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (Model.getInstance().isLoginAnon) {
                performLoginAnon();
            } else {
                UserDefaults.setUsername(null);
                UserDefaults.setPassword(null);
                performLogin();
            }
        }


    }

    protected EditText getUsernameField() {
        return (EditText) findViewById(R.id.emailField);
    }

    protected EditText getPasswordField() {
        return (EditText) findViewById(R.id.passwordField);
    }

    protected Button getLoginButton() {
        return (Button) findViewById(R.id.loginButton);
    }

    protected TextView getForgotPasswordField() {
        return (TextView) findViewById(R.id.forgotField);
    }

    protected RelativeLayout getSignupButton() {
        return (RelativeLayout) findViewById(R.id.right_nav_button);
    }

    protected NavigationBar getNaviga() {
        return (NavigationBar) findViewById(R.id.navBar);
    }

    protected Button getAnonLoginButton() {
        return (Button) findViewById(R.id.anonLoginButton);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}

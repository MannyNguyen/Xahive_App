package ca.xahive.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.CurrentUser;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.api_object.RegisterRequest;
import ca.xahive.app.bl.objects.api_object.UserRegisterRequest;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;
import ca.xahive.app.webservice.CallBackDone;

public class SignupActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();
    }

    private void initViews() {

        FontHelper.getInstance(this).setCustomFont(getAliasField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);
        FontHelper.getInstance(this).setCustomFont(getUsernameField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);
        FontHelper.getInstance(this).setCustomFont(getPasswordField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);
        FontHelper.getInstance(this).setCustomFont(getPasswordConfirmField(), FontHelper.CustomFontEnum.XAHNavBarButtonAndInputTextFont);
        FontHelper.getInstance(this).setCustomFont(getSignupButton(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);

        getNaviga().updateRightButton("log in", this);
        getSignupButton().setOnClickListener(this);
        getLoginButton().setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signupButton:
                validateSignup();
                break;
            case R.id.right_nav_button:
                presentLoginPage();
                finish();
                break;
        }
    }

    private void presentLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void validateSignup() {
        boolean aliasOk = Helpers.stringOrEmptyString(Helpers.extractStringFromTextView(getAliasField())).length() > Config.MINALIASLENGTH;
        boolean usernameOk = android.util.Patterns.EMAIL_ADDRESS.matcher(Helpers.extractStringFromTextView(getUsernameField())).matches();
        boolean passwordOk = Helpers.stringIsNotNullAndMeetsMinLength(Helpers.extractStringFromTextView(getPasswordField()), Config.MINPASSWDLENGTH);
        boolean passwordMatch = Helpers.stringOrEmptyString(Helpers.extractStringFromTextView(getPasswordConfirmField())).equals(Helpers.extractStringFromTextView(getPasswordField()));

        if (aliasOk && usernameOk && passwordOk && passwordMatch) {
            performSignup();
        } else {
            SimpleAlertDialog.showMessageWithOkButton(
                    this,
                    getString(R.string.invalid_info),
                    getString(R.string.invalid_signup_info_msg),
                    null
            );
        }
    }

    private void performSignup() {
        getBusyIndicator(getString(R.string.signup_loading)).show();

        CurrentUser user = new CurrentUser();
        user.setAlias(Helpers.extractStringFromTextView(getAliasField()));
        user.setEmail(Helpers.extractStringFromTextView(getUsernameField()));
        user.setPassword(Helpers.extractStringFromTextView(getPasswordField()));

        callAPISignup(user);
    }

    private void callAPISignup(CurrentUser user) {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    onSignupSucceeded();
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

                onSignupSucceeded();
                // grabInitialHiveSettings();

            }

            @Override
            public void onFail(ModelError error) {
                // Log.v("testlog", "error" + error.getCode());

                onSignupFailed(error);
            }

            @Override
            public void onComplete() {

            }
        });


        RegisterRequest loginRequest = new RegisterRequest();
        UserRegisterRequest userLoginRequest = new UserRegisterRequest();
        userLoginRequest.email = user.getEmail();
        userLoginRequest.encryptedPassword = user.getEncryptedPassword();
        userLoginRequest.isAnonMessageAllowed = "0";
        userLoginRequest.isAnonymous = "0";
        userLoginRequest.alias = user.getAlias();
        loginRequest.user = userLoginRequest;
        APIConnectionRequest.API_Signup(callBackDone, loginRequest);

    }


    private void onSignupFailed(ModelError error) {
        getBusyIndicator().dismiss();

        Runnable onAccepted = new Runnable() {
            @Override
            public void run() {
                // SignupActivity.this.signupRequest = null;
            }
        };

        SimpleAlertDialog.showErrorWithOkButton(SignupActivity.this, error, onAccepted);
    }

    private void onSignupSucceeded() {
        getBusyIndicator().dismiss();

        UserDefaults.setUsername(Helpers.extractStringFromTextView(getUsernameField()));
        UserDefaults.setPassword(Helpers.extractStringFromTextView(getPasswordField()));

        Runnable onAccepted = new Runnable() {
            @Override
            public void run() {
                Model.getInstance().isJustSignUp = true;
                SignupActivity.this.finish();
            }
        };

        SimpleAlertDialog.showMessageWithOkButton(SignupActivity.this, getString(R.string.success), getString(R.string.account_created_msg), onAccepted);
    }

    private EditText getAliasField() {
        return (EditText) findViewById(R.id.aliasField);
    }

    private EditText getUsernameField() {
        return (EditText) findViewById(R.id.userNameField);
    }

    private EditText getPasswordField() {
        return (EditText) findViewById(R.id.passwordField);
    }


    private EditText getPasswordConfirmField() {
        return (EditText) findViewById(R.id.confirmPasswordField);
    }

    private Button getSignupButton() {
        return (Button) findViewById(R.id.signupButton);
    }

    protected RelativeLayout getLoginButton() {
        return (RelativeLayout) findViewById(R.id.right_nav_button);
    }

    protected NavigationBar getNaviga() {
        return (NavigationBar) findViewById(R.id.navBar);
    }

}

package ca.xahive.app.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.LoadingView;
import ca.xahive.app.ui.views.NavigationBar;

public class TermsAndConditionsActivity extends Activity {
    private UserAgreedToTermsDelegate userAgreedToTermsDelegate;

    public UserAgreedToTermsDelegate getUserAgreedToTermsDelegate() {
        if (userAgreedToTermsDelegate == null) {
            userAgreedToTermsDelegate = new UserAgreedToTermsDelegate() {
                @Override
                public void userAgreedToTerms(boolean agreed) {
                }
            };
        }
        return userAgreedToTermsDelegate;
    }

    public void setUserAgreedToTermsDelegate(UserAgreedToTermsDelegate userAgreedToTermsDelegate) {
        this.userAgreedToTermsDelegate = userAgreedToTermsDelegate;
    }

    protected LoadingView getLoadingView() {
        return (LoadingView) findViewById(R.id.termsLoadingView);
    }

    protected WebView getWebView() {
        return (WebView) findViewById(R.id.webView);
    }

    protected CheckBox getAgreeCheckBox() {
        return (CheckBox) findViewById(R.id.agreeCheckbox);
    }

    protected CheckBox getAgeCheckBox() {
        return (CheckBox) findViewById(R.id.ageCheckbox);
    }

    protected Button getAcceptButton() {
        return (Button) findViewById(R.id.acceptButton);
    }

    protected Button getCancelButton() {
        return (Button) findViewById(R.id.cancelButton);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms_conditions);

        NavigationBar navBar = (NavigationBar) findViewById(R.id.navBar);
        navBar.configNavBarWithTitle(getString(R.string.terms_and_conditions_title));

        View.OnClickListener acceptButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptButtonPressed();
            }
        };
        getAcceptButton().setOnClickListener(acceptButtonListener);

        View.OnClickListener cancelButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelButtonPressed();
            }
        };
        getCancelButton().setOnClickListener(cancelButtonListener);

        FontHelper.getInstance(this).setCustomFont(getAgreeCheckBox(), FontHelper.CustomFontEnum.XAHTableSectionHeaderFont);
        FontHelper.getInstance(this).setCustomFont(getAgeCheckBox(), FontHelper.CustomFontEnum.XAHTableSectionHeaderFont);

        FontHelper.getInstance(this).setCustomFont(getAcceptButton(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(this).setCustomFont(getCancelButton(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoadingView().setVisibility(View.VISIBLE);
        getWebView().setVisibility(View.GONE);
        getWebView().loadUrl(Config.TERMS_AND_CONDITIONS_URL);
        WebSettings settings = getWebView().getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        getWebView().setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                getLoadingView().setVisibility(View.GONE);
                getWebView().setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed(); // Ignore SSL certificate errors
            }

        });

        ParceledInteger parceledInteger = getIntent().getParcelableExtra(Parcelator.PARCELATOR_KEY);

        if (parceledInteger != null) {
            setUserAgreedToTermsDelegate((UserAgreedToTermsDelegate) Model.getInstance().getParcelator().getObjectForParcel(parceledInteger));
        }
    }

    private void acceptButtonPressed() {
        boolean checkBoxesOk = (getAgreeCheckBox().isChecked() && getAgeCheckBox().isChecked());

        if (checkBoxesOk) {
            Model.getInstance().isJustSignUp = false;
            TermsAndConditionsActivity.this.getUserAgreedToTermsDelegate().userAgreedToTerms(true);
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();

        } else {
            SimpleAlertDialog.showMessageWithOkButton(this, getString(R.string.error), getString(R.string.terms_checkbox_error_message), null);
        }
    }

    private void cancelButtonPressed() {
        finish();
    }

}

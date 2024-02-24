package ca.xahive.app.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.ui.views.NavigationBar;

public class ForgotPasswordActivity extends BaseActivity implements TokenEntrySuccessDelegate, View.OnClickListener {
    private boolean tokenEntrySuccess;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        FontHelper.getInstance(this).setCustomFont(getTokenRequiredTextView(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        FontHelper.getInstance(this).setCustomFont(getRequestTokenButton(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        FontHelper.getInstance(this).setCustomFont(getUsernameField(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        FontHelper.getInstance(this).setCustomFont(getEnterTokenButton(), FontHelper.CustomFontEnum.XAHMiscLabelFont);

        getRequestTokenButton().setOnClickListener(this);
        getEnterTokenButton().setOnClickListener(this);
        getBackButton().setOnClickListener(this);
        getSignupButton().setOnClickListener(this);
        getNaviga().updateRightButton(getString(R.string.signup_text), this);
        getNaviga().updateLeftButton(getString(R.string.back_text), this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (tokenEntrySuccess) {
            finish();
        }
    }

    private void requestButtonPressed() {

    }

    private void entryButtonPressed() {

    }

    @Override
    public void tokenEntryCompleted(boolean success) {
        tokenEntrySuccess = success;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.requestTokenButton):
                requestButtonPressed();
                break;
            case (R.id.enterTokenButton):
                entryButtonPressed();
                break;
            case (R.id.right_nav_button):
                break;
            case (R.id.left_nav_button):
                finish();
                break;

        }
    }

    protected NavigationBar getNaviga() {
        return (NavigationBar) findViewById(R.id.navBar);
    }

    private Button getRequestTokenButton() {
        return (Button) findViewById(R.id.requestTokenButton);
    }

    protected EditText getUsernameField() {
        return (EditText) findViewById(R.id.emailField);
    }

    protected RelativeLayout getSignupButton() {
        return (RelativeLayout) findViewById(R.id.right_nav_button);
    }

    protected RelativeLayout getBackButton() {
        return (RelativeLayout) findViewById(R.id.left_nav_button);
    }

    private Button getEnterTokenButton() {
        return (Button) findViewById(R.id.enterTokenButton);
    }

    private TextView getTokenRequiredTextView() {
        return (TextView) findViewById(R.id.tokenRequiredTextView);
    }
}

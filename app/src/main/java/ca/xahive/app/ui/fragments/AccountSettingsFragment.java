package ca.xahive.app.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Observable;

import ca.xahive.app.ui.activities.LoginActivity;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.local.Avatar;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.local.XYDimension;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.api_object.PasswordUpdateRequest;
import ca.xahive.app.bl.utils.Config;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.S3TaskParams;
import ca.xahive.app.bl.utils.S3TaskResult;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.HexagonImageView;
import ca.xahive.app.ui.views.NavigationBar;
import ca.xahive.app.webservice.CallBackDone;

public class AccountSettingsFragment extends BaseFragment implements View.OnClickListener {

    public static AccountSettingsFragment _instance;

    private static int SELECT_AVATAR = 41;

    private Avatar avatar;
    private boolean shouldUpdateAvatar;

    public static AccountSettingsFragment getInstance() {
        if (_instance == null) {
            _instance = new AccountSettingsFragment();
        }
        return _instance;
    }


    private TextView getIdField(){
        return (TextView)getActivity().findViewById(R.id.id_field);
    }
    protected EditText getAliasField() {
        return (EditText) getActivity().findViewById(R.id.aliasField);
    }

    protected HexagonImageView getAvatarImageView() {
        return (HexagonImageView) getActivity().findViewById(R.id.avatarImageView);
    }

    protected EditText getPasswordField() {
        return (EditText) getActivity().findViewById(R.id.passwordField);
    }

    protected EditText getConfirmPasswordField() {
        return (EditText) getActivity().findViewById(R.id.confirmPasswordField);
    }

    protected EditText getOldPasswordField() {
        return (EditText) getActivity().findViewById(R.id.oldField);
    }

    protected EditText getNewPasswordField() {
        return (EditText) getActivity().findViewById(R.id.newPasswordField);
    }


    protected TextView getPasswordLabel() {
        return (TextView) getActivity().findViewById(R.id.accountChangePasswordLabel);
    }


    protected EditText getEmailField() {
        return (EditText) getActivity().findViewById(R.id.emailField);
    }

    protected ImageView getEditEmailButton() {
        return (ImageView) getActivity().findViewById(R.id.editEmail);
    }


    protected ImageView getEditPasswordlButton() {
        return (ImageView) getActivity().findViewById(R.id.editPassword);
    }

    protected ImageView getEditAliasButton() {
        return (ImageView) getActivity().findViewById(R.id.editAlias);
    }

    protected LinearLayout getEditPasswordView() {
        return (LinearLayout) getActivity().findViewById(R.id.changePassItem);
    }

    protected LinearLayout getActionEditView() {
        return (LinearLayout) getActivity().findViewById(R.id.actionEditView);
    }

    protected Button getCancelButton() {
        return (Button) getActivity().findViewById(R.id.cancelBtn);
    }

    protected Button getSaveButton() {
        return (Button) getActivity().findViewById(R.id.saveBtn);
    }

    protected RelativeLayout getEmailFieldView() {
        return (RelativeLayout) getActivity().findViewById(R.id.emailFieldView);
    }

    /**
     protected TextView getChangeEmailItemTextView(){
     return (TextView)getChangeEmailItem().findViewById(R.id.chevronCellTextView);
     }**/

    /**
     * protected Button getLogoutButton() {
     * return (Button)getActivity().findViewById(R.id.logoutBtn);
     * }
     **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    protected Button getUploadNewButton() {
        return (Button) getActivity().findViewById(R.id.uploadNewButton);
    }

    private void setupUploadButton() {
        getUploadNewButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, SELECT_AVATAR);
            }
        });
    }

    public void updateLoginPassword(String currentPassword) {
        loginPasswordUpdateCallAPI(currentPassword);
    }

    private void loginPasswordUpdateCallAPI(String currentPassword) {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    if (result.has("success")) {
                        try {
                            if (result.getString("success").equals("true")) {
                                UserDefaults.setPassword(Model.getInstance().getCurrentUser().getPassword());
                                getBusyIndicator().dismiss();
                                SimpleAlertDialog.showMessageWithOkButton(
                                        getActivity(),
                                        getString(R.string.success),
                                        getString(R.string.changed_setting),
                                        null
                                );
                                setFieldViewInit();
                                return;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }
                SimpleAlertDialog.showMessageWithOkButton(
                        getActivity(),
                        getString(R.string.invalid_email),
                        getString(R.string.invalid_email_msg),
                        null
                );

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

            }

            @Override
            public void onFail(ModelError error) {

            }

            @Override
            public void onComplete() {

            }
        });
        PasswordUpdateRequest updatePasswordSettingRequest = new PasswordUpdateRequest();
        updatePasswordSettingRequest.current_password = currentPassword;
        updatePasswordSettingRequest.new_password = Model.getInstance().getCurrentUser().getEncryptedPassword();
        APIConnectionRequest.API_UpdatePassword(callBackDone, updatePasswordSettingRequest, Model.getInstance().getCurrentUser().getToken());


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_AVATAR && resultCode == getActivity().RESULT_OK) {

            final Uri contentUri = data.getData();
            File avatarFile = new File(Helpers.getRealPathFromURI(contentUri));

            //Helpers.rotateSavedImage(avatarFile);

            S3TaskParams taskParams = new S3TaskParams(avatarFile, this.avatar.getKey());

            new AvatarUploadTask(new Runnable() {
                @Override
                public void run() {

                    //Log.v("contentUri",String.valueOf(contentUri));
                    if (!avatar.updateAvatarWithSelectedImage(contentUri)) {
                        showAvatarUpdateError();
                    } else {

                    }
                }
            }).execute(taskParams);

        } else if(requestCode == SELECT_AVATAR) {
            showAvatarUpdateError();
        }

    }

    private void showAvatarUpdateError() {
        SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.avatar_update_failed), null);
    }

    private class AvatarUploadTask extends AsyncTask<S3TaskParams, Integer, S3TaskResult> {

        ProgressDialog dialog;
        Runnable onSuccess;

        public AvatarUploadTask(Runnable onSuccess) {
            super();
            this.onSuccess = onSuccess;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(getString(R.string.uploading_avatar));
            dialog.setMax(100);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }


        @Override
        protected S3TaskResult doInBackground(S3TaskParams... params) {

            S3TaskResult result = new S3TaskResult();

            result.setFile(params[0].getFile());

            try {

                TransferManager tm = Model.getInstance().getS3Manager();
                ObjectMetadata metadata = new ObjectMetadata();
                FileInputStream in = new FileInputStream(params[0].getFile());
                metadata.setContentLength((params[0].getFile()).length());

                PutObjectRequest por = new PutObjectRequest(Config.S3_FILE_BUCKET, params[0].getKey(), in, metadata);
                final Upload myUpload = tm.upload(por);
                while (!myUpload.isDone()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setProgress((int) myUpload.getProgress().getPercentTransferred());

                        }
                    });
                }

                return result;

            } catch (Exception e) {
                result.setErrorMessage(e.getMessage());
                return result;
            }

        }


        @Override
        protected void onPostExecute(S3TaskResult result) {
            dialog.dismiss();

            if (result.getErrorMessage() != null) {
                showAvatarUpdateError();
            } else {
                onSuccess.run();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NavigationBar navBar = (NavigationBar) getActivity().findViewById(R.id.navBar);
        navBar.configNavBarWithTitleAndRightButton(
                getString(R.string.action_settings),
                getString(R.string.logout).toLowerCase(), R.drawable.logout_white, this);
        //getChangeEmailItemTextView().setText(getString(R.string.email_label));
        //getChangePasswordItemTextView().setText(getString(R.string.password_label));
        // getChangeDisplayPictureItemTextView().setText(getString(R.string.display_picture));


        setFieldViewInit();
        /**
         DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
         .cacheInMemory(true)
         .cacheOnDisk(true).build();
         ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
         .defaultDisplayImageOptions(defaultOptions).build();
         ImageLoader.getInstance().init(config);
         //ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(getActivity());
         // ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));
         ImageLoader img = ImageLoader.getInstance();

         //        getUserNameField().setText("" + Model.getInstance().getCurrentUser().getUserId());
         // getAliasField().setText(Model.getInstance().getCurrentUser().getAlias());
         **/
        setupUploadButton();

        loadCurrentAvatar();
        getCancelButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFieldViewInit();
            }
        });

        getSaveButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getAliasField().isEnabled() || getEmailField().isEnabled()) {
                    saveNewSetting();
                }
                if (getEditPasswordView().getVisibility() == View.VISIBLE) {
                    saveNewPassword();
                }


            }
        });
        getEditAliasButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditAliasButton().setVisibility(View.GONE);
                getAliasField().setEnabled(true);
                getAliasField().setFocusableInTouchMode(true);
                getActionEditView().setVisibility(View.VISIBLE);
            }
        });
        getEditPasswordlButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditPasswordlButton().setVisibility(View.GONE);
                getPasswordField().setVisibility(View.GONE);
                getPasswordLabel().setVisibility(View.GONE);
                getEditPasswordView().setVisibility(View.VISIBLE);
                getActionEditView().setVisibility(View.VISIBLE);
                getOldPasswordField().setFocusable(true);
                Helpers.showSoftKeyboardForEditText(getOldPasswordField());
            }
        });
        getEditEmailButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditEmailButton().setVisibility(View.GONE);
                getEmailField().setEnabled(true);
                getEmailField().setFocusableInTouchMode(true);
                getActionEditView().setVisibility(View.VISIBLE);
            }
        });
        /**
         getLogoutButton().setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
        logoutUser();
        }
        });**/

        //fonts


        Model.getInstance().getCurrentUser().addObserver(this, true);
        FontHelper.getInstance(getActivity()).setCustomFont(getAliasField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(getActivity()).setCustomFont(getConfirmPasswordField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(getActivity()).setCustomFont(getPasswordField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(getActivity()).setCustomFont(getOldPasswordField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(getActivity()).setCustomFont(getNewPasswordField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);
        FontHelper.getInstance(getActivity()).setCustomFont(getEmailField(), FontHelper.CustomFontEnum.XAHButtonAndLoginSignUpLabelFont);

        //        FontHelper.getInstance(getActivity()).setCustomFont(getUserNameField(), FontHelper.CustomFontEnum.XAHBuzzCountLabelFont);
        //      FontHelper.getInstance(getActivity()).setCustomFont(getAliasField(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        // FontHelper.getInstance(this).setCustomFont(getChangeEmailItemTextView(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        //FontHelper.getInstance(this).setCustomFont(getChangePasswordItemTextView(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        //FontHelper.getInstance(this).setCustomFont(getChangeDisplayPictureItemTextView(), FontHelper.CustomFontEnum.XAHMiscLabelFont);
        //    FontHelper.getInstance(getActivity()).setCustomFont(getLogoutButton(), FontHelper.CustomFontEnum.XAHMiscLabelFont);

    }

    private Avatar getAvatar() {
        return avatar;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o!=null) {
            if (o.toString().equals("USER_CHANGED")) {
                getBusyIndicator().dismiss();
                SimpleAlertDialog.showMessageWithOkButton(
                        getActivity(),
                        getString(R.string.success),
                        getString(R.string.changed_setting),
                        null
                );
                UserDefaults.setPassword(Model.getInstance().getCurrentUser().getPassword());
                setFieldViewInit();
                return;
            }
        }
          if (shouldUpdateAvatar && getAvatar() != null) {
            Bitmap bitmap = getAvatar().getBitmapWithDimensions(new XYDimension(160, 160));
            if (bitmap != null) {
                if(getAvatarImageView()!=null)
                getAvatarImageView().setBitmapToClip(bitmap);
            }
        }

    }

    private void loadCurrentAvatar() {

        shouldUpdateAvatar = true;
        //Log.v("testlog", "avatar " + Model.getInstance().getCurrentUser().getUserId());
        setAvatar(Avatar.avatarWithId(Model.getInstance().getCurrentUser().getUserId()));

    }

    private void setAvatar(Avatar avatar) {

        if (this.avatar != null) {
            this.avatar.deleteObserver(this);
        }


        this.avatar = avatar;

        if (this.avatar != null) {
            this.avatar.addObserver(this, true);
        }
    }


    private void saveNewPassword() {
        boolean oldPasswordOk = Helpers.extractStringFromTextView(getOldPasswordField()).equals(UserDefaults.getPassword());
        boolean newPasswordOK = Helpers.stringIsNotNullAndMeetsMinLength(Helpers.extractStringFromTextView(getNewPasswordField()), Config.MINPASSWDLENGTH);
        boolean confirmNewPasswordOK = Helpers.extractStringFromTextView(getNewPasswordField()).equals(Helpers.extractStringFromTextView(getConfirmPasswordField()));

        if (!oldPasswordOk) {
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.wrong_password),
                    getString(R.string.wrong_password_msg),
                    null
            );
        } else if (!newPasswordOK) {
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.invalid_password),
                    getString(R.string.invalid_password_msg),
                    null
            );

        } else if (!confirmNewPasswordOK) {
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.invalid_password),
                    getString(R.string.passwords_mismatched_msg),
                    null
            );

        } else {

            getBusyIndicator(R.string.loading).show();
            Model.getInstance().getCurrentUser().setPassword(Helpers.extractStringFromTextView(getNewPasswordField()));
            updateLoginPassword(Helpers.getEncryptedPassword(UserDefaults.getPassword()));
        }
    }

    private void saveNewSetting() {
        boolean emailOk = Helpers.stringIsValidEmail(Helpers.extractStringFromTextView(getEmailField()));
        if (!emailOk && getEmailField().isEnabled()) {
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.invalid_email),
                    getString(R.string.invalid_email_msg),
                    null
            );
        } else {
            getBusyIndicator(R.string.loading).show();
            Model.getInstance().getCurrentUser().setEmail(Helpers.extractStringFromTextView(getEmailField()));
            Model.getInstance().getCurrentUser().setAlias(Helpers.extractStringFromTextView(getAliasField()));
            Model.getInstance().updateLoginUser();

        }

    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.USER_CHANGED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.success),
                    getString(R.string.changed_setting),
                    null
            );

            UserDefaults.setPassword(Model.getInstance().getCurrentUser().getPassword());


            setFieldViewInit();
        } else if (evt == ModelEvent.LOGIN_USER_UPDATE_FAILED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.error),
                    getString(R.string.failed_change_setting),
                    null
            );
        }
    }

    private void saveNewEmail() {
        boolean emailOk = Helpers.stringIsValidEmail(Helpers.extractStringFromTextView(getEmailField()))
                && !((Helpers.extractStringFromTextView(getEmailField())).equals(Model.getInstance().getCurrentUser().getEmail()));

        //String passwordFieldString = Helpers.extractStringFromTextView(getPasswordField());
        //String savedPasswordString = UserDefaults.getPassword();
        //  boolean passwordOk = passwordFieldString.equals(savedPasswordString);

        if (!emailOk) {
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.invalid_email),
                    getString(R.string.invalid_email_msg),
                    null
            );
        }
        /**
         else if (!passwordOk) {
         SimpleAlertDialog.showMessageWithOkButton(
         getActivity(),
         getString(R.string.wrong_password),
         getString(R.string.wrong_password_msg),
         null
         );

         }**/
        else {
            getBusyIndicator().show();
            Model.getInstance().getCurrentUser().setEmail(Helpers.extractStringFromTextView(getEmailField()));
            Model.getInstance().updateLoginUser();
        }
    }


    private void setFieldViewInit() {
        getEditPasswordView().setVisibility(View.GONE);
        getActionEditView().setVisibility(View.GONE);

        getEditAliasButton().setVisibility(View.VISIBLE);
        getEditEmailButton().setVisibility(View.VISIBLE);

        getEditPasswordlButton().setVisibility(View.VISIBLE);
        getPasswordField().setVisibility(View.VISIBLE);
        getPasswordLabel().setVisibility(View.VISIBLE);
        boolean isUserAnonymous = Model.getInstance().getCurrentUser().isAnonymous();
        if (Model.getInstance().hasUser() && Model.getInstance().getCurrentUser() != null) {
            getAliasField().setText(Model.getInstance().getCurrentUser().getAlias());
            getPasswordField().setText(Model.getInstance().getCurrentUser().getEmail());
            getEmailField().setText(Model.getInstance().getCurrentUser().getEmail());
            getIdField().setText(String.valueOf(Model.getInstance().getCurrentUser().getUserId()));

        }
        getAliasField().setFocusable(false);
        getPasswordField().setFocusable(false);
        getEmailField().setFocusable(false);

        getAliasField().setEnabled(false);
        getPasswordField().setEnabled(false);
        getEmailField().setEnabled(false);
        //setup the other fields and buttons if user is not anonymous
        if (!isUserAnonymous) {
            setupAliasFieldHandling();

            /**
             getChangeEmailItem().setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
            launchChangeEmailScreen();
            }
            });

             getChangePasswordItem().setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
            launchChangePasswordScreen();
            }
            });

             getChangeDisplayPictureItem().setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
            launchChangeDisplayPictureScreen();
            }
            });
             **/
        } else {

            //getChangeEmailItem().setCellDisabled(true);
            //getChangePasswordItem().setCellDisabled(true);
            //getChangeDisplayPictureItem().setCellDisabled(true);
        }

    }

    private void launchChangeEmailScreen() {
        //Intent intent = new Intent(this, ChangeAccountEmail.class);
        //startActivity(intent);
    }

    private void launchChangePasswordScreen() {
        // Intent intent = new Intent(this, ChangeAccountPassword.class);
        //startActivity(intent);
    }

    private void launchChangeDisplayPictureScreen() {
        // Intent intent = new Intent(this, ChangeAvatarActivity.class);
        //startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.right_nav_button):
                logoutUser();
                // onAddButton();
                break;
        }
    }

    private void logoutUser() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
        Model.getInstance().logoutCurrentUser();
        getActivity().finish();
    }

    private void setupAliasFieldHandling() {

        getAliasField().setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateCurrentUserAlias();

                    return false;

                } else {
                    return false;
                }

            }
        });
    }


    public void updateCurrentUserAlias() {
        String newAlias = Helpers.extractStringFromTextView(getAliasField());

        if (!(Model.getInstance().getCurrentUser().getAlias().equals(newAlias)) && (newAlias.length() > 0)) {
            Model.getInstance().getCurrentUser().setAlias(newAlias);
            Model.getInstance().updateLoginUser();
        }
    }
}

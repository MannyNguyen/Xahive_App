package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.objects.api_object.ChangeAliasHiveRequest;
import ca.xahive.app.bl.objects.api_object.DeleteHiveRequest;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.cells.DrowsyRelativeLayoutCell;
import ca.xahive.app.ui.cells.SettingEditRowCell;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;

/**
 * Created by prosoft on 10/28/15.
 */
public class HiveSettingFragment extends BaseFragment implements View.OnClickListener {

    public static HiveSettingFragment _instance;
    public SettingState state;

    public static HiveSettingFragment getInstance() {
        if (_instance == null) {
            _instance = new HiveSettingFragment();
        }
        return _instance;
    }

    public enum SettingState {
        CHANGEPASS(0),
        CHANGEALIAS(1),
        CHANGEMETHOD(2),
        MESSAGE(3),
        SELECTOR(4);

        private final int value;

        SettingState(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public LinearLayout getSeletorList() {
        return (LinearLayout) getView().findViewById(R.id.ll_selector_settting_hive);
    }

    public LinearLayout getEditSetting() {
        return (LinearLayout) getView().findViewById(R.id.ll_edit_settting_hive);
    }

    public DrowsyRelativeLayoutCell getBtnChangePass() {
        return (DrowsyRelativeLayoutCell) getView().findViewById(R.id.rl_change_hive_pass_field);
    }

    public DrowsyRelativeLayoutCell getBtnChangeAlias() {
        return (DrowsyRelativeLayoutCell) getView().findViewById(R.id.rl_change_hive_alias_field);
    }

    public DrowsyRelativeLayoutCell getBtnChangeMethod() {
        return (DrowsyRelativeLayoutCell) getView().findViewById(R.id.rl_change_hive_method_field);
    }

    public DrowsyRelativeLayoutCell getBtnMessage() {
        return (DrowsyRelativeLayoutCell) getView().findViewById(R.id.rl_hive_messagefield);
    }

    public SettingEditRowCell getEdtField1() {
        return (SettingEditRowCell) getView().findViewById(R.id.edit_field_1);
    }

    public SettingEditRowCell getEdtField2() {
        return (SettingEditRowCell) getView().findViewById(R.id.edit_field_2);
    }

    public SettingEditRowCell getEdtField3() {
        return (SettingEditRowCell) getView().findViewById(R.id.edit_field_3);
    }

    public Button getSaveBtn() {
        return (Button) getView().findViewById(R.id.saveButton);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.xa_hive_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBtnChangePass().setTitle("Change Hive Password");
        getBtnChangeAlias().setTitle("Change Hive Alias");
        getBtnChangeMethod().setTitle("Change Hive Joining Method");
        getBtnMessage().setTitle("Hive Message");
        setState(SettingState.SELECTOR);

        getBtnChangePass().setOnClickListener(this);
        getBtnChangeAlias().setOnClickListener(this);
        getBtnChangeMethod().setOnClickListener(this);
        getBtnMessage().setOnClickListener(this);
        getSaveBtn().setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        HiveListMainFragment mainFragment = (HiveListMainFragment) getParentFragment();
        switch (view.getId()) {
            case (R.id.rl_change_hive_pass_field):
                setState(SettingState.CHANGEPASS);
                mainFragment.getNavigationBar().configNavBarWithTitleAndLeftButton(
                        getString(R.string.change_hive_password).toLowerCase(), getString(R.string.settings_title), this);
                getSeletorList().setVisibility(View.GONE);
                getEditSetting().setVisibility(View.VISIBLE);
                getEdtField1().setVisibility(View.VISIBLE);
                getEdtField2().setVisibility(View.VISIBLE);
                getEdtField3().setVisibility(View.VISIBLE);
                getEdtField1().setTitle("Current Password");
                getEdtField1().getEditText().setText("");
                getEdtField2().setTitle("New Password");
                getEdtField2().getEditText().setText("");
                getEdtField3().setTitle("Confirm New Password");
                getEdtField3().getEditText().setText("");
                getSaveBtn().setVisibility(View.VISIBLE);
                break;
            case (R.id.rl_change_hive_alias_field):
                setState(SettingState.CHANGEALIAS);
                mainFragment.getNavigationBar().configNavBarWithTitleAndLeftButton(
                        getString(R.string.change_hive_alias).toLowerCase(), getString(R.string.settings_title).toLowerCase(), this);
                getSeletorList().setVisibility(View.GONE);
                getEditSetting().setVisibility(View.VISIBLE);
                getEdtField1().setTitle("Current Alias");
                getEdtField1().getEditText().setText("");
                getEdtField2().setTitle("New Alias");
                getEdtField2().getEditText().setText("");
                getEdtField3().setVisibility(View.GONE);
                getSaveBtn().setVisibility(View.VISIBLE);
                break;
            case (R.id.rl_change_hive_method_field):
                setState(SettingState.CHANGEMETHOD);
//                mainFragment.getNavigationBar().configNavBarWithTitleAndLeftButton(
//                        getString(R.string.change_hive_joining).toLowerCase(), getString(R.string.settings_title).toLowerCase(), this);
//                getSeletorList().setVisibility(View.GONE);
//                getEditSetting().setVisibility(View.VISIBLE);
//                getSaveBtn().setVisibility(View.VISIBLE);
                break;
            case (R.id.rl_hive_messagefield):
                setState(SettingState.MESSAGE);
//                mainFragment.getNavigationBar().configNavBarWithTitleAndLeftButton(
//                        getString(R.string.change_hive_alias).toLowerCase(), getString(R.string.settings_title).toLowerCase(), this);
//                getSeletorList().setVisibility(View.GONE);
//                getEditSetting().setVisibility(View.VISIBLE);
                break;
            case (R.id.left_nav_button):
                if (getState() != SettingState.SELECTOR) {
                    mainFragment.getNavigationBar().configNavBarWithTitleAndLeftButton(
                            getString(R.string.change_hive_alias).toLowerCase(), getString(R.string.back_text), this);
                    setState(SettingState.SELECTOR);
                    getSeletorList().setVisibility(View.VISIBLE);
                    getEditSetting().setVisibility(View.GONE);
                    getSaveBtn().setVisibility(View.GONE);
                } else {
                    mainFragment.setLayoutHiveList(false);
                }
                //onAddButton();
                break;
            case (R.id.saveButton):
                if (getState() == SettingState.CHANGEALIAS) {
                    String currentAlias = Helpers.extractStringFromTextView(getEdtField1().getEditText());
                    String alias = Helpers.extractStringFromTextView(getEdtField2().getEditText());
                    if (currentAlias != null && alias != null) {
                        if (mainFragment.getCurrentHive().getName().equals(currentAlias)) {
                            getBusyIndicator(R.string.change_hive_alias).show();
                            ChangeAliasHiveRequest aliasHive = new ChangeAliasHiveRequest();
                            aliasHive.setId(String.valueOf(mainFragment.getCurrentHive().getHiveId()));
                            aliasHive.setName(alias);
                            aliasHive.setOwnerId(String.valueOf(Model.getInstance().getCurrentUser().getUserId()));
                            Model.getInstance().changeAliasHive(aliasHive);
                        } else {
                            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
                        }
                    } else {
                        SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_text), null);
                    }
                } else if (getState() == SettingState.CHANGEPASS) {

                }
                break;

        }

    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.CONTACT_UPDATE) {
            getBusyIndicator().dismiss();

        } else if (evt == ModelEvent.CONTACT_UPDATE_FAILED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.error),
                    getString(R.string.failed_add_hive),
                    null
            );
        } else if (evt == ModelEvent.CREATE_HIVE) {
            getBusyIndicator().dismiss();

            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.success),
                    getString(R.string.add_hive_success),
                    null
            );

        }
    }

    public SettingState getState() {
        return state;
    }

    public void setState(SettingState state) {
        this.state = state;
    }
}

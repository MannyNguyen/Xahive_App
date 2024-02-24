package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.lang.reflect.Field;

import ca.xahive.app.bl.objects.HiveListItem;
import ca.xahive.app.bl.objects.api_object.DeleteHiveRequest;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;

public class HiveListMainFragment extends BaseFragment implements View.OnClickListener {

    public static HiveListMainFragment _instance;
    public HiveListItem currentHive;

    public NavigationBar getNavigationBar() {
        return (NavigationBar) getView().findViewById(R.id.navBarAddHive);
    }

    private View getFragmentListContactView() {
        View fragment = (View) getView().findViewById(R.id.FRAGMENT_PLACEHOLDER);
        return fragment;
    }

    public static HiveListMainFragment getInstance() {
        if (_instance == null) {
            _instance = new HiveListMainFragment();
        }
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_hive_main_list, container, false);
    }

    private static View view;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        HiveListFragment contactListFragment = (HiveListFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.hiveListFragment);
//
//        if (contactListFragment != null)
//            getFragmentManager().beginTransaction().remove(contactListFragment).commit();
//        Log.i("aaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaa");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLayoutHiveList(true);

        getNavigationBar().setVisibility(View.VISIBLE);

    }

    public void setLayoutHiveList(boolean isFirstShow) {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        if (!isFirstShow) {
            childFragTrans.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
        }
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, HiveListFragment.getInstance());
        childFragTrans.commit();

        getNavigationBar().invisibleLeftMenu();
        getNavigationBar().configNavBarWithTitleAndRightButton(
                getString(R.string.hive_titile),
                getString(R.string.create).toLowerCase(), R.drawable.white_plus, this);
    }

    public void setLayoutCreateHive() {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, HiveAddFragment.getInstance());
        childFragTrans.commit();
        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.create_hive),
                getString(R.string.back_text), this);

        // getNavigationBar().visibleRightMenu();
    }

    public void setLayoutSettingHive() {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, HiveSettingFragment.getInstance());
        childFragTrans.commit();
        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(currentHive.getName() + " " +
                        getString(R.string.settings_title),
                getString(R.string.back_text), this);

        // getNavigationBar().visibleRightMenu();
    }

    public void setLayouJoinHive() {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, HiveJoinFragment.getInstance());
        childFragTrans.commit();
        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(currentHive.getName(),
                getString(R.string.back_text), this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.right_nav_button):
                setLayoutCreateHive();
                // onAddButton();
                break;
            case (R.id.left_nav_button):
                setLayoutHiveList(false);
                //onAddButton();
                break;
        }
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        // Nothing to do, yet.
        //Log.v("Contact" , String.valueOf(evt));
    }

    /**
     * public void onAddButton() {
     * AlertDialog.Builder inputIDAlert = new AlertDialog.Builder(getActivity());
     * inputIDAlert.setTitle(getString(R.string.add_contact));
     * inputIDAlert.setMessage(getString(R.string.enter_user_id));
     * <p/>
     * final EditText editText = new EditText(getActivity());
     * editText.setInputType(InputType.TYPE_CLASS_NUMBER);
     * inputIDAlert.setView(editText);
     * <p/>
     * inputIDAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
     * public void onClick(DialogInterface dialog, int whichButton) {
     * String userIDString = editText.getText().toString();
     * <p/>
     * int userIDInt = 0;
     * <p/>
     * try {
     * userIDInt = Integer.parseInt(userIDString);
     * <p/>
     * if (userIDInt != Model.getInstance().getCurrentUser().getUserId()) {
     * addContact(Model.getInstance().getUserInfoCache().userWithId(userIDInt));
     * }
     * } catch (NumberFormatException exception) {
     * XADebug.d("Failed to parse user ID string.");
     * }
     * <p/>
     * Helpers.hideSoftKeyboardForEditText(editText);
     * }
     * });
     * <p/>
     * inputIDAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
     * public void onClick(DialogInterface dialog, int whichButton) {
     * Helpers.hideSoftKeyboardForEditText(editText);
     * }
     * });
     * <p/>
     * inputIDAlert.show();
     * }
     **/
    public void addContact(User user) {
        if (!user.isAnonymous()) {
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(true);
            Model.getInstance().updateUserRelationship(userRelationship);

            //reload the contact fragment list
            ContactListFragment contactListFragment = (ContactListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
            //contactListFragment.reloadList();
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
        }
    }

    public HiveListItem getCurrentHive() {
        return currentHive;
    }

    public void setCurrentHive(HiveListItem currentHive) {
        this.currentHive = currentHive;
    }


}

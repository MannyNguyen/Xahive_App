package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;

/**
 * Created by Hoan on 10/15/2015.
 */
public class ContactListMainFragment extends BaseFragment implements View.OnClickListener {

    public static ContactListMainFragment _instance ;

    public NavigationBar getNavigationBar() {
        return (NavigationBar) getView().findViewById(R.id.navBarAddHive);
    }

    private View getFragmentListContactView() {
        View fragment = (View) getView().findViewById(R.id.FRAGMENT_PLACEHOLDER);
        return fragment;
    }

    public static ContactListMainFragment getInstance(){
        if(_instance == null){
            _instance = new ContactListMainFragment();
        }
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_contact_main_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLayoutContactList(true);

//        if (Model.getInstance().getUserHivesList() == null) {
//            Model.getInstance().reloadUserHivesList();
//        }
//        getNavigationBar().setVisibility(View.VISIBLE);
//           getListView().setBackgroundColor(getResources().getColor(R.color.xa_light_grey));

    }

    public void setLayoutContactList(boolean isFirstShow) {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        if (!isFirstShow) {
            childFragTrans.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
        }
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, ContactListFragment.getInstance());
        childFragTrans.commit();

        getNavigationBar().invisibleLeftMenu();
        getNavigationBar().configNavBarWithTitleAndRightButton(
                getString(R.string.contacts_title),
                getString(R.string.add_nav_button_title).toLowerCase(), R.drawable.white_plus, this);
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.contacts_title), getString(R.string.blocked_list), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLayoutBlockList();
                    }
                });
    }

    private void setLayoutBlockList()
    {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, ContactBlockListFragment.getInstance());
        childFragTrans.commit();

        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.blocked_list_title),
                getString(R.string.back_text),R.drawable.icon_arrow_left, this);

    }

    private void setLayoutAddContact() {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, ContactAddFragment.getInstance());
        childFragTrans.commit();

        getNavigationBar().invisibleRightMenu();

        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.add_contact),
                getString(R.string.back_text), this);

        // getNavigationBar().visibleRightMenu();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.right_nav_button):
                setLayoutAddContact();
                // onAddButton();
                break;
            case (R.id.left_nav_button):
                setLayoutContactList(false);
                //onAddButton();
                break;
        }
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        // Nothing to do, yet.
        //Log.v("Contact" , String.valueOf(evt));
    }


    /**8
     public void onAddButton() {
     AlertDialog.Builder inputIDAlert = new AlertDialog.Builder(getActivity());
     inputIDAlert.setTitle(getString(R.string.add_contact));
     inputIDAlert.setMessage(getString(R.string.enter_user_id));

     final EditText editText = new EditText(getActivity());
     editText.setInputType(InputType.TYPE_CLASS_NUMBER);
     inputIDAlert.setView(editText);

     inputIDAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int whichButton) {
     String userIDString = editText.getText().toString();

     int userIDInt = 0;

     try {
     userIDInt = Integer.parseInt(userIDString);

     if (userIDInt != Model.getInstance().getCurrentUser().getUserId()) {
     addContact(Model.getInstance().getUserInfoCache().userWithId(userIDInt));
     }
     } catch (NumberFormatException exception) {
     XADebug.d("Failed to parse user ID string.");
     }

     Helpers.hideSoftKeyboardForEditText(editText);
     }
     });

     inputIDAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int whichButton) {
     Helpers.hideSoftKeyboardForEditText(editText);
     }
     });

     inputIDAlert.show();
     }**/

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
}

package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.xahive.app.ui.activities.myapp.R;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

/**
 * Created by Hoan on 10/16/2015.
 */
public class ContactAddFragment extends AddHandlerFragment {
    public static ContactAddFragment _instance ;

    public static ContactAddFragment getInstance(){
        if(_instance == null){
            _instance = new ContactAddFragment();
        }
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String editTitle = getResources().getString(R.string.enter_user_id);
        String nameBtn = getActivity().getResources().getString(R.string.add_nav_button_title);
        configView(editTitle, nameBtn);
    }

    @Override
    protected void actionAdd(){
        super.actionAdd();
        String userIDString = getEditText().getText().toString();

        int userIDInt = 0;

        try {
            userIDInt = Integer.parseInt(userIDString);

            if (userIDInt != Model.getInstance().getCurrentUser().getUserId()) {
                addContact(Model.getInstance().getUserInfoCache().userWithId(userIDInt));
            }
        } catch (NumberFormatException exception) {
            XADebug.d("Failed to parse user ID string.");
        }

    }
    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.CONTACT_UPDATE) {
            getBusyIndicator().dismiss();

            /** SimpleAlertDialog.showMessageWithOkButton(
             getActivity(),
             getString(R.string.success),
             getString(R.string.add_contact_success),
             null
             );**/

        } else if (evt == ModelEvent.CONTACT_UPDATE_FAILED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.error),
                    getString(R.string.failed_add_contact),
                    null
            );
        }
        else if(evt == ModelEvent.ADD_CONTACT_UPDATE)
        {
            getBusyIndicator().dismiss();

            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.success),
                    getString(R.string.add_contact_success),
                    null
            );
            if(getParentFragment() instanceof  ContactListMainFragment)
            {
                getEditText().setText("");
                ((ContactListMainFragment) getParentFragment()).setLayoutContactList(false);
            }

        }
    }

    public void addContact(User user) {
        if(!user.isAnonymous()) {

            getBusyIndicator(R.string.adding).show();
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(true);
            Model.getInstance().updateUserRelationship(userRelationship);

            //reload the contact fragment list
            //ContactListFragment contactListFragment = (ContactListFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
            //contactListFragment.reloa();
        }else{
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
        }
    }


}

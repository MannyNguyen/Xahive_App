package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserRelationshipsModelItem;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.adapters.ContactBlockListAdapter;
import ca.xahive.app.ui.adapters.ContactListAdapter;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuCreator;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

public class ContactBlockListFragment extends ListHandlerFragment {
    ContactBlockListAdapter contactListAdapter;
    ArrayList<UserRelationship> contacts = null;
    public static ContactBlockListFragment _instance;

    public static ContactBlockListFragment getInstance() {
        if (_instance == null) {
            _instance = new ContactBlockListFragment();
        }
        return _instance;
    }

    public ContactBlockListAdapter getContactListAdapter() {
        if (contactListAdapter == null) {
            contactListAdapter = new ContactBlockListAdapter(getActivity(), R.layout.xa_contact_list_cell, getContacts(), getInstance());
            getListView().setAdapter(contactListAdapter);
            contactListAdapter.notifyDataSetChanged();
        }
        return contactListAdapter;
    }


    public void setContactListAdapter(ContactBlockListAdapter contactListAdapter) {
        this.contactListAdapter = contactListAdapter;
    }

    public ArrayList<UserRelationship> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<UserRelationship>();
        }
        return contacts;
    }

    public void setContacts(ArrayList<UserRelationship> contacts) {
        getContacts().clear();
        getContacts().addAll(contacts);
        getContactListAdapter().notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactListAdapter = null;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListViewOnClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().getUserRelationships().addObserver(this, true);
//        if (Model.getInstance().getUserRelationships() == null) {
//            Model.getInstance().reloadUserRelationships();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Model.getInstance().getUserRelationships().deleteObserver(this);
        Model.getInstance().getUserRelationships().setState(ModelState.STALE);
    }

    @Override
    protected String getEmptyListMessage() {
        return getString(R.string.no_contacts);
    }

    @Override
    protected int getListCount() {
        return getContacts().size();
    }

    @Override
    protected void reloadList() {
        Model.getInstance().reloadUserRelationships();
    }

    @Override
    protected void setDataForList(ModelItem modelItem) {
        UserRelationshipsModelItem userRelationshipsModelItem = Model.getInstance().getUserRelationships();
        setContacts(userRelationshipsModelItem.getBlockedUsersList());
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {

        super.onModelUpdated(evt);

        if (evt == ModelEvent.CONTACT_UPDATE) {
            UserRelationshipsModelItem userRelationshipsModelItem = Model.getInstance().getUserRelationships();
            setContacts(userRelationshipsModelItem.getBlockedUsersList());

        } else if (evt == ModelEvent.LOGIN_FAILED) {

        } else if (evt == ModelEvent.ADD_CONTACT_UPDATE) {
            getBusyIndicator().dismiss();
            reloadList();
        }
    }

    public void unblockContact(UserRelationship user) {
        if (!user.isContact()) {
            getBusyIndicator(R.string.remove_processing).show();
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(true);
            userRelationship.setBlockedBuzz(false);
            userRelationship.setOtherUserId(user.getOtherUserId());
            userRelationship.setBlockedMessages(false);
            Model.getInstance().updateUserRelationship(userRelationship);
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getErrorView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadUserRelationships();
            }
        });

        getEmptyListView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadUserRelationships();
            }
        });
    }

}

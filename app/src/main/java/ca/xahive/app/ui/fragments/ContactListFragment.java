package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.ArrayList;

import ca.xahive.app.bl.local.TabBarManager;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.TabBarManagerActivity;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserRelationshipsModelItem;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.adapters.ContactListAdapter;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuCreator;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

public class ContactListFragment extends ListHandlerFragment {

    ContactListAdapter contactListAdapter;
    ArrayList<UserRelationship> contacts = null;
    public static ContactListFragment _instance;

    public static ContactListFragment getInstance() {
        if (_instance == null) {
            _instance = new ContactListFragment();
        }
        return _instance;
    }

    public ContactListAdapter getContactListAdapter() {
        if (contactListAdapter == null) {
            contactListAdapter = new ContactListAdapter(getActivity(), R.layout.xa_contact_list_cell, getContacts());
            getListView().setAdapter(contactListAdapter);
            contactListAdapter.notifyDataSetChanged();
        }
        return contactListAdapter;
    }

    public void setContactListAdapter(ContactListAdapter contactListAdapter) {
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
    protected void setupListViewOnClickListener() {
        super.setupListViewOnClickListener();
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                createSwipeMenuItem(menu, R.drawable.white_ban, "contact_ban");
                createSwipeMenuItem(menu, R.drawable.white_trash, "contact_del");

            }
        };

        getListView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        blockContact(getContacts().get(position));
                        //removeContact(getContacts().get(position));
                        break;
                    case 1:
                        removeContact(getContacts().get(position));
                        break;
                }
                return false;
            }
        });
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getActivity() instanceof TabBarManagerActivity) {
                    ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
                    //for (int j = 0; j < conversationList.conversation.size(); j++) {
                    //   Conversation conversation = conversationList.conversation.get(i);
                    //   if (conversation.getOtherUserId() == getContacts().get(i).getOtherUserId()) {
                    //       if (i < getContacts().size()) {
                    UserRelationship userRelationship = getContacts().get(i);
                    ((TabBarManagerActivity) getActivity()).nagationToMessageWithUserID(userRelationship.getOtherUserId());
                    //       }

                    //  }
                    //}
                }
                /**
                 for(int i=0;i< ( (ConversationList) Model.getInstance().getConversationList().getData()).getConversations().size(); i++) {
                 Conversation conversation = ( (ConversationList) Model.getInstance().getConversationList().getData()).getConversations().get(i);

                 }**/
            }
        });
        getListView().setMenuCreator(creator);

        /**
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        HiveListItem hiveListItem = (HiveListItem) getListView().getItemAtPosition(position);

        commitHiveChange(hiveListItem.getHiveId());
        }
        });**/
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getEditTextSearch().setInputType(InputType.TYPE_CLASS_NUMBER);
        setupListViewOnClickListener();

    }


    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().getUserRelationships().setState(ModelState.STALE);
        Model.getInstance().getUserRelationships().addObserver(this, true);
        getLoadingView().setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
        Helpers.hideSoftKeyboardForEditText(getEditTextSearch());


//        if (Model.getInstance().getUserRelationships() == null) {
//            Model.getInstance().reloadUserRelationships();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Model.getInstance().getUserRelationships().deleteObserver(this);
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
        setContacts(userRelationshipsModelItem.getContactsList());
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);
        getLoadingView().setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
        if (evt == ModelEvent.CONTACT_UPDATE) {
            // reloadList();
            UserRelationshipsModelItem userRelationshipsModelItem = Model.getInstance().getUserRelationships();
            setContacts(userRelationshipsModelItem.getContactsList());

        } else if (evt == ModelEvent.LOGIN_FAILED) {

        } else if (evt == ModelEvent.ADD_CONTACT_UPDATE) {
            getBusyIndicator().dismiss();
            reloadList();
        }
    }

    public void removeContact(UserRelationship user) {
        if (user.isContact()) {

            getBusyIndicator(R.string.remove_processing).show();
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(false);
            userRelationship.setBlockedBuzz(false);
            userRelationship.setOtherUserId(user.getOtherUserId());
            userRelationship.setBlockedMessages(false);

            Model.getInstance().updateUserRelationship(userRelationship);
            //reload the contact fragment list
            //ContactListFragment contactListFragment = (ContactListFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
            //contactListFragment.reloa();
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
        }
    }

    public void blockContact(UserRelationship user) {
        if (user.isContact()) {

            getBusyIndicator(R.string.remove_processing).show();
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(false);
            userRelationship.setBlockedBuzz(false);
            userRelationship.setOtherUserId(user.getOtherUserId());
            userRelationship.setBlockedMessages(true);

            Model.getInstance().updateUserRelationship(userRelationship);
            //reload the contact fragment list
            //ContactListFragment contactListFragment = (ContactListFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
            //contactListFragment.reloa();
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

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        getContactListAdapter().getFilter().filter(s);

    }

}

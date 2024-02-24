package ca.xahive.app.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.utils.ChatStarter;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.NavigationBar;

/**
 * Created by Hoan on 10/15/2015.
 */
public class MessagesListMainFragment extends BaseFragment implements View.OnClickListener {

    public static MessagesListMainFragment _instance;
    public static final String ID_CONTACT = "ID_CONTACT";

    public NavigationBar getNavigationBar() {
        return (NavigationBar) getView().findViewById(R.id.navBarAddHive);
    }

    public View getFragmentListContactView() {
        View fragment = (View) getView().findViewById(R.id.FRAGMENT_PLACEHOLDER);
        return fragment;
    }

    public static MessagesListMainFragment getInstance() {
        if (_instance == null) {
            _instance = new MessagesListMainFragment();
        }
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_contact_main_list, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLayoutContactList(true);

//        if (Model.getInstance().getUserHivesList() == null) {
//            Model.getInstance().reloadUserHivesList();
//        }
//        getNavigationBar().setVisibility(View.VISIBLE);
//           getListView().setBackgroundColor(getResources().getColor(R.color.xa_light_grey));

    }

    private void setLayoutContactList(boolean isFirstShow) {
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        if (!isFirstShow) {
            childFragTrans.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
        }
        final MessagesListFragment frag = new MessagesListFragment();
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, frag);
        childFragTrans.commit();

        getNavigationBar().invisibleLeftMenu();
        getNavigationBar().configNavBarWithTitleAndRightButton(
                getString(R.string.messages_title),
                getString(R.string.add_nav_button_title).toLowerCase(), R.drawable.white_plus, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLayoutMessage(null);
                    }
                });
        if (isNavigationToChatMessage) {
            //for (Fragment fragment : mFragment.getChildFragmentManager().getFragments()) {
            //    if(fragment instanceof MessagesListFragment)
            //    {
            navigationConversationWithContact(idContactToNavigation);
            isNavigationToChatMessage = false;
            idContactToNavigation = -1;
            //      return;
            //  }
            //}

        }
    }

    public void navigationConversationWithContact(int idContact) {
        ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
        for (int i = 0; i < conversationList.conversation.size(); i++) {
            Conversation conversation = conversationList.conversation.get(i);
            if (conversation.getOtherUserId() == idContact) {
                //Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
                ParceledInteger parceledInteger = ChatStarter.parceledInteger(conversation);
                setLayoutMessage(parceledInteger);
            }
        }
        setLayoutMessageWithContactID(idContact);

    }

    private void setLayoutAddContact() {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        MessagesAddFragment frag = new MessagesAddFragment();
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, frag);
        childFragTrans.commit();

        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.new_message),
                getString(R.string.back_text), this);

        getNavigationBar().configNavBarWithTitleAndRightButton(
                getString(R.string.new_message),
                getString(R.string.new_mess), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        // getNavigationBar().visibleRightMenu();
    }

    private static boolean isNavigationToChatMessage = false;
    private static int idContactToNavigation = -1;

    public void nagationChatView(int idContact) {
        idContactToNavigation = idContact;
        isNavigationToChatMessage = true;

    }

    public void attachButtonPressed(Message message) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof MessagesChatNewFragment) {
                ((MessagesChatNewFragment) fragment).attachButtonPressed(message);
                return;
            }
        }
    }

    public void setLayoutMessage(ParceledInteger parceledInteger) {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);

        MessagesChatNewFragment frag = new MessagesChatNewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Parcelator.PARCELATOR_KEY, parceledInteger);
        frag.setArguments(bundle);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, frag);
        childFragTrans.commit();

        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.new_message),
                getString(R.string.back_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        setLayoutContactList(false);
                    }
                });

        // getNavigationBar().visibleRightMenu();
    }


    public void setLayoutMessageWithContactID(int idContactToNavigation) {

        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        childFragTrans.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);

        MessagesChatNewFragment frag = new MessagesChatNewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ID_CONTACT, idContactToNavigation);
        frag.setArguments(bundle);
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, frag);
        childFragTrans.commit();
        getNavigationBar().invisibleRightMenu();
        getNavigationBar().configNavBarWithTitleAndLeftButton(
                getString(R.string.new_message),
                getString(R.string.back_text), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        setLayoutContactList(false);
                    }
                });

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }

        }
    }

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

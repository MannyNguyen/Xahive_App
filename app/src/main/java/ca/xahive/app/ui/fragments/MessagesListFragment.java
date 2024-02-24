package ca.xahive.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.utils.ChatStarter;
import ca.xahive.app.bl.utils.PollingTimer;
import ca.xahive.app.bl.utils.PollingTimerDelegate;
import ca.xahive.app.ui.adapters.ConversationListAdapter;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuCreator;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

/**
 * Created by Hoan on 10/12/2015.
 */
public class MessagesListFragment extends ListHandlerFragment implements PollingTimerDelegate {
    ConversationListAdapter convoAdapter;
    ArrayList<Conversation> conversationList = null;
    private PollingTimer timer;

    public ConversationListAdapter getConvoAdapter() {
        if (convoAdapter == null) {
            convoAdapter = new ConversationListAdapter(
                    getActivity(),
                    R.layout.xa_contact_list_cell,
                    getConversationList()
            );
            getListView().setAdapter(convoAdapter);
            convoAdapter.notifyDataSetChanged();
        }
        return convoAdapter;
    }

    public ArrayList<Conversation> getConversationList() {
        if (conversationList == null) {
            conversationList = new ArrayList<Conversation>();
        }
        return conversationList;
    }

    public void navigationConversationWithContact(int idContact) {
        ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
        for (int i = 0; i < conversationList.conversation.size(); i++) {
            Conversation conversation = conversationList.conversation.get(i);
            if (conversation.getOtherUserId() == idContact) {
                //Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
                ParceledInteger parceledInteger = ChatStarter.parceledInteger(conversation);
                ((MessagesListMainFragment) getParentFragment()).setLayoutMessage(parceledInteger);
            }
        }
        ((MessagesListMainFragment) getParentFragment()).setLayoutMessageWithContactID(idContact);

    }

    public void setConversationList(ArrayList<Conversation> conversationList) {
        getConversationList().clear();
        ArrayList<Conversation> conversationArrayList = new ArrayList<>();
        for (int i = 0; i < conversationList.size(); i++) {
            UserRelationship relationship = null;
            Conversation conversationTemp = conversationList.get(i);
            if (conversationTemp.getFromUserId() == Model.getInstance().getCurrentUser().getUserId()) {
                relationship = Model.getInstance().getUserRelationships().getRelationshipForUserId(conversationTemp.getToUserId());
            } else {
                relationship = Model.getInstance().getUserRelationships().getRelationshipForUserId(conversationTemp.getFromUserId());
            }
            if (relationship != null) {
                if (!relationship.isBlockedMessages() || !relationship.isBlockedAtAll()) {
                    conversationArrayList.add(conversationTemp);
                }
            } else {
                conversationArrayList.add(conversationTemp);
            }
            // if(conversationTemp.getFromUserId()== Model.getInstance().getUserRelationships().getRelationshipForUserId());

        }
        getConversationList().addAll(conversationArrayList);
        getConvoAdapter().notifyDataSetChanged();

        getLoadingView().setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        convoAdapter = null;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public AutoCompleteTextView getEditTextSearch() {
        return (AutoCompleteTextView) getView().findViewById(R.id.control_search_Input);
    }

    /**
     * public ImageButton getClearSearch()
     * {
     * return (ImageButton) getView().findViewById(R.id.btnClear);
     * }
     **/

    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().getConversationList().setState(ModelState.STALE);
        Model.getInstance().getConversationList().addObserver(this, true);

        getLoadingView().setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
        if (Model.getInstance().getConversationList() == null) {
            Model.getInstance().reloadConversationList();
        }
        setupListViewOnClickListener();
        /**
         getClearSearch().setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        getEditTextSearch().setText("");
        }
        });**/

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
                ParceledInteger parceledInteger = ChatStarter.parceledInteger(conversation);
                MessagesListMainFragment frag = (MessagesListMainFragment) getParentFragment();
                frag.setLayoutMessage(parceledInteger);
            }
        });
        timer = new PollingTimer(this, 0);
        boolean instantRefresh = (Model.getInstance().getConversationList().getAge() > timer.getInterval());
        timer.start(instantRefresh);
        Helpers.hideSoftKeyboardForEditText(getEditTextSearch());


        if (!UserDefaults.getAdvertsDisabled()) {
            /**
             AdView adView = (AdView) getActivity().findViewById(R.id.adView);
             AdRequest adRequest = new AdRequest.Builder()
             .build();
             adView.loadAd(adRequest);
             adView.setVisibility(View.VISIBLE);**/
        }
    }

    @Override
    public void onPause() {
        timer.stop();
        timer = null;
        Model.getInstance().getConversationList().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);

        getConvoAdapter().getFilter().filter(s);
    }

    protected void setupListViewOnClickListener() {
        super.setupListViewOnClickListener();
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                createSwipeMenuItem(menu, R.drawable.white_trash, "Clear Data");

            }
        };
        getListView().setMenuCreator(creator);
        getListView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        break;
                    case 1:
                        //Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
                        //ChatStarter.startChatWithUser(getActivity(), conversation);
                        break;
                    case 2:
                        break;
                }
                return false;
            }
            /*8
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, int index) {
                switch (index) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                //Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
               // ChatStarter.startChatWithUser(getActivity(), conversation);
            }**/
        });
    }


    @Override
    protected String getEmptyListMessage() {
        return getString(R.string.no_conversations);
    }

    @Override
    protected int getListCount() {
        return getConversationList().size();
    }

    @Override
    protected void reloadList() {
        Model.getInstance().reloadConversationList();
    }

    @Override
    protected void setDataForList(ModelItem modelItem) {

        ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
        setConversationList(conversationList.getConversations());
    }

    @Override
    public void timerDidFire(PollingTimer timer) {
        reloadList();
    }

//    public void newButtonClicked() {
//        Intent intent = new Intent(getActivity(), StartChatListActivity.class);
//        startActivityForResult(intent, ChatStarter.CONVERSATION_REQUEST);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ChatStarter.openConversationScreenWithActivityResult(requestCode, resultCode, data, getActivity());
    }
}

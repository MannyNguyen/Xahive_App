package ca.xahive.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.utils.ChatStarter;
import ca.xahive.app.bl.utils.PollingTimer;
import ca.xahive.app.bl.utils.PollingTimerDelegate;
import ca.xahive.app.ui.adapters.ConversationListAdapter;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuCreator;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuItem;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

/**
 * Created by Hoan on 10/12/2015.
 */
public class MessagesFragment extends ListHiveHandlerFragment implements PollingTimerDelegate {

    ConversationListAdapter convoAdapter;
    ArrayList<Conversation> conversationList = null;
    private PollingTimer timer;

    public ConversationListAdapter getConvoAdapter() {
        if (convoAdapter == null) {
            convoAdapter = new ConversationListAdapter(
                    getActivity(),
                    R.layout.xa_conversation_list_cell,
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

    public void setConversationList(ArrayList<Conversation> conversationList) {
        getConversationList().clear();
        getConversationList().addAll(conversationList);
        getConvoAdapter().notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        setupListViewOnClickListener();
        Model.getInstance().reloadConversationList();
        getErrorView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadConversationList();
            }
        });
        getListView().setDividerHeight(1);
        getEmptyListView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadConversationList();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        convoAdapter = null;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().getConversationList().addObserver(this, true);
        timer = new PollingTimer(this, 0);
        boolean instantRefresh = (Model.getInstance().getConversationList().getAge() > timer.getInterval());
        timer.start(instantRefresh);
        if(!UserDefaults.getAdvertsDisabled()) {
            // AdView adView = (AdView) getActivity().findViewById(R.id.adView);
            //AdRequest adRequest = new AdRequest.Builder()
            //       .build();
            //adView.loadAd(adRequest);
            //adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        timer.stop();
        timer = null;

        Model.getInstance().getConversationList().deleteObserver(this);

        super.onPause();
    }

    private void setupListViewOnClickListener() {
        getListView().setClickable(true);
        /**
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Conversation conversation = (Conversation) getListView().getItemAtPosition(position);
        ChatStarter.startChatWithUser(getActivity(), conversation);
        }
        });**/
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                createMenu(menu);
            }
        };

        getListView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        break;
                }
                return false;
            }
        });
        getListView().setMenuCreator(creator);

    }
    private void createMenu(SwipeMenu menu) {
        SwipeMenuItem setting = new SwipeMenuItem(
                getActivity().getApplicationContext());

        setting.setBackground(R.color.xa_dark_green);
        setting.setWidth(dp2px(64));
        setting.setIcon(R.drawable.white_trash);
        setting.setTitle("Delete");
        menu.addMenuItem(setting);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getActivity().getResources().getDisplayMetrics());
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

    public void newButtonClicked() {
        //  Intent intent = new Intent(getActivity(), StartChatListActivity.class);
        //  startActivityForResult(intent, ChatStarter.CONVERSATION_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ChatStarter.openConversationScreenWithActivityResult(requestCode, resultCode, data, getActivity());
    }
}

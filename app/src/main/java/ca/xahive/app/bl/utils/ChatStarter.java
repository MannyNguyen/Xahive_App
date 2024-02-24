package ca.xahive.app.bl.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.ArrayList;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.ui.activities.ChatView;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.fragments.MessagesChatNewFragment;
import ca.xahive.app.ui.fragments.MessagesListFragment;

public class ChatStarter {
    public static final int CONVERSATION_REQUEST = 65432;

    public static void startChatWithUser(Activity ctx, int userId) {
        Conversation conversation = new Conversation();
        conversation.setToUserId(userId);

        ConversationList conversationListObject = (ConversationList) Model.getInstance().getConversationList().getData();

        if (conversationListObject != null) {
            ArrayList<Conversation> conversationArrayList = conversationListObject.getConversations();

            for (Conversation aConversation : conversationArrayList) {
                int otherUserID = aConversation.getOtherUserId();

                if (otherUserID == userId) {
                    conversation = aConversation;
                    break;
                }
            }
        }

        startChatWithUser(ctx, conversation);
    }


    public static void startChatWithUser(FragmentTransaction childFragTrans, Conversation conversation) {
        //Intent intent = new Intent(ctx, ChatView.class);

        ParceledInteger parceledInteger = Model.getInstance().getParcelator().createParcelForObject(conversation);
        //intent.putExtra(Parcelator.PARCELATOR_KEY, parceledInteger);
        //ctx.startActivity(intent);
        MessagesChatNewFragment frag = new MessagesChatNewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Parcelator.PARCELATOR_KEY, parceledInteger);
        frag.setArguments(bundle);
        childFragTrans.add(R.id.FRAGMENT_PLACEHOLDER, frag);
        childFragTrans.commit();

    }
    public  static  ParceledInteger parceledInteger (Conversation conversation)
    {
         return  Model.getInstance().getParcelator().createParcelForObject(conversation);

    }

    public static void startChatWithUser(Activity ctx, Conversation conversation) {
        //Intent intent = new Intent(ctx, ChatView.class);

        ParceledInteger parceledInteger = Model.getInstance().getParcelator().createParcelForObject(conversation);
        //intent.putExtra(Parcelator.PARCELATOR_KEY, parceledInteger);
        //ctx.startActivity(intent);
    }

    public static void openConversationScreenWithActivityResult(int requestCode, int resultCode, Intent data, Activity ctx) {
        if(data != null) {
            if (requestCode == CONVERSATION_REQUEST) {
                ParceledInteger parceledInteger = data.getParcelableExtra(Parcelator.PARCELATOR_KEY);

                Integer userId = (Integer) Model.getInstance().getParcelator().getObjectForParcel(parceledInteger);

                // We wouldn't need this check here, but onActivityResult is being called twice,
                // for some unclear reason. The real solution is to use iOS,
                // but sadly, we are stuck using Android here.
                if (userId != null) {
                    ChatStarter.startChatWithUser(ctx, userId);
                }
            }
        }
    }
}

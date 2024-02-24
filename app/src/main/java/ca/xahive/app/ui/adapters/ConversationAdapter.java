package ca.xahive.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.ui.cells.ChatMessageCell;


public class ConversationAdapter extends ArrayAdapter<PersonalMessage> {

    int resource;

    public ConversationAdapter(Context context, int resource, List<PersonalMessage> personalMessages) {
        super(context, resource, personalMessages);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMessageCell messageView;

        PersonalMessage personalMessage = getItem(position);

        //inflate the item's view
        if(convertView == null){
            messageView = new ChatMessageCell(getContext());
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resource, messageView, true);
        } else {
            messageView = (ChatMessageCell)convertView;
        }

        messageView.setPersonalMessage(personalMessage);

        return messageView;
    }


}

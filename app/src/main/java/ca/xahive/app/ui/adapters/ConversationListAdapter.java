package ca.xahive.app.ui.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.cells.ConversationListCell;

public class ConversationListAdapter extends ArrayAdapter<Conversation> implements Filterable {

    int resource;
    List<Conversation> conversations;
    List<Conversation> mConversations;

    public ConversationListAdapter(Context context, int resource, List<Conversation> conversations) {
        super(context, resource, conversations);
        this.resource = resource;
        this.conversations = conversations;
        this.mConversations = conversations;
    }

    @Override
    public int getCount() {

        return conversations.size();
    }

    //Get the data item associated with the specified position in the data set.
    @Override
    public Conversation getItem(int position) {

        return conversations.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ConversationListCell convoView;

        Conversation convo = conversations.get(position);

        //inflate the item's view
        if(convertView == null){
            convoView = new ConversationListCell(getContext());
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resource, convoView, true);
        } else {
            convoView = (ConversationListCell)convertView;
        }
        convoView.getBtnImage().setVisibility(View.GONE);
        convoView.setConvo(convo);

        return convoView;
    }
    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                conversations = (List<Conversation>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    List<Conversation> FilteredArrayNames = new ArrayList<>();

                    // perform your search here using the searchConstraint String.

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mConversations.size(); i++) {
                        String userId = String.valueOf(mConversations.get(i).getOtherUserId());
                        if (userId.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrayNames.add(mConversations.get(i));
                        }
                    }

                    results.count = FilteredArrayNames.size();
                    results.values = FilteredArrayNames;
                } else {
                    results.count = mConversations.size();
                    results.values = mConversations;
                }
                return results;
            }
        };

        return filter;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}

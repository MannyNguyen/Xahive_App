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

import ca.xahive.app.bl.objects.HiveListItem;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.cells.ContactListCell;


public class ContactListAdapter extends ArrayAdapter<UserRelationship> implements Filterable {
    int resource;
    List<UserRelationship> contactsList;
    List<UserRelationship> mcontactsList;

    public ContactListAdapter(Context context, int resource, List<UserRelationship> contactsList) {
        super(context, resource, contactsList);
        this.resource = resource;
        this.mcontactsList = contactsList;
        this.contactsList = contactsList;
    }

    @Override
    public int getCount() {

        return contactsList.size();
    }

    //Get the data item associated with the specified position in the data set.
    @Override
    public UserRelationship getItem(int position) {

        return contactsList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactListCell contactListCell;

        UserRelationship userRelationship = contactsList.get(position);

        if (convertView == null) {
            contactListCell = new ContactListCell(getContext());

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resource, contactListCell, true);
        } else {
            contactListCell = (ContactListCell) convertView;
        }

        contactListCell.setUserRelationship(userRelationship);
        contactListCell.getBtnImage().setVisibility(View.GONE);

        return contactListCell;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactsList = (List<UserRelationship>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    List<UserRelationship> FilteredArrayNames = new ArrayList<>();

                    // perform your search here using the searchConstraint String.

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mcontactsList.size(); i++) {
                        String userId = String.valueOf(mcontactsList.get(i).getOtherUserId());
                        if (userId.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrayNames.add(mcontactsList.get(i));
                        }
                    }

                    results.count = FilteredArrayNames.size();
                    results.values = FilteredArrayNames;
                } else {
                    results.count = mcontactsList.size();
                    results.values = mcontactsList;
                }
                return results;
            }
        };

        return filter;
    }
}

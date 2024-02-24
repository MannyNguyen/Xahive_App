package ca.xahive.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.ui.cells.ContactListCell;
import ca.xahive.app.ui.fragments.BaseFragment;
import ca.xahive.app.ui.fragments.ContactBlockListFragment;
import ca.xahive.app.ui.fragments.HiveListMainFragment;

/**
 * Created by prosoft on 10/28/15.
 */
public class ContactBlockListAdapter extends ArrayAdapter<UserRelationship> {
    int resource;
    BaseFragment fragment;

    public ContactBlockListAdapter(Context context, int resource, List<UserRelationship> contactsList, BaseFragment fragment ) {
        super(context, resource, contactsList);
        this.resource = resource;
        this.fragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactListCell contactListCell;

        final UserRelationship userRelationship = getItem(position);

        if (convertView == null) {
            contactListCell = new ContactListCell(getContext());

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resource, contactListCell, true);
        } else {
            contactListCell = (ContactListCell) convertView;
        }

        contactListCell.setUserRelationship(userRelationship);

        contactListCell.getBtnImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userRelationship.isBlockedMessages()) {
                    ContactBlockListFragment frag = (ContactBlockListFragment)fragment;
                    frag.unblockContact(userRelationship);
                }
            }
        });

        return contactListCell;
    }
}

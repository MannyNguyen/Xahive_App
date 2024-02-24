package ca.xahive.app.ui.adapters;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ca.xahive.app.bl.objects.HiveListItem;
import ca.xahive.app.ui.cells.HiveCell;
import ca.xahive.app.ui.fragments.BaseFragment;
import ca.xahive.app.ui.fragments.HiveListMainFragment;


public class HiveListAdapter extends ArrayAdapter<HiveListItem> implements Filterable {
    int resource;
    Context context;
    BaseFragment fragment;
    List<HiveListItem> hiveList;
    List<HiveListItem> mhiveList;

    public HiveListAdapter(Context context, int resource, List<HiveListItem> hiveList, BaseFragment fragment) {
        super(context, resource, hiveList);
        this.resource = resource;
        this.fragment = fragment;
        this.hiveList = hiveList;
        this.mhiveList =hiveList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HiveCell hiveCell;

        final HiveListItem hiveListItem = hiveList.get(position);

        if (convertView == null) {
            hiveCell = new HiveCell(getContext());

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(resource, hiveCell, true);
        } else {
            hiveCell = (HiveCell) convertView;
        }

        hiveCell.setHiveListItem(hiveListItem);

        hiveCell.getBtnImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HiveListMainFragment frag = (HiveListMainFragment) fragment;
                frag.setCurrentHive(hiveListItem);
                frag.setLayouJoinHive();
            }
        });

        return hiveCell;
    }

    @Override
    public int getCount() {

        return hiveList.size();
    }

    //Get the data item associated with the specified position in the data set.
    @Override
    public HiveListItem getItem(int position) {

        return hiveList.get(position);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                hiveList = (List<HiveListItem>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    List<HiveListItem> FilteredArrayNames = new ArrayList<>();

                    // perform your search here using the searchConstraint String.

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mhiveList.size(); i++) {
                        String dataNames = mhiveList.get(i).getName();
                        if (dataNames.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrayNames.add(mhiveList.get(i));
                        }
                    }

                    results.count = FilteredArrayNames.size();
                    results.values = FilteredArrayNames;
                } else {
                    results.count = mhiveList.size();
                    results.values = mhiveList;
                }
                return results;
            }
        };

        return filter;
    }
}

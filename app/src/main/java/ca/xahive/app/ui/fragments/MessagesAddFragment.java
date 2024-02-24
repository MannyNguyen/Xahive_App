package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.xahive.app.ui.activities.myapp.R;

/**
 * Created by Hoan on 10/20/2015.
 */
public class MessagesAddFragment extends AddHandlerFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String editTitle = getResources().getString(R.string.enter_name_message);
        String nameBtn = getActivity().getResources().getString(R.string.add_nav_button_title);
        configView(editTitle, nameBtn);

    }

    @Override
    protected void actionAdd() {
        // add something
    }
}

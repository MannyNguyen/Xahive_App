package ca.xahive.app.ui.fragments;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;


public class BaseFragment extends Fragment implements Observer {


    protected ProgressDialog busyIndicator;

    // UI components
    protected ProgressDialog getBusyIndicator() {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(getActivity(), getString(R.string.logging_in_message));
        }
        return busyIndicator;
    }

    protected ProgressDialog getBusyIndicator(int resString) {
        //if (busyIndicator == null) {
        busyIndicator = SimpleAlertDialog.createBusyIndicator(getActivity(), getString(resString));
        /// }
        return busyIndicator;
    }

    protected ProgressDialog getBusyIndicator(String string) {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(getActivity(), string);
        }
        return busyIndicator;
    }

    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().addObserver(this);
        Helpers.hideSoftKeyboardInActivity(getActivity());
    }

    @Override
    public void onPause() {
        Model.getInstance().deleteObserver(this);
        super.onPause();
    }

    public void update(Observable observable, Object o) {
        if (observable == Model.getInstance()) {
            onModelUpdated((ModelEvent) o);
        }
    }

    protected void onModelUpdated(ModelEvent evt) {
        // Nothing to do, yet.
    }
}

package ca.xahive.app.ui.activities;

import android.app.ProgressDialog;
import android.support.v4.app.FragmentActivity;

import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

/**
 * Created by trantung on 10/5/15.
 */
public class BaseActivity  extends FragmentActivity implements Observer {
    private boolean isVisible;

    protected void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    protected boolean isVisible() {
        return isVisible;
    }
    protected ProgressDialog busyIndicator;
    // UI components
    protected ProgressDialog getBusyIndicator() {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(this, getString(R.string.logging_in_message));
        }
        return busyIndicator;
    }
    protected ProgressDialog getBusyIndicator(int resString) {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(this, getString(resString));
        }
        return busyIndicator;
    }
    protected ProgressDialog getBusyIndicator(String string) {
        if (busyIndicator == null) {
            busyIndicator = SimpleAlertDialog.createBusyIndicator(this, string);
        }
        return busyIndicator;
    }

    protected void showMessageWithEditText(int string, int cancel, int accept, Runnable onCancel, Runnable onAccept) {
        SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(this, null, getString(string), getString(cancel).toUpperCase(), getString(accept).toUpperCase(), onCancel , onAccept);
    }

    @Override
    public void onResume(){
        super.onResume();
        setIsVisible(true);
        Model.getInstance().addObserver(this);
    }

    @Override
    public void onPause() {
        setIsVisible(false);
        Model.getInstance().deleteObserver(this);
        super.onPause();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable == Model.getInstance() && isVisible()) {
           onModelUpdated((ModelEvent)o);
        }
    }
    protected void onModelUpdated(ModelEvent evt) {
        // Nothing to do, yet.
    }
}

package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.api_object.CreateHiveRequest;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

/**
 * Created by prosoft on 10/28/15.
 */
public class HiveJoinFragment extends AddHandlerFragment {

    private static HiveJoinFragment _instance;

    public static HiveJoinFragment getInstance() {
        if (_instance == null) {
            _instance = new HiveJoinFragment();
        }
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String editTitle = getResources().getString(R.string.enter_hive_join);
        String nameBtn = getActivity().getResources().getString(R.string.joinHeader);
        configView(editTitle, nameBtn);
        getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.CONTACT_UPDATE) {
            getBusyIndicator().dismiss();

        } else if (evt == ModelEvent.CONTACT_UPDATE_FAILED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.error),
                    getString(R.string.failed_add_hive),
                    null
            );
        } else if (evt == ModelEvent.CREATE_HIVE) {
            getBusyIndicator().dismiss();

            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.success),
                    getString(R.string.add_hive_success),
                    null
            );

        }
    }

    @Override
    protected void actionAdd() {


    }

    public void addHive(CreateHiveRequest user) {
        Model.getInstance().createHive(user);

    }
}

package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.api_object.CreateHiveRequest;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;

/**
 * Created by Hoan on 10/16/2015.
 */
public class HiveAddFragment extends AddHandlerFragment {

    private static HiveAddFragment _instance;

    public static HiveAddFragment getInstance() {
        if (_instance == null) {
            _instance = new HiveAddFragment();
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
        String editTitle = getResources().getString(R.string.name);
        String nameBtn = getActivity().getResources().getString(R.string.create);
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
            if(getParentFragment() instanceof  HiveListMainFragment)
            {
                getEditText().setText("");
                ((HiveListMainFragment) getParentFragment()).setLayoutHiveList(false);
            }

        }
    }

    @Override
    protected void actionAdd() {
        super.actionAdd();
        getBusyIndicator(R.string.processing).show();

        CreateHiveRequest hiveRequest = new CreateHiveRequest();
        hiveRequest.name = getEditText().getText().toString();
        hiveRequest.ownerId = Model.getInstance().getCurrentUser().getUserId();
        hiveRequest.description = "";
        hiveRequest.publicKey = Helpers.getEncryptionRSA();
        addHive(hiveRequest);

    }

    public void addHive(CreateHiveRequest user) {
        Model.getInstance().createHive(user);

    }
}

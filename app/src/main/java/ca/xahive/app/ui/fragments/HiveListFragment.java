package ca.xahive.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.CurrentUser;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.objects.api_object.DeleteHiveRequest;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.HivesList;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.HiveListItem;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.ui.adapters.HiveListAdapter;
import ca.xahive.app.ui.dialogs.ConfirmPassDialog;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenu;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuCreator;
import ca.xahive.app.ui.views.swipelistmenu.SwipeMenuListView;

public class HiveListFragment extends ListHandlerFragment {
    HiveListAdapter hiveListAdapter;
    ArrayList<HiveListItem> hiveList = null;
    HiveListMainFragment mainFragment;

    private Context context;
    public static HiveListFragment _instance;

    public static HiveListFragment getInstance() {
        if (_instance == null) {
            _instance = new HiveListFragment();
        }
        return _instance;
    }

    public HiveListAdapter getHiveListAdapter() {
        if (hiveListAdapter == null) {
            hiveListAdapter = new HiveListAdapter(
                    getActivity(),
                    R.layout.xa_hive_list_cell,
                    getHiveList(), (HiveListMainFragment) getParentFragment()
            );
            getListView().setAdapter(hiveListAdapter);
            hiveListAdapter.notifyDataSetChanged();
        }
        return hiveListAdapter;
    }

    public ArrayList<HiveListItem> getHiveList() {
        if (hiveList == null) {
            hiveList = new ArrayList<HiveListItem>();
        }
        return hiveList;
    }

    public void setHiveList(ArrayList<HiveListItem> hiveList) {
        getHiveList().clear();
        getHiveList().addAll(hiveList);
        getHiveListAdapter().notifyDataSetChanged();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getErrorView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadUserHivesList();
            }
        });

        getEmptyListView().getErrorViewRetryButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().reloadUserHivesList();
            }
        });
        Helpers.hideSoftKeyboardForEditText(getEditTextSearch());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hiveListAdapter = null;
        mainFragment = (HiveListMainFragment) getParentFragment();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void setupListViewOnClickListener() {
        super.setupListViewOnClickListener();
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                createSwipeMenuItem(menu, R.drawable.icon_setting, "Clear Data");
                createSwipeMenuItem(menu, R.drawable.icon_discussion, "discussion");
                createSwipeMenuItem(menu, R.drawable.white_trash, "Clear Data");

            }
        };

        getListView().setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                FragmentManager fm = getChildFragmentManager();
                Bundle bundle = new Bundle();
                ConfirmPassDialog dialog2 = new ConfirmPassDialog();
                switch (index) {
                    case 0:
                        mainFragment.setCurrentHive(hiveList.get(position));
                        mainFragment.setLayoutSettingHive();
                        break;
                    case 1:
                        mainFragment.setCurrentHive(hiveList.get(position));
                        bundle.putInt("STATE", ConfirmPassDialog.DialogState.ALERT.getValue());
                        dialog2.setArguments(bundle);
                        dialog2.show(fm, "fragment_edit_name");
                        break;
                    case 2:
                        mainFragment.setCurrentHive(hiveList.get(position));
                        bundle.putInt("STATE", ConfirmPassDialog.DialogState.CONFIRMPASS.getValue());
                        dialog2.setArguments(bundle);
                        dialog2.show(fm, "fragment_edit_name");
                        break;
                }
                return false;
            }
        });
        getListView().setMenuCreator(creator);

        /**
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        HiveListItem hiveListItem = (HiveListItem) getListView().getItemAtPosition(position);

        commitHiveChange(hiveListItem.getHiveId());
        }
        });**/
    }

    @Override
    public void onStart() {
        super.onStart();
        // getNavigationBar().setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Model.getInstance().getUserHivesList().setState(ModelState.STALE);
        Model.getInstance().getUserHivesList().addObserver(this, true);

        getLoadingView().setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        Model.getInstance().getUserHivesList().deleteObserver(this);
    }

    @Override
    protected String getEmptyListMessage() {
        return getString(R.string.no_hives);
    }

    @Override
    protected int getListCount() {
        return getHiveList().size();
    }

    @Override
    protected void reloadList() {
        Model.getInstance().reloadUserHivesList();
    }

    @Override
    protected void setDataForList(ModelItem modelItem) {
        HivesList hiveList = (HivesList) Model.getInstance().getUserHivesList().getData();
        setHiveList(hiveList.getHiveListItems());

    }

    public void commitHiveChange(int hiveID) {
        if (hiveID == Model.getInstance().getHiveId()) {
            getActivity().finish();
            return;
        }

        SimpleAlertDialog.showMessageWithOkButton(
                getActivity(),
                getString(R.string.changing_hive),
                getString(R.string.ok),
                null
        );

        Model.getInstance().setHiveId(hiveID);
    }

    public void removeHive(String password) {
        if (password != null && Model.getInstance().getCurrentUser().getPassword().equals(password)) {
            if (!mainFragment.currentHive.isDefault()) {
                getBusyIndicator(R.string.remove_processing).show();
                DeleteHiveRequest delHive = new DeleteHiveRequest();
                delHive.setId(String.valueOf(mainFragment.currentHive.getHiveId()));
                delHive.setEncryptedPassword(Model.getInstance().getCurrentUser().getEncryptedPassword());
                delHive.setOwnerId(String.valueOf(Model.getInstance().getCurrentUser().getUserId()));
                Model.getInstance().deleteHive(delHive);

            } else {
                SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
            }
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.wrong_password), null);

        }
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        getLoadingView().setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
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
    public void onTextChanged(CharSequence cs, int start, int before, int count) {
        super.onTextChanged(cs, start, before, count);
        getHiveListAdapter().getFilter().filter(cs);

    }

}

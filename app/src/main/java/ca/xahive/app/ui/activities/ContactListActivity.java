package ca.xahive.app.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.fragments.BaseFragment;
import ca.xahive.app.ui.fragments.ContactListFragment;
import ca.xahive.app.ui.views.NavigationBar;

public class ContactListActivity extends BaseFragment {

    public NavigationBar getNavigationBar() {
        return (NavigationBar) getView().findViewById(R.id.navBar);
    }

    public NavigationBar getNavigationBarAddContact() {
        return (NavigationBar) getView().findViewById(R.id.navBarAddContact);
    }

    public LinearLayout getAddContactView() {
        return (LinearLayout) getActivity().findViewById(R.id.xa_layout_AddView);
    }


    public ListView getListView() {
        return (ListView) getView().findViewById(R.id.listHandlerFragmentListView);
    }

    public EditText getContactIDField() {
        return (EditText) getActivity().findViewById(R.id.idContactField);
    }

    public Button getAddContactButton() {
        return (Button) getActivity().findViewById(R.id.addButton);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.activity_contact_list, container, false);
        } catch (InflateException e) {
        }
        return view;
        //return inflater.inflate(R.layout.activity_contact_list, container, false);
    }

    private static View view;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ContactListFragment contactListFragment = (ContactListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);

        if (contactListFragment != null)
            getFragmentManager().beginTransaction().remove(contactListFragment).commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLayoutContactList();

        if (Model.getInstance().getUserRelationships() == null) {
            Model.getInstance().reloadUserRelationships();
        }

        //getListView().setBackgroundColor(getResources().getColor(R.color.xa_light_grey));

    }

    private void setLayoutContactList() {
        getAddContactView().setVisibility(View.GONE);
        getNavigationBar().setVisibility(View.VISIBLE);
        getFragmentListContactView().setVisibility(View.VISIBLE);
        getNavigationBar().configNavBarWithTitleAndRightButton(
                getString(R.string.contacts),
                getString(R.string.add_nav_button_title),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLayoutAddContact();
                        // onAddButton();
                    }
                }
        );

        ContactListFragment contactListFragment = (ContactListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);

        if (contactListFragment != null) {

            contactListFragment.getContactListAdapter().notifyDataSetChanged();
        }
    }

    private void setLayoutAddContact() {
        getAddContactView().setVisibility(View.VISIBLE);
        getFragmentListContactView().setVisibility(View.GONE);
        getNavigationBarAddContact().configNavBarWithTitleAndLeftButton(
                getString(R.string.add_contact),
                getString(R.string.back_text),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLayoutContactList();
                        //onAddButton();
                    }
                }
        );
        getAddContactButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userIDString = getContactIDField().getText().toString();

                int userIDInt = 0;

                try {
                    userIDInt = Integer.parseInt(userIDString);

                    if (userIDInt != Model.getInstance().getCurrentUser().getUserId()) {
                        addContact(Model.getInstance().getUserInfoCache().userWithId(userIDInt));
                    }
                } catch (NumberFormatException exception) {
                    XADebug.d("Failed to parse user ID string.");
                }

                Helpers.hideSoftKeyboardForEditText(getContactIDField());
            }
        });


        // getNavigationBar().visibleRightMenu();
    }

    private View getFragmentListContactView() {
        View fragment = (View) getView().findViewById(R.id.contactListFragment);
        return fragment;
    }

    public void onAddButton() {
        AlertDialog.Builder inputIDAlert = new AlertDialog.Builder(getActivity());
        inputIDAlert.setTitle(getString(R.string.add_contact));
        inputIDAlert.setMessage(getString(R.string.enter_user_id));

        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputIDAlert.setView(editText);

        inputIDAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String userIDString = editText.getText().toString();

                int userIDInt = 0;

                try {
                    userIDInt = Integer.parseInt(userIDString);

                    if (userIDInt != Model.getInstance().getCurrentUser().getUserId()) {
                        addContact(Model.getInstance().getUserInfoCache().userWithId(userIDInt));
                    }
                } catch (NumberFormatException exception) {
                    XADebug.d("Failed to parse user ID string.");
                }

                Helpers.hideSoftKeyboardForEditText(editText);
            }
        });

        inputIDAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Helpers.hideSoftKeyboardForEditText(editText);
            }
        });

        inputIDAlert.show();
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

        if (evt == ModelEvent.CONTACT_UPDATE) {
            getBusyIndicator().dismiss();

            /** SimpleAlertDialog.showMessageWithOkButton(
             getActivity(),
             getString(R.string.success),
             getString(R.string.add_contact_success),
             null
             );**/

        } else if (evt == ModelEvent.CONTACT_UPDATE_FAILED) {
            getBusyIndicator().dismiss();
            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.error),
                    getString(R.string.failed_add_contact),
                    null
            );
        } else if (evt == ModelEvent.ADD_CONTACT_UPDATE) {
            getBusyIndicator().dismiss();

            SimpleAlertDialog.showMessageWithOkButton(
                    getActivity(),
                    getString(R.string.success),
                    getString(R.string.add_contact_success),
                    null
            );
            setLayoutContactList();
        }
    }

    public void addContact(User user) {
        if (!user.isAnonymous()) {

            getBusyIndicator(R.string.adding).show();
            UserRelationship userRelationship = UserRelationship.relationshipWithUserId(user.getUserId());
            userRelationship.setContact(true);
            Model.getInstance().updateUserRelationship(userRelationship);

            //reload the contact fragment list
            //ContactListFragment contactListFragment = (ContactListFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.contactListFragment);
            //contactListFragment.reloa();
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.no_user_with_ID_found), null);
        }
    }

}

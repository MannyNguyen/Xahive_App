package ca.xahive.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Observable;

import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;

/**
 * Created by Hoan on 10/16/2015.
 */
public class AddHandlerFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.xa_hive_add_new, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getButton().setOnClickListener(this);
    }

    protected EditText getEditText() {
        return (EditText) getView().findViewById(R.id.edt_field);
    }

    protected Button getButton() {
        return (Button) getView().findViewById(R.id.btn_field);
    }

    protected View[] getAllViews() {
        return new View[]{getEditText(), getButton()};
    }

    protected void configView(String editTitle, String nameBtn) {
        getEditText().setHint(editTitle);
        getButton().setText(nameBtn);
    }

    @Override
    public void update(Observable observable, Object object) {
        super.update(observable, object);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btn_field):
                actionAdd();
                break;
        }
    }

    protected void actionAdd() {
        Helpers.hideSoftKeyboardForEditText(getEditText());

    }
}

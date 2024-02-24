package ca.xahive.app.ui.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.fragments.HiveListFragment;
import ca.xahive.app.ui.fragments.HiveListMainFragment;

/**
 * Created by prosoft on 10/30/15.
 */
public class ConfirmPassDialog extends DialogFragment implements
        android.view.View.OnClickListener {

    int state;
    HiveListFragment fragment;

    public enum DialogState {
        CONFIRMPASS(0),
        ALERT(1);

        private final int value;

        DialogState(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
        }

    public ConfirmPassDialog() {
    }

    private TextView getBtnCancle() {
        return (TextView) getDialog().findViewById(R.id.tv_cancel);
    }

    private TextView getBtnDone() {
        return (TextView) getDialog().findViewById(R.id.tv_done);
    }

    private TextView getBtnOk() {
        return (TextView) getDialog().findViewById(R.id.tv_ok);
    }

    private EditText getEdtConfirm(){
        return (EditText) getDialog().findViewById(R.id.et_confirm_pass);
    }

    private TextView getTitle() {
        return (TextView) getDialog().findViewById(R.id.tv_title);
    }

    private RelativeLayout getBtm1() {
        return (RelativeLayout) getDialog().findViewById(R.id.rl_btm1);
    }

    private RelativeLayout getBtm2() {
        return (RelativeLayout) getDialog().findViewById(R.id.rl_btm2);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        fragment = (HiveListFragment) getParentFragment();
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        state = bundle.getInt("STATE");
        switch (state) {
            case 0:
                getBtm1().setVisibility(View.VISIBLE);
                getBtm2().setVisibility(View.GONE);
                getEdtConfirm().setVisibility(View.VISIBLE);
                break;
            case 1:
                getTitle().setText(getString(R.string.alert_join_hive));
                getEdtConfirm().setVisibility(View.GONE);
                getBtm2().setVisibility(View.VISIBLE);
                getBtm1().setVisibility(View.GONE);
                break;
        }
        getBtnCancle().setOnClickListener(this);
        getBtnDone().setOnClickListener(this);
        getBtnOk().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_done:
                String password = Helpers.extractStringFromTextView(getEdtConfirm());
                fragment.removeHive(password);
            case R.id.tv_ok:
                dismiss();
                break;
            default:
                break;
        }
    }
}

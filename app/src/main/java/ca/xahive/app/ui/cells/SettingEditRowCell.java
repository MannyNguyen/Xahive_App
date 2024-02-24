package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.ui.activities.myapp.R;

/**
 * Created by Hoan on 10/23/2015.
 */
public class SettingEditRowCell extends LinearLayout {

    private boolean isCellDisabled;

    public SettingEditRowCell(Context context) {
        super(context);

    }

    public SettingEditRowCell(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public boolean isCellDisabled() {
        return isCellDisabled;
    }

    public void setCellDisabled(boolean isCellDisabled) {
        this.isCellDisabled = isCellDisabled;
        setEnabled(!isCellDisabled);

        setChildrenEnabled(this, !isCellDisabled);
    }

    public void setChildrenEnabled(ViewGroup viewGroup, boolean isEnabled) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = getChildAt(i);

            if (view != null) {
                view.setEnabled(isEnabled);
            }

            if (view instanceof ViewGroup) {
                setChildrenEnabled((ViewGroup) view, isEnabled);
            }
        }
    }

    public TextView getTitleView() {
        return (TextView) findViewById(R.id.aliasLabel);
    }

    public EditText getEditText() {
        return (EditText)findViewById(R.id.oldField);
    }

    public void setTitle(String title) {
        getTitleView().setText(title);
    }

    private void loadViews() {
        FontHelper.getInstance(getContext()).setCustomFont(getTitleView(), FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
    }

    public String getText(){
        return getEditText().getText().toString();
    }
}

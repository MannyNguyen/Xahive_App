package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.ui.activities.myapp.R;

public class DrowsyRelativeLayoutCell extends RelativeLayout {
    private boolean isCellDisabled;

    public boolean isCellDisabled() {
        return isCellDisabled;
    }

    public void setCellDisabled(boolean isCellDisabled) {
        this.isCellDisabled = isCellDisabled;
        setEnabled(!isCellDisabled);

        setChildrenEnabled(this, !isCellDisabled);
    }

    public DrowsyRelativeLayoutCell(Context context) {
        super(context);
    }

    public DrowsyRelativeLayoutCell(Context context, AttributeSet attrs) {
        super(context, attrs);

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
        return (TextView) findViewById(R.id.tv_title);
    }

    public void setTitle(String title) {
        getTitleView().setText(title);
    }

    private void loadViews() {
        FontHelper.getInstance(getContext()).setCustomFont(getTitleView(), FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
    }
}
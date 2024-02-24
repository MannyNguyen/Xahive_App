package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.utils.FontHelper;

public class DistanceCell extends DrowsyRelativeLayoutCell {

    //properties
    public TextView getMinDistanceLabel() {
        return (TextView) findViewById(R.id.minDistanceLabel);
    }

    public TextView getMaxDistanceLabel() {
        return (TextView) findViewById(R.id.maxDistanceLabel);
    }

    public SeekBar getSlider() {
        return (SeekBar) findViewById(R.id.distanceSeekBar);
    }

    public DistanceCell(Context context) {
        super(context);
    }

    public DistanceCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate () {
        super.onFinishInflate();

        FontHelper.getInstance(getContext()).setCustomFont(getMinDistanceLabel(), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
        FontHelper.getInstance(getContext()).setCustomFont(getMaxDistanceLabel(), FontHelper.CustomFontEnum.XAHTabBarLabelFont);
    }
}

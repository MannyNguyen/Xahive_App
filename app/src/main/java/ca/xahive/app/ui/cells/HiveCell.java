package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.objects.HiveListItem;

public class HiveCell extends RelativeLayout {
    private HiveListItem hiveListItem;

    public HiveListItem getHiveListItem() {
        return hiveListItem;
    }

    public void setHiveListItem(HiveListItem hiveListItem) {
        this.hiveListItem = hiveListItem;
        updateCell();
    }
    public ImageButton getBtnImage(){
        return (ImageButton) findViewById(R.id.btn_image);
    }

    public TextView getCellTextView() {
        return (TextView) findViewById(R.id.cellTextView);
    }

    public ImageView getCheckmarkView(){
        return (ImageView)findViewById(R.id.cellCheckmark);
    }

    public HiveCell(Context context) {
        super(context);
    }

    public HiveCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updateCell() {
        getCellTextView().setText(getHiveListItem().getName());

        boolean isHiveID = getHiveListItem().getHiveId() == Model.getInstance().getHiveId();

        //getCheckmarkView().setVisibility(isHiveID ? VISIBLE : INVISIBLE);
    }
}
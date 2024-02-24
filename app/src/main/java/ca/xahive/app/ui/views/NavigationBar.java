package ca.xahive.app.ui.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.utils.FontHelper;

public class NavigationBar extends FrameLayout {

    private TextView titleView;
    private RelativeLayout leftButton;
    private RelativeLayout rightButton;
    private TextView backTextView;
    private TextView forwardTextView;
    private ImageView leftImageView;
    private ImageView rightImageView;

    public NavigationBar(Context context) {
        super(context);
        loadViews();
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.xa_navigation_bar_layout, this);

        loadViews();
    }

    private void loadViews() {
        titleView = (TextView) findViewById(R.id.nav_bar_title);
        leftButton = (RelativeLayout) findViewById(R.id.left_nav_button);
        rightButton = (RelativeLayout) findViewById(R.id.right_nav_button);
        backTextView = (TextView) findViewById(R.id.back_text_view);
        forwardTextView = (TextView) findViewById(R.id.forward_text_view);
        leftImageView = (ImageView) findViewById(R.id.back_image_view);
        rightImageView = (ImageView) findViewById(R.id.forward_image_view);

        FontHelper.getInstance(getContext()).setCustomFont(titleView, FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
        FontHelper.getInstance(getContext()).setCustomFont(backTextView, FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
        FontHelper.getInstance(getContext()).setCustomFont(forwardTextView, FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
    }

    // public config methods

    public void configNavBarWithTitle(String title) {
        configNavBarWithTitleAndButtons(title, null, null, null, null, null, null);
    }

    public void configNavBarWithTitleAndLeftButton(String title, String leftBtnText, OnClickListener leftBtnListener) {
        configNavBarWithTitleAndButtons(title, leftBtnText, leftBtnListener, null, null);
    }

    public void configNavBarWithTitleAndLeftButton(String title, String leftBtnText, Integer leftResource, OnClickListener leftBtnListener) {
        configNavBarWithTitleAndButtons(title, leftBtnText, leftResource, leftBtnListener, null, null, null);
    }

    public void configNavBarWithTitleAndRightButton(String title, String rightBtnText, OnClickListener rightBtnListener) {
        configNavBarWithTitleAndButtons(title, null, null, rightBtnText, rightBtnListener);
    }

    public void configNavBarWithTitleAndRightButton(String title, String rightBtnText, Integer rightResource, OnClickListener rightBtnListener) {
        configNavBarWithTitleAndButtons(title, null, null, null, rightBtnText, rightResource, rightBtnListener);
    }


    public void updateLeftButton(String leftBtnText, OnClickListener leftBtnListener) {
        backTextView.setText(leftBtnText);
        leftButton.setOnClickListener(leftBtnListener);
        leftButton.setVisibility(VISIBLE);
    }

    public void invisibleLeftMenu() {
        leftButton.setVisibility(GONE);

    }

    public void invisibleRightMenu() {
        rightButton.setVisibility(GONE);

    }

    public void updateRightButton(String rightBtnText, OnClickListener rightBtnListener) {
        forwardTextView.setText(rightBtnText);
        rightButton.setOnClickListener(rightBtnListener);
        rightButton.setVisibility(VISIBLE);
    }

    public void configNavBarWithTitleAndButtons(String title, String leftBtnText, OnClickListener leftBtnListener, String rightBtnText, OnClickListener rightBtnListener) {
        titleView.setText(title);

        if (leftBtnText != null) {
            backTextView.setText(leftBtnText);
            leftButton.setOnClickListener(leftBtnListener);
            leftButton.setVisibility(VISIBLE);
        }

        if (rightBtnText != null) {
            forwardTextView.setText(rightBtnText);
            rightButton.setOnClickListener(rightBtnListener);
            rightButton.setVisibility(VISIBLE);

        }
    }

    public void configNavBarWithTitleAndButtons(String title, String leftBtnText, Integer leftResource, OnClickListener leftBtnListener, String rightBtnText, Integer rightResource, OnClickListener rightBtnListener) {
        titleView.setText(title);

        if (leftBtnText != null) {

            if (leftResource != null) {
                leftImageView.setVisibility(VISIBLE);
                leftImageView.setBackgroundResource(leftResource);
            } else {
                leftImageView.setVisibility(GONE);
            }
            backTextView.setText(leftBtnText);
            leftButton.setOnClickListener(leftBtnListener);
            leftButton.setVisibility(VISIBLE);
        }

        if (rightBtnText != null) {
            if (rightResource != null) {
                rightImageView.setVisibility(VISIBLE);
                rightImageView.setBackgroundResource(rightResource);
            } else {
                rightImageView.setVisibility(GONE);
            }
            forwardTextView.setText(rightBtnText);
            rightButton.setOnClickListener(rightBtnListener);
            rightButton.setVisibility(VISIBLE);

        }
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

}

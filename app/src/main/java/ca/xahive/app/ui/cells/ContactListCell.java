package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Avatar;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.XYDimension;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.UserInfoCacheHelper;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.HexagonImageView;

public class ContactListCell extends RelativeLayout implements Observer {
    UserRelationship userRelationship;
    boolean hideButtons;

    private Avatar avatar;
    private boolean shouldUpdateAvatar = true;

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        // Avoid updating when the avatar is the same as previous.
        if (this.avatar == avatar) {
            return;
        }

        if (this.avatar != null) {
            this.avatar.deleteObserver(this);
        }

        this.avatar = avatar;

        if (this.avatar != null) {
            this.avatar.addObserver(this, true);
        }
    }

    public HexagonImageView getAvatarImageView() {
        return (HexagonImageView) findViewById(R.id.avatarImageView);
    }

    public ImageButton getBtnImage(){
        return (ImageButton) findViewById(R.id.btn_image);
    }

    public TextView getAliasLabel() {
        return (TextView) findViewById(R.id.aliasLabel);
    }

    public UserRelationship getUserRelationship() {
        return userRelationship;
    }

    public void setUserRelationship(UserRelationship userRelationship) {

        int oldUserId = (this.userRelationship != null) ? this.userRelationship.getOtherUserId() : 0;
        int newUserId = (userRelationship != null) ? userRelationship.getOtherUserId() : 0;

        this.userRelationship = userRelationship;

        UserInfoCacheHelper.changeUserObservation(this, oldUserId, newUserId);

        setAvatar(Avatar.avatarWithId(this.userRelationship.getOtherUserId()));

        updateForUser();
    }

    public ContactListCell(Context context) {
        super(context);
    }

    public ContactListCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        FontHelper.getInstance(getContext()).setCustomFont(getAliasLabel(), FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
    }

    public void updateForUser() {
        getAliasLabel().setText(Model.getInstance().getUserInfoCache().userWithId(this.userRelationship.getOtherUserId()).getAlias());
    }

    private void updateAvatar() {
        if (shouldUpdateAvatar && getAvatar() != null) {
            getAvatarImageView().setBitmapToClip(getAvatar().getBitmapWithDimensions(new XYDimension(40, 40)));
        }
    }

    public void onSettingsButton() {
        // Intent intent = new Intent(getContext(), ContactNotificationSettingsActivity.class);
        //ParceledInteger parceledInteger = Model.getInstance().getParcelator().createParcelForObject(getUserRelationship());
        //intent.putExtra(Parcelator.PARCELATOR_KEY, parceledInteger);
        //getContext().startActivity(intent);
    }

    public void onDeleteButton() {
        Runnable onAccepted = new Runnable() {
            @Override
            public void run() {
                UserRelationship relationship = getUserRelationship();
                relationship.setContact(false);
                Model.getInstance().updateUserRelationship(relationship);
            }
        };

        SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(getContext(),
                getContext().getString(R.string.delete_contact),
                getContext().getString(R.string.confirm_remove_user),
                getContext().getString(R.string.cancel),
                getContext().getString(R.string.ok),
                null,
                onAccepted);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        shouldUpdateAvatar = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.userRelationship != null) {
            Model.getInstance().getUserInfoCache().removeObserverForUserId(this, this.userRelationship.getOtherUserId());
            this.userRelationship = null;
        }

        shouldUpdateAvatar = false;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable == Model.getInstance().getUserInfoCache()) {
            updateForUser();
        }

        updateAvatar();
    }
}

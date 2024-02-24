package ca.xahive.app.ui.cells;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Avatar;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.XYDimension;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.UserInfoCacheHelper;
import ca.xahive.app.ui.views.HexagonImageView;

public class ConversationListCell extends LinearLayout implements Observer {
    private Conversation convo;
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
        updateAvatar();
    }

    public ImageButton getBtnImage(){
        return (ImageButton) findViewById(R.id.btn_image);
    }

    public void setConvo(Conversation convo) {
        int oldUserId = (this.convo != null) ? this.convo.getOtherUserId() : 0;
        int newUserId = (convo != null) ? convo.getOtherUserId() : 0;

        this.convo = convo;

        UserInfoCacheHelper.changeUserObservation(this, oldUserId, newUserId);

        convoChanged();
        setAvatar(Avatar.avatarWithId(this.convo.getOtherUserId()));
    }

    public Conversation getConvo() {
        if (convo == null) {
            convo = new Conversation();
        }
        return convo;
    }

    public HexagonImageView getAvatarView() {
        return (HexagonImageView)findViewById(R.id.avatarImageView);
    }

    public TextView getUserNameTextView() {
        return (TextView)findViewById(R.id.aliasLabel);
    }

    public ConversationListCell(Context context) {
        super(context);
    }

    public ConversationListCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate () {
        super.onFinishInflate();

        FontHelper.getInstance(getContext()).setCustomFont(getUserNameTextView(), FontHelper.CustomFontEnum.XAHNavBarTitleAndUserNameLabelFont);
    }

    private void convoChanged() {
         getUserNameTextView().setText(Model.getInstance().getUserInfoCache().userWithId(convo.getOtherUserId()).getAlias());
    }

    public void update(Observable observable, Object data) {
        if (observable == Model.getInstance().getUserInfoCache()) {
            convoChanged();
        }

        updateAvatar();
    }

    @Override
    protected void onAttachedToWindow () {
        super.onAttachedToWindow();
        shouldUpdateAvatar = true;
    }

    protected void onDetachedFromWindow () {
        super.onDetachedFromWindow();
        if (this.convo != null) {
            Model.getInstance().getUserInfoCache().removeObserverForUserId(this, this.convo.getOtherUserId());
            this.convo = null;
            shouldUpdateAvatar = false;
        }
    }

    private void updateAvatar() {
        if (shouldUpdateAvatar && getAvatar() != null) {
            getAvatarView().setBitmapToClip(getAvatar().getBitmapWithDimensions(new XYDimension(40, 40)));
        }
    }

}

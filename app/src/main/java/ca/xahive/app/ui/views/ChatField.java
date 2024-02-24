package ca.xahive.app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import ca.xahive.app.ui.activities.myapp.R;


public class ChatField extends FrameLayout {

    public ChatEditText getChatEditText() {
        return (ChatEditText)findViewById(R.id.chatEditText);
    }

    public ImageButton getLockButton() {
        return (ImageButton)findViewById(R.id.lockButton);
    }
    public ImageButton getAttachButton() {
        return (ImageButton)findViewById(R.id.attachButton);
    }

    public ChatField(Context context) {
        super(context);
    }

    public ChatField(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.xa_chat_field_layout, this);
    }

}
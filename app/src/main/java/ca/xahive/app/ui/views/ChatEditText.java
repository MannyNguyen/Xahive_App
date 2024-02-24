package ca.xahive.app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

// An EditText that lets you use actions ("Done", "Go", etc.) on multi-line edits.
public class ChatEditText extends EditText {

    public interface OnBackButtonListener{
        boolean OnEditTextBackButton();
    }

    OnBackButtonListener _listener;

    public ChatEditText(Context context)
    {
        super(context);
    }

    public ChatEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ChatEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setOnBackButtonListener(OnBackButtonListener l) {
        _listener = l;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (event.getAction()==KeyEvent.ACTION_UP && keyCode==KeyEvent.KEYCODE_BACK)
        {
            if (_listener!=null && _listener.OnEditTextBackButton())
                return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
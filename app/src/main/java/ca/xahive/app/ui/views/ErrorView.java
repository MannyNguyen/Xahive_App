package ca.xahive.app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.xahive.app.ui.activities.myapp.R;

public class ErrorView extends RelativeLayout {
    public ErrorView(Context context) {
        super(context);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView getErrorViewTextView(){
        return (TextView)findViewById(R.id.errorViewTextView);
    }

    public Button getErrorViewRetryButton(){
        return (Button)findViewById(R.id.errorViewRetryButton);
    }
}

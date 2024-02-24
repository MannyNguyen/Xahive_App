package ca.xahive.app.bl.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;
 
public class FontHelper {
    private static FontHelper _instance;

    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private Context context;

    public static FontHelper getInstance(Context context) {
        if (_instance == null) {
            _instance = new FontHelper(context);
        }
        return _instance;
    }

    private FontHelper(Context context) {
        setContext(context);
    }

    public enum CustomFontEnum {
        XAHNavBarButtonAndInputTextFont(0),
        XAHNavBarTitleAndUserNameLabelFont(1),
        XAHButtonAndLoginSignUpLabelFont(2),
        XAHTabBarLabelFont(3),
        XAHTextViewFont(4),
        XAHDetailLabelFont(5),
        XAHTableSectionHeaderFont(6),
        XAHMiscLabelFont(7),
        XAHBuzzCountLabelFont(8);

        private final int value;

        CustomFontEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public void setCustomFont(TextView textView, CustomFontEnum customFontEnum) {
        switch (customFontEnum.getValue()) {
            case 0: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Book.otf"));
                textView.setTextSize(17.0f);
                break;
            }

            case 1: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Book.otf"));
                textView.setTextSize(20.0f);
                break;
            }

            case 2: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Medium.otf"));
                textView.setTextSize(18.5f);
                break;
            }

            case 3: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Book.otf"));
                textView.setTextSize(12.0f);
                break;
            }

            case 4: {
//                textView.setTypeface(Typeface.create()); //TODO: use Helvetica Neue?
                textView.setTextSize(12.5f);
                break;
            }

            case 5: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Medium.otf"));
                textView.setTextSize(10.0f);
                break;
            }

            case 6: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Book.otf"));
                textView.setTextSize(15.0f);
                break;
            }

            case 7: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Medium.otf"));
                textView.setTextSize(17.0f);
                break;
            }

            case 8: {
                textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FuturaStd-Medium.otf"));
                textView.setTextSize(20.0f);
                break;
            }

            default: {
                break;
            }
        }
    }
}

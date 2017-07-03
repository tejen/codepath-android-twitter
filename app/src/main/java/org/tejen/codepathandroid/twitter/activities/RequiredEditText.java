package org.tejen.codepathandroid.twitter.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class RequiredEditText extends android.support.v7.widget.AppCompatEditText {

    public RequiredEditText (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public RequiredEditText (Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public RequiredEditText (Context context) {
        super(context);

    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dispatchKeyEvent(event);
            ((AppCompatActivity) getContext()).finish();
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

}

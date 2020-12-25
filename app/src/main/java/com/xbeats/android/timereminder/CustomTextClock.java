package com.xbeats.android.timereminder;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextClock;

/**
 * Created by Fring on 2020/12/25
 */
public class CustomTextClock extends TextClock {

    private static final String TAG = "CustomTextClock_TAG";
    private static final String StopTicking_FieldName = "mStopTicking";

    public CustomTextClock(Context context) {
        super(context);
    }

    public CustomTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int maxWidth = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w >= maxWidth) {
            Log.d(TAG, String.format("text = %s, w = %d, h = %d", getText(), w, h));
            maxWidth = w;
            setMinimumWidth(maxWidth);
        }
    }

    public void stopTicking() {
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}

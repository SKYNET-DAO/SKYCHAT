package cn.wildfire.imshat.kit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class AutoLinkTextView extends TextView {
    private long time=0;

    public AutoLinkTextView(Context context) {
        super(context);
    }

    public AutoLinkTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            time = System.currentTimeMillis();
        } else if (event.getAction() == MotionEvent.ACTION_UP)
            if (System.currentTimeMillis() - time > 500)
                return true;
        return super.onTouchEvent(event);
    }
}

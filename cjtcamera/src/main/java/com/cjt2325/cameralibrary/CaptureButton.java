package com.cjt2325.cameralibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.cjt2325.cameralibrary.listener.CaptureListener;
import com.cjt2325.cameralibrary.util.CheckPermission;
import com.cjt2325.cameralibrary.util.LogUtil;

import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_BOTH;
import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_ONLY_CAPTURE;
import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_ONLY_RECORDER;



public class CaptureButton extends View {

    private int state;              
    private int button_state;       

    public static final int STATE_IDLE = 0x001;        
    public static final int STATE_PRESS = 0x002;       
    public static final int STATE_LONG_PRESS = 0x003;  
    public static final int STATE_RECORDERING = 0x004; 
    public static final int STATE_BAN = 0x005;         

    private int progress_color = 0xEE16AE16;            
    private int outside_color = 0xEEDCDCDC;             
    private int inside_color = 0xFFFFFFFF;              


    private float event_Y;  


    private Paint mPaint;

    private float strokeWidth;         
    private int outside_add_size;      
    private int inside_reduce_size;     


    private float center_X;
    private float center_Y;

    private float button_radius;            
    private float button_outside_radius;    
    private float button_inside_radius;     
    private int button_size;                

    private float progress;         
    private int duration;           
    private int min_duration;       
    private int recorded_time;      

    private RectF rectF;

    private LongPressRunnable longPressRunnable;    
    private CaptureListener captureLisenter;        
    private RecordCountDownTimer timer;             

    public CaptureButton(Context context) {
        super(context);
    }

    public CaptureButton(Context context, int size) {
        super(context);
        this.button_size = size;
        button_radius = size / 2.0f;

        button_outside_radius = button_radius;
        button_inside_radius = button_radius * 0.75f;

        strokeWidth = size / 15;
        outside_add_size = size / 5;
        inside_reduce_size = size / 8;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        progress = 0;
        longPressRunnable = new LongPressRunnable();

        state = STATE_IDLE;                
        button_state = BUTTON_STATE_BOTH;  
        LogUtil.i("CaptureButtom start");
        duration = 10 * 1000;              
        LogUtil.i("CaptureButtom end");
        min_duration = 1500;              

        center_X = (button_size + outside_add_size * 2) / 2;
        center_Y = (button_size + outside_add_size * 2) / 2;

        rectF = new RectF(
                center_X - (button_radius + outside_add_size - strokeWidth / 2),
                center_Y - (button_radius + outside_add_size - strokeWidth / 2),
                center_X + (button_radius + outside_add_size - strokeWidth / 2),
                center_Y + (button_radius + outside_add_size - strokeWidth / 2));

        timer = new RecordCountDownTimer(duration, duration / 360);    
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(button_size + outside_add_size * 2, button_size + outside_add_size * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setColor(outside_color); 
        canvas.drawCircle(center_X, center_Y, button_outside_radius, mPaint);

        mPaint.setColor(inside_color);  
        canvas.drawCircle(center_X, center_Y, button_inside_radius, mPaint);

        
        if (state == STATE_RECORDERING) {
            mPaint.setColor(progress_color);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            canvas.drawArc(rectF, -90, progress, false, mPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.i("state = " + state);
                if (event.getPointerCount() > 1 || state != STATE_IDLE)
                    break;
                event_Y = event.getY();     
                state = STATE_PRESS;       

                
                if ((button_state == BUTTON_STATE_ONLY_RECORDER || button_state == BUTTON_STATE_BOTH))
                    postDelayed(longPressRunnable, 500);    
                break;
            case MotionEvent.ACTION_MOVE:
                if (captureLisenter != null
                        && state == STATE_RECORDERING
                        && (button_state == BUTTON_STATE_ONLY_RECORDER || button_state == BUTTON_STATE_BOTH)) {
                    
                    captureLisenter.recordZoom(event_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
               
                handlerUnpressByState();
                break;
        }
        return true;
    }

    
    private void handlerUnpressByState() {
        removeCallbacks(longPressRunnable); 
        
        switch (state) {
            
            case STATE_PRESS:
                if (captureLisenter != null && (button_state == BUTTON_STATE_ONLY_CAPTURE || button_state ==
                        BUTTON_STATE_BOTH)) {
                    startCaptureAnimation(button_inside_radius);
                } else {
                    state = STATE_IDLE;
                }
                break;
            
            case STATE_RECORDERING:
                timer.cancel(); 
                recordEnd();    
                break;
        }
    }

    
    private void recordEnd() {
        if (captureLisenter != null) {
            if (recorded_time < min_duration)
                captureLisenter.recordShort(recorded_time);
            else
                captureLisenter.recordEnd(recorded_time);  
        }
        resetRecordAnim(); 
    }

    
    private void resetRecordAnim() {
        state = STATE_BAN;
        progress = 0;       
        invalidate();
        
        startRecordAnimation(
                button_outside_radius,
                button_radius,
                button_inside_radius,
                button_radius * 0.75f
        );
    }

    
    private void startCaptureAnimation(float inside_start) {
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_start * 0.75f, inside_start);
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        inside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                
                captureLisenter.takePictures();
                state = STATE_BAN;
            }
        });
        inside_anim.setDuration(100);
        inside_anim.start();
    }

    
    private void startRecordAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_outside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        AnimatorSet set = new AnimatorSet();
        
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                
                if (state == STATE_LONG_PRESS) {
                    if (captureLisenter != null)
                        captureLisenter.recordStart();
                    state = STATE_RECORDERING;
                    timer.start();
                }
            }
        });
        set.playTogether(outside_anim, inside_anim);
        set.setDuration(100);
        set.start();
    }


  
    private void updateProgress(long millisUntilFinished) {
        recorded_time = (int) (duration - millisUntilFinished);
        progress = 360f - millisUntilFinished / (float) duration * 360f;
        invalidate();
    }

    
    private class RecordCountDownTimer extends CountDownTimer {
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            updateProgress(0);
            recordEnd();
        }
    }

   
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            state = STATE_LONG_PRESS;   
         
            if (CheckPermission.getRecordState() != CheckPermission.STATE_SUCCESS) {
                state = STATE_IDLE;
                if (captureLisenter != null) {
                    captureLisenter.recordError();
                    return;
                }
            }
           
            startRecordAnimation(
                    button_outside_radius,
                    button_outside_radius + outside_add_size,
                    button_inside_radius,
                    button_inside_radius - inside_reduce_size
            );
        }
    }


    public void setDuration(int duration) {
        this.duration = duration;
        timer = new RecordCountDownTimer(duration, duration / 360);    
    }

    
    public void setMinDuration(int duration) {
        this.min_duration = duration;
    }

   
    public void setCaptureLisenter(CaptureListener captureLisenter) {
        this.captureLisenter = captureLisenter;
    }


    public void setButtonFeatures(int state) {
        this.button_state = state;
    }

 
    public boolean isIdle() {
        return state == STATE_IDLE ? true : false;
    }


    public void resetState() {
        state = STATE_IDLE;
    }
}

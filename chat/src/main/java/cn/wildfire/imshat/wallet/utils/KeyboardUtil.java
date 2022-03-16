package cn.wildfire.imshat.wallet.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class KeyboardUtil {
    
    public static void hideInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

   
    public static void showInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

   
    public static void hideInputMethod(Activity mAct) {
        try {// hide keybord anyway
            View v = mAct.getWindow().getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) mAct.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (Exception e) {
        }
    }

    
    public static void showInputMethod(final Activity mAct) {
        View v = mAct.getCurrentFocus();
        if (null == v) {
            return;
        }
        ((InputMethodManager) mAct.getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
    }
   
    public static boolean isSoftShowing(Context context) {
       
        int screenHeight = ((Activity)context).getWindow().getDecorView().getHeight();
     
        Rect rect = new Rect();
  
        ((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        
        return screenHeight*2/3 > rect.bottom;
    }

}

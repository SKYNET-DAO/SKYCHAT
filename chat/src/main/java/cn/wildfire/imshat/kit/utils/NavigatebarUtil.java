package cn.wildfire.imshat.kit.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

public class NavigatebarUtil {

    public static void hideNavKey(Context context) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = ((Activity) context).getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = ((Activity) context).getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);

        }
    }

    public static void showNavKey(Context context, int systemUiVisibility) {//getWindow().getDecorView().getSystemUiVisibility() 
        ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }


}

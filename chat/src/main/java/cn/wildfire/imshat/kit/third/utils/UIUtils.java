package cn.wildfire.imshat.kit.third.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.android.base.BaseApp;
import cn.wildfire.imshat.app.MyApp;

//import static io.rong.imlib.statistics.Statistics.TAG;



public class UIUtils {

    public static Toast mToast;
    public static int screenWidth;
    public static int screenHeight;
    public static int screenMin;// 
    public static int screenMax;// 
    public static float density;
    public static float scaleDensity;
    public static float xdpi;
    public static float ydpi;
    public static int densityDpi;
    public static int statusbarheight;
    public static int navbarheight;

    public static void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(String msg, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", duration);
        }
        mToast.setText(msg);
        mToast.show();
    }



    public static Context getContext() {
        return BaseApp.getContext();
    }


    public static Resources getResource() {
        return getContext().getResources();
    }


    public static String getString(int resId) {
        return getResource().getString(resId);
    }


    public static String getString(int id, Object... formatArgs) {
        return getResource().getString(id, formatArgs);
    }


    public static String[] getStringArr(int resId) {
        return getResource().getStringArray(resId);
    }


    public static int getColor(int colorId) {
        return getResource().getColor(colorId);
    }


    public static String getPackageName() {
        return getContext().getPackageName();
    }


    public static Handler getMainThreadHandler() {
        return MyApp.getMainHandler();
    }


    public static long getMainThreadId() {
        return MyApp.getMainThreadId();
    }


    public static void postTaskSafely(Runnable task) {
        int curThreadId = android.os.Process.myTid();
        
        if (curThreadId == getMainThreadId()) {
            task.run();
        } else {
            
            getMainThreadHandler().post(task);
        }
    }


    public static void postTaskDelay(Runnable task, int delayMillis) {
        getMainThreadHandler().postDelayed(task, delayMillis);
    }


    public static void removeTask(Runnable task) {
        getMainThreadHandler().removeCallbacks(task);
    }


    public static int dip2Px(int dip) {
        // px/dip = density;
        // density = dpi/160
        // 320*480 density = 1 1px = 1dp
        // 1280*720 density = 2 2px = 1dp

        float density = getResource().getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);
        return px;
    }

    /**
     * px-->dip
     */
    public static int px2dip(int px) {

        float density = getResource().getDisplayMetrics().density;
        int dip = (int) (px / density + 0.5f);
        return dip;
    }

    /**
     * sp-->px
     */
    public static int sp2px(int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResource().getDisplayMetrics()) + 0.5f);
    }


    public static int getDisplayWidth() {
        if (screenWidth == 0) {
            GetInfo(UIUtils.getContext());
        }
        return screenWidth;
    }

    public static int getDisplayHeight() {
        if (screenHeight == 0) {
            GetInfo(UIUtils.getContext());
        }
        return screenHeight;
    }

    public static void GetInfo(Context context) {
        if (null == context) {
            return;
        }
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        screenMin = (screenWidth > screenHeight) ? screenHeight : screenWidth;
        screenMax = (screenWidth < screenHeight) ? screenHeight : screenWidth;
        density = dm.density;
        scaleDensity = dm.scaledDensity;
        xdpi = dm.xdpi;
        ydpi = dm.ydpi;
        densityDpi = dm.densityDpi;
        statusbarheight = getStatusBarHeight(context);
        navbarheight = getNavBarHeight(context);
//        Log.d(TAG, "screenWidth=" + screenWidth + " screenHeight=" + screenHeight + " density=" + density);
    }

    public static int getNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        try {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
package com.android.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import androidx.multidex.MultiDexApplication;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.litepal.LitePal;

public class BaseApp extends MultiDexApplication {

    
    private static Context mContext;
    private static long mMainThreadId;
    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        
        mContext = getApplicationContext();
        mMainThreadId = android.os.Process.myTid();
        mHandler = new Handler();
        initPrettyFormatStrategy();
        LitePal.initialize(this);

    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        BaseApp.mContext = mContext;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }

    public static Handler getMainHandler() {
        return mHandler;
    }


    
    private void initPrettyFormatStrategy() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                //.logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("Skychat")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        Context newContext = LanguageUtil.initAppLanguage(base);
        super.attachBaseContext(newContext);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LanguageUtil.initAppLanguage(this);
    }
}

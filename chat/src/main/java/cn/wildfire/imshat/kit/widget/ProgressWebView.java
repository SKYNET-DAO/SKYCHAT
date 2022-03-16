package cn.wildfire.imshat.kit.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.wildfirechat.imshat.R;


public class ProgressWebView extends WebView {

    private ProgressBar mProgressBar;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 8, 0, 0));

        Drawable drawable = context.getResources().getDrawable(R.drawable.progressbar_webview);
        mProgressBar.setProgressDrawable(drawable);
        addView(mProgressBar);
        setWebChromeClient(new WebChromeClient());
        
        getSettings().setSupportZoom(false);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);  
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            getSettings().setAllowUniversalAccessFromFileURLs(true);
        }else{
            try {
                Class<?> clazz = getSettings().getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(getSettings(), true);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }



    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(GONE);
            } else {
                if (mProgressBar.getVisibility() == GONE)
                    mProgressBar.setVisibility(VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

    }

//    @Override
//    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
//        lp.x = l;
//        lp.y = t;
//        mProgressBar.setLayoutParams(lp);
//        super.onScrollChanged(l, t, oldl, oldt);
//    }



}


package cn.wildfire.imshat.wallet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import cn.wildfire.imshat.app.MyApp;


public class ResourceUtil {
    public static final String LAYTOUT="layout";
    public static final String DRAWABLE="drawable";
    public static final String MIPMAP="mipmap";
    public static final String MENU="menu";
    public static final String RAW="raw";
    public static final String ANIM="anim";
    public static final String STRING="string";
    public static final String STYLE="style";
    public static final String STYLEABLE="styleable";
    public static final String INTEGER="integer";
    public static final String ID="id";
    public static final String DIMEN="dimen";
    public static final String COLOR="color";
    public static final String BOOL="bool";
    public static final String ATTR="attr";

   
    public static int getResourceId(Context context,String name,String type){
        Resources resources=null;
        PackageManager pm=context.getPackageManager();
        try {
            resources=context.getResources();
            return resources.getIdentifier(name, type, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static byte[] readBytesFromAssets(Context context, String fileName) {
        InputStream is = null;
        byte[] buffer = null;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return buffer;
    }

   
    public static byte[] readBytesFromRaw(Context context, int rawId) {
        InputStream is = null;
        byte[] buffer = null;
        try {
            is = context.getResources().openRawResource(rawId);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return buffer;
    }

   
    public static String readStringFromAssets(Context context, String fileName) {
        String result = null;
        byte[] buffer = readBytesFromAssets(context, fileName);
        try {
            result = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

   
    public static String readStringFromRaw(Context context, int rawId) {
        String result = null;
        byte[] buffer = readBytesFromRaw(context, rawId);
        try {
            result = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    
    public static String getString(int strId){
        return MyApp.getContext().getString(strId);
    }

   
    public static int getColor(Context context,int colorId){
        return context.getResources().getColor(colorId);
    }


    public static int getColor(int colorId){
        return MyApp.getContext().getResources().getColor(colorId);
    }

  
    public static Drawable getDrawable(Context context,int drawableId){
        return context.getResources().getDrawable(drawableId);
    }

 
    public static String[] getStringArray(Context ctx,int resID) {
        return ctx.getResources().getStringArray(resID);
    }

 
    public static int[] getIntArray(Context ctx,int resID) {
        return ctx.getResources().getIntArray(resID);
    }


    public static float getDimension(Context ctx,int resID) {
        return ctx.getResources().getDimension(resID);
    }

}

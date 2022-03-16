package com.lqr.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;

public class Utils {


    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static int getImageItemWidth(Activity activity) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = activity.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * activity.getResources().getDisplayMetrics().density);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static DisplayMetrics getScreenPix(Activity activity) {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
        return displaysMetrics;
    }

 
    public static void notifyToScanMedia(Context context, String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + path);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    public static void takePhoto(Activity activity, String outputPath, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", new File(outputPath)));
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
    }


    public static String genTakePhotoOutputPath() {
        File takeImageFile;
        if (existSDCard()) {
            takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
        } else {
            takeImageFile = Environment.getDataDirectory();
        }
        takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
        return takeImageFile.getAbsolutePath();
    }


    private static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }
}

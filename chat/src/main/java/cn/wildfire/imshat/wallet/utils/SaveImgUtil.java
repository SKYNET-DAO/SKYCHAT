package cn.wildfire.imshat.wallet.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.wildfire.imshat.app.Config;



public class SaveImgUtil {


    
    public static boolean saveBitmap(Context context,Bitmap bitmap, String bitName){
        String fileName ;
        File file ;
        if(Build.BRAND .equals("Xiaomi") ){ 
            fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/"+bitName ;
        }else{  // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/"+bitName ;
        }
        file = new File(fileName);

        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out))
            {
                out.flush();
                out.close();

                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), bitName, null);
                return true;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;

        }
        
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));

        return true;

    }


    
    public static Uri saveImageToPath(Context context,String pathname, Bitmap bmp) {
        
        String storePath="";
        if(!TextUtils.isEmpty(pathname)){
            storePath = getExternalDirectoryOrCache(context, pathname) + File.separator;

        }else{
            storePath = getExternalDirectoryOrCache(context, Environment.DIRECTORY_PICTURES) + File.separator;

        }
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            

            
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return uri;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri saveImageToPath(Context context, Bitmap bmp) {
        
        String storePath = getExternalDirectoryOrCache(context, Environment.DIRECTORY_PICTURES) + File.separator;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return uri;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static File getExternalDirectoryOrCache(Context context, String type) {
        File appCacheDir = null;
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)){
                appCacheDir = context.getExternalCacheDir();
            }else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null){
                appCacheDir = new File(Environment.getExternalStorageDirectory(),"Android/data/"+context.getPackageName()+"/cache/"+type);
            }

            if (appCacheDir == null){
            //    LogUtil.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard unknown exception !");
            }else {
                if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                 //   LogUtil.e("getExternalDirectory","getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        }else {
           // LogUtil.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }


    public static void saveImageToGallery(Context context, Bitmap bmp) {
        
         File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
        String fileName = Config.Companion.getFILE_SAVE_DIR();
        File appDir = new File(file ,fileName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName1 = System.currentTimeMillis() + ".jpg";
        File currentFile = new File(appDir, fileName1);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(currentFile.getPath()))));
    }

}

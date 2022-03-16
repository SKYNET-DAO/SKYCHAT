package cn.wildfire.imshat.kit.utils.Path;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileChooseUtil {

    private Context context;
    private static FileChooseUtil util = null;

    private FileChooseUtil(Context context) {
        this.context = context;
    }

    public static FileChooseUtil getInstance(Context context) {
        if (util == null) {
            util = new FileChooseUtil(context);
        }
        return util;
    }

  
    public String getChooseFileResultPath(Uri uri) {
        String chooseFilePath = null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            chooseFilePath = uri.getPath();
         //   Toast.makeText(context, chooseFilePath, Toast.LENGTH_SHORT).show();
            return chooseFilePath;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            chooseFilePath = getPath(context, uri);
        } else {
            chooseFilePath = getRealPathFromURI(uri);
        }
        return chooseFilePath;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];

                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);


            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                }

              //  final String selection = "_id=?";
              //  final String[] selectionArgs = new String[]{split[1]};

                return  getFilePathFromURI(context,contentUri);

            }

        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getFilePathFromURI(context, uri);

        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
           return uri.getPath();

        }
        return null;
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


//    public String getAbsolutePath(Context context,Uri uri) {
//        if(Build.VERSION.SDK_INT >= 19){
//            String id = uri.getLastPathSegment().split(":")[1];
//            final String[] imageColumns = {MediaStore.Images.Media.DATA };
//            final String imageOrderBy = null;
//            Uri tempUri = getUri();
//            Cursor imageCursor = context.getContentResolver().query(tempUri, imageColumns,
//                    MediaStore.Images.Media._ID + "="+id, null, imageOrderBy);
//            if (imageCursor.moveToFirst()) {
//                return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
//            }else{
//                return null;
//            }
//        }else{
//            String[] projection = { MediaStore.MediaColumns.DATA };
//
//            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//            if (cursor != null) {
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//                cursor.moveToFirst();
//                return cursor.getString(column_index);
//            } else
//                return null;
//        }
//
//    }



    public static String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(context.getFilesDir() + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
package com.lqr.imagepicker;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.lqr.imagepicker.bean.ImageFolder;
import com.lqr.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         
    public static final int LOADER_CATEGORY = 1;    
    private final String[] IMAGE_PROJECTION = {     
            MediaStore.Images.Media.DISPLAY_NAME,   
            MediaStore.Images.Media.DATA,           
            MediaStore.Images.Media.SIZE,           
            MediaStore.Images.Media.WIDTH,          
            MediaStore.Images.Media.HEIGHT,         
            MediaStore.Images.Media.MIME_TYPE,      
            MediaStore.Images.Media.DATE_ADDED};    

    private FragmentActivity activity;
    private OnImageLoadListener loadedListener;                     
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   

    /**
     * @param activity       
     * @param path           
     * @param loadedListener 
     */
    public ImageDataSource(FragmentActivity activity, String path, OnImageLoadListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;

        LoaderManager loaderManager = activity.getSupportLoaderManager();
        if (path == null) {
            loaderManager.initLoader(LOADER_ALL, null, this);
        } else {

            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;

        if (id == LOADER_ALL) {
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC");
        }

        if (id == LOADER_CATEGORY) {
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[6] + " DESC");
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && !data.isAfterLast()) {
            imageFolders.clear();
            ArrayList<ImageItem> allImages = new ArrayList<>();   
            while (data.moveToNext()) {

                String imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                long imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                int imageWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                int imageHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                String imageMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
                long imageAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));

                ImageItem imageItem = new ImageItem();
                imageItem.name = imageName;
                imageItem.path = imagePath;
                imageItem.size = imageSize;
                imageItem.width = imageWidth;
                imageItem.height = imageHeight;
                imageItem.mimeType = imageMimeType;
                imageItem.createTime = imageAddTime;
                allImages.add(imageItem);

                File imageFile = new File(imagePath);
                File imageParentFile = imageFile.getParentFile();
                ImageFolder imageFolder = new ImageFolder();
                imageFolder.name = imageParentFile.getName();
                imageFolder.path = imageParentFile.getAbsolutePath();

                if (!imageFolders.contains(imageFolder)) {
                    ArrayList<ImageItem> images = new ArrayList<>();
                    images.add(imageItem);
                    imageFolder.cover = imageItem;
                    imageFolder.images = images;
                    imageFolders.add(imageFolder);
                } else {
                    imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
                }
            }

            if (data.getCount() > 0) {

                ImageFolder allImagesFolder = new ImageFolder();
                allImagesFolder.name = activity.getResources().getString(R.string.all_images);
                allImagesFolder.path = "/";
                allImagesFolder.cover = allImages.get(0);
                allImagesFolder.images = allImages;
                imageFolders.add(0, allImagesFolder);  
            }
        }


        loadedListener.onImageLoad(imageFolders);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }


    public interface OnImageLoadListener {
        void onImageLoad(List<ImageFolder> imageFolders);
    }
}

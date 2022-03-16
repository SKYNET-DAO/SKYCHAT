package com.lqr.imagepicker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.android.base.BaseApp;
import com.lqr.imagepicker.ImageDataSource;
import com.lqr.imagepicker.ImagePickStore;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.R;
import com.lqr.imagepicker.Utils;
import com.lqr.imagepicker.adapter.ImageFolderAdapter;
import com.lqr.imagepicker.adapter.ImageGridAdapter;
import com.lqr.imagepicker.bean.ImageFolder;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.view.FolderPopUpWindow;

import java.util.List;

import androidx.annotation.NonNull;

public class ImageGridActivity extends ImageBaseActivity implements ImageDataSource.OnImageLoadListener, ImageGridAdapter.OnImageItemClickListener, View.OnClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

    private ImagePickStore store;
    private boolean multiMode = false;
    private int limit;
    private boolean showCamera;

    private GridView mGridView;  
    private View mFooterBar;     
    private Button mBtnOk;       
    private Button mBtnDir;      
    private Button mBtnPre;      
    private ImageFolderAdapter mImageFolderAdapter;    
    private FolderPopUpWindow mFolderPopupWindow;  
    private List<ImageFolder> mImageFolders;   
    private ImageGridAdapter mImageGridAdapter;  

    private String takePhotoOutputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);

        store = ImagePickStore.getInstance();
        store.clearSelectedImages();

        Intent intent = getIntent();
        multiMode = intent.getBooleanExtra("multiMode", false);
        limit = intent.getIntExtra("limit", 9);
        showCamera = intent.getBooleanExtra("showCamera", false);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnDir = (Button) findViewById(R.id.btn_dir);
        mBtnDir.setOnClickListener(this);
        mBtnPre = (Button) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mFooterBar = findViewById(R.id.footer_bar);
        if (multiMode) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

        mImageGridAdapter = new ImageGridAdapter(this, showCamera, multiMode, limit);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(this, null, this);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageGridAdapter.notifyDataSetChanged();
        mImageFolderAdapter.notifyDataSetChanged();
        updatePickStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, null, this);
            } else {
                showToast(BaseApp.getContext().getString(R.string.str_not_permission));
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utils.takePhoto(this, takePhotoOutputPath, ImagePicker.REQUEST_CODE_TAKE);
            } else {
                showToast(BaseApp.getContext().getString(R.string.str_not_open_camera));
            }
        }
    }

    public void takePhoto() {
        takePhotoOutputPath = Utils.genTakePhotoOutputPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(Manifest.permission.CAMERA)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, ImageGridActivity.REQUEST_PERMISSION_CAMERA);
            } else {
                Utils.takePhoto(this, takePhotoOutputPath, ImagePicker.REQUEST_CODE_TAKE);
            }
        } else {
            Utils.takePhoto(this, takePhotoOutputPath, ImagePicker.REQUEST_CODE_TAKE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        store.destroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            finishImagePick();
        } else if (id == R.id.btn_dir) {
            if (mImageFolders == null) {
                return;
            }
           
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, store.getSelectedImages());
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.btn_back) {
            
            finish();
        }
    }


    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                store.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
                    mImageGridAdapter.refreshData(imageFolder.images);
                    mBtnDir.setText(imageFolder.name);
                }
                mGridView.smoothScrollToPosition(0);
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImageLoad(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        store.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
            mImageGridAdapter.refreshData(null);
        } else {
            mImageGridAdapter.refreshData(imageFolders.get(0).images);
        }
        mImageGridAdapter.setOnImageItemClickListener(this);
        mGridView.setAdapter(mImageGridAdapter);
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {

        position = showCamera ? position - 1 : position;
        if (multiMode) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);

            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  
        } else {
            store.addSelectedImageItem(position, store.getCurrentImageFolderItems().get(position), true);
            finishImagePick();
        }
    }

    public void updatePickStatus() {
        if (store.getSelectImageCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, store.getSelectImageCount(), limit));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
        }
        mBtnPre.setText(getResources().getString(R.string.preview_count, store.getSelectImageCount()));
        mImageGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ImagePicker.REQUEST_CODE_TAKE:
                    onTakePhoto();
                    break;
                case ImagePicker.REQUEST_CODE_PREVIEW:
                    onPreview();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void finishImagePick() {
        Intent intent = new Intent();
        intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, store.getSelectedImages());
        intent.putExtra(ImagePicker.EXTRA_COMPRESS, store.isCompress());
        setResult(Activity.RESULT_OK, intent);   
        finish();
    }

    private void onPreview() {
        if (store.getSelectImageCount() > 0) {
            finishImagePick();
        }
    }

    private void onTakePhoto() {
        
        Utils.notifyToScanMedia(this, takePhotoOutputPath);
        ImageItem imageItem = new ImageItem();
        imageItem.path = takePhotoOutputPath;
        store.clearSelectedImages();
        store.addSelectedImageItem(0, imageItem, true);
        Intent intent = new Intent();
        intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, store.getSelectedImages());
        intent.putExtra(ImagePicker.EXTRA_COMPRESS, store.isCompress());
        setResult(Activity.RESULT_OK, intent);   
        finish();
    }
}
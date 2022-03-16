package com.lqr.imagepicker.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lqr.imagepicker.ImagePickStore;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.R;
import com.lqr.imagepicker.Utils;
import com.lqr.imagepicker.adapter.ImagePageAdapter;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.view.ViewPagerFixed;

import java.util.ArrayList;

public abstract class ImagePreviewBaseActivity extends ImageBaseActivity {

    protected ImagePickStore store;
    protected ArrayList<ImageItem> mImageItems;      
    protected int mCurrentPosition = 0;              
    protected TextView mTitleCount;                  
    protected ArrayList<ImageItem> selectedImages;   
    protected View content;
    protected View topBar;
    protected ViewPagerFixed mViewPager;
    protected ImagePageAdapter mAdapter;
    protected int pickLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        pickLimit = getIntent().getIntExtra("pickLimit", 9);

        store = ImagePickStore.getInstance();
        mCurrentPosition = getIntent().getIntExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
        mImageItems = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
        if (mImageItems == null) {
            mImageItems = store.getCurrentImageFolderItems();
        }

        selectedImages = store.getSelectedImages();

        
        content = findViewById(R.id.content);

        
        topBar = findViewById(R.id.top_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topBar.getLayoutParams();
            params.topMargin = Utils.getStatusHeight(this);
            topBar.setLayoutParams(params);
        }
        topBar.findViewById(R.id.btn_ok).setVisibility(View.GONE);
        topBar.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleCount = (TextView) findViewById(R.id.tv_des);

        mViewPager = (ViewPagerFixed) findViewById(R.id.viewpager);
        mAdapter = new ImagePageAdapter(this, mImageItems);
        mAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view, float v, float v1) {
                onImageSingleTap();
            }
        });
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);

        
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mImageItems.size()));
    }


    public abstract void onImageSingleTap();
}
package com.lqr.imagepicker.ui;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.lqr.imagepicker.R;

public class ImagePreviewDelActivity extends ImagePreviewBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView mBtnDel = (ImageView) findViewById(R.id.btn_del);
        mBtnDel.setOnClickListener(this);
        mBtnDel.setVisibility(View.VISIBLE);
        topBar.findViewById(R.id.btn_back).setOnClickListener(this);

        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mImageItems.size()));
        
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mImageItems.size()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_del) {
            showDeleteDialog();
        } else if (id == R.id.btn_back) {
            onBackPressed();
        }
    }

    
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.str_tip));
        builder.setMessage(getString(R.string.str_tip_del_img));
        builder.setNegativeButton(getString(R.string.str_cancel), null);
        builder.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                mImageItems.remove(mCurrentPosition);
                if (mImageItems.size() > 0) {
                    mAdapter.setData(mImageItems);
                    mAdapter.notifyDataSetChanged();
                    mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mImageItems.size()));
                } else {
                    onBackPressed();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    
    @Override
    public void onImageSingleTap() {
        if (topBar.getVisibility() == View.VISIBLE) {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, com.lqr.imagepicker.R.anim.top_out));
            topBar.setVisibility(View.GONE);
            tintManager.setStatusBarTintResource(com.lqr.imagepicker.R.color.transparent);
            
            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            topBar.setAnimation(AnimationUtils.loadAnimation(this, com.lqr.imagepicker.R.anim.top_in));
            topBar.setVisibility(View.VISIBLE);
            tintManager.setStatusBarTintResource(com.lqr.imagepicker.R.color.status_bar);
            
            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
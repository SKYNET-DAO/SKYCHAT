package com.lqr.emoji;

import android.content.Context;
import android.widget.ImageView;


public interface IImageLoader {

    void displayImage(Context context, String path, ImageView imageView);
}

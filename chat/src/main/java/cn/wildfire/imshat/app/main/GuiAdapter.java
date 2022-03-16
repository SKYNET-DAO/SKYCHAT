package cn.wildfire.imshat.app.main;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class GuiAdapter extends PagerAdapter {

    private int[] mImages;
    private int[] tips;

    public GuiAdapter(int[] mImage, int[] tips) {
        this.mImages = mImage;
        this.tips=tips;
    }



    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(mImages[position]);
        container.addView(imageView); 
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }
}

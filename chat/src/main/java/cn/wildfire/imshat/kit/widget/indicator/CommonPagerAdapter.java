package cn.wildfire.imshat.kit.widget.indicator;


import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;



public abstract class CommonPagerAdapter<T> extends RealPagerAdapterImp {

    private SparseArray<View> viewCache = new SparseArray<>();
    private List<T> dataList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public CommonPagerAdapter(List<T> dataList) {
        this.dataList = dataList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public final int getRealCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public View instantiateItem(@NonNull ViewGroup container, final int position) {
        View itemView = getItemView(container, position);
        if (onItemClickListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
        }
        if (onItemLongClickListener != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemLongClickListener.onItemLongClick(v, position);
                }
            });
        }
        return itemView;
    }

    
    private View getItemView(@NonNull ViewGroup container, int position) {
        View itemView = viewCache.get(position);
        if (itemView == null) {
            itemView = getPageItemView(container, position);
            viewCache.put(position, itemView);
        }
        renderItemView(itemView, position);
        container.addView(itemView);
        return itemView;
    }

    
    public abstract void renderItemView(@NonNull View itemView, int position);

    
    @NonNull
    public abstract View getPageItemView(@NonNull ViewGroup container, final int position);

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    public T getBindItemData(int position) {
        if (dataList != null && dataList.size() > position && position >= 0) {
            return dataList.get(position);
        }
        return null;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}

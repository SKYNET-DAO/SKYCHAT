package cn.wildfire.imshat.plugs.listener;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by Administrator on 2017/1/5 0005.
 */

public class ItemDragHelperCallBack extends ItemTouchHelper.Callback {
    private OnChannelDragListener onChannelDragListener;

    public ItemDragHelperCallBack(OnChannelDragListener onChannelDragListener) {
        this.onChannelDragListener = onChannelDragListener;
    }

    public void setOnChannelDragListener(OnChannelDragListener onChannelDragListener) {
        this.onChannelDragListener = onChannelDragListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        int dragFlags;
        if (manager instanceof GridLayoutManager || manager instanceof StaggeredGridLayoutManager) {
            
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        } else {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if (onChannelDragListener != null)
            onChannelDragListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}

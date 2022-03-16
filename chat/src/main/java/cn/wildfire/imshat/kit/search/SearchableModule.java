package cn.wildfire.imshat.kit.search;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SearchableModule<R, V extends RecyclerView.ViewHolder> {
    protected String keyword;
    private OnResultItemClickListener<R> listener;

    public abstract V onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type);

    public abstract void onBind(Fragment fragment, V holder, R r);

 
    public final void onClickInternal(Fragment fragment, V holder, View view, R r) {
        if (listener != null) {
            listener.onResultItemClick(fragment, holder.itemView, view, r);
        } else {
            onClick(fragment, holder, view, r);
        }
    }

    public void onClick(Fragment fragment, V holder, View view, R r) {
        // do nothing
    }

    /**
     * -1, 0, 1 
     *
     * @param r one of the search results
     * @return
     */
    public abstract int getViewType(R r);

    public abstract int priority();

    public abstract String category();

    public final List<R> searchInternal(String keyword) {
        this.keyword = keyword;
        return search(keyword);
    }


    public abstract List<R> search(String keyword);

    public void setOnResultItemListener(OnResultItemClickListener<R> listener) {
        this.listener = listener;
    }


    public boolean expandable() {
        return true;
    }

    public static final int DEFAULT_SHOW_RESULT_ITEM_COUNT = 4;
}

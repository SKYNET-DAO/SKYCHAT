package cn.wildfire.imshat.kit.search.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public class ExpandViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.expandTextView)
    TextView expandTextView;

    public ExpandViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void onBind(String category, int count) {
        // todo
        expandTextView.setText(UIUtils.getString(R.string.str_sy_show) + count + UIUtils.getString(R.string.str_item));
    }
}

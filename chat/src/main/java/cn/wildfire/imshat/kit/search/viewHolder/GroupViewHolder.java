package cn.wildfire.imshat.kit.search.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.fragment.app.Fragment;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupSearchResult;

public class GroupViewHolder extends ResultItemViewHolder<GroupSearchResult> {
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.descTextView)
    TextView descTextView;

    public GroupViewHolder(Fragment fragment, View itemView) {
        super(fragment, itemView);
        ButterKnife.bind(this, itemView);
    }


    @Override
    public void onBind(String keyword, GroupSearchResult groupSearchResult) {
        nameTextView.setText(groupSearchResult.groupInfo.name);
        Glide.with(fragment).load(groupSearchResult.groupInfo.portrait).into(portraitImageView);

        String desc = "";
        switch (groupSearchResult.marchedType) {
            case 0:
                desc = UIUtils.getString(R.string.str_groupname_container) + keyword;
                break;
            case 1:
                desc = UIUtils.getString(R.string.str_groupmember_container) + keyword;
                break;
            case 2:
                desc = UIUtils.getString(R.string.str_groupname_member_container) + keyword;
                break;
            default:
                break;
        }
        descTextView.setText(desc);
    }
}

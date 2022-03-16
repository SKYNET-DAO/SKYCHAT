package cn.wildfire.imshat.kit.conversation.mention;

import android.view.View;

import androidx.fragment.app.Fragment;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.HeaderValue;
import cn.wildfire.imshat.kit.contact.viewholder.header.HeaderViewHolder;
import cn.wildfirechat.imshat.R;

@LayoutRes(resId = R.layout.conversation_header_mention_all)
public class MentionAllHeaderViewHolder extends HeaderViewHolder<HeaderValue> {
    public MentionAllHeaderViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(HeaderValue value) {

    }
}

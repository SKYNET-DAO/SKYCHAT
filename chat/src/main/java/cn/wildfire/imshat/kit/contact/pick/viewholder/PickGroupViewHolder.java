package cn.wildfire.imshat.kit.contact.pick.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.GroupValue;
import cn.wildfire.imshat.kit.contact.viewholder.header.HeaderViewHolder;
import cn.wildfirechat.imshat.R;

@SuppressWarnings("unused")
@LayoutRes(resId = R.layout.contact_header_group)
public class PickGroupViewHolder extends HeaderViewHolder<GroupValue> {

    public PickGroupViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(GroupValue groupValue) {

    }
}

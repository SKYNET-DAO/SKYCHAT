package cn.wildfire.imshat.kit.contact.viewholder.header;

import android.view.View;

import androidx.fragment.app.Fragment;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.HeaderValue;
import cn.wildfirechat.imshat.R;

@SuppressWarnings("unused")
@LayoutRes(resId = R.layout.contact_header_channel)
public class ChannelViewHolder extends HeaderViewHolder<HeaderValue> {

    public ChannelViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(HeaderValue headerValue) {

    }
}

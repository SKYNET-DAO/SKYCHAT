package cn.wildfire.imshat.kit.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.FriendRequestValue;
import cn.wildfirechat.imshat.R;

@SuppressWarnings("unused")
@LayoutRes(resId = R.layout.contact_header_friend)
public class FriendRequestViewHolder extends HeaderViewHolder<FriendRequestValue> {
    @Bind(R.id.unreadFriendRequestCountTextView)
    TextView unreadRequestCountTextView;
    private FriendRequestValue value;


    public FriendRequestViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(FriendRequestValue value) {
        this.value = value;
        if (value.getUnreadRequestCount() > 0) {
            unreadRequestCountTextView.setVisibility(View.VISIBLE);
            unreadRequestCountTextView.setText("" + value.getUnreadRequestCount());
        } else {
            unreadRequestCountTextView.setVisibility(View.GONE);
        }
    }
}

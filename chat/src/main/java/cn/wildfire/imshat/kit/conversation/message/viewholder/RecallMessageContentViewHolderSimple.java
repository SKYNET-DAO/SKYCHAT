package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.Bind;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.notification.RecallMessageContent;

@MessageContentType(RecallMessageContent.class)
@LayoutRes(resId = R.layout.conversation_item_notification)
public class RecallMessageContentViewHolderSimple extends SimpleNotificationMessageContentViewHolder {
    @Bind(R.id.notificationTextView)
    TextView notificationTextView;

    public RecallMessageContentViewHolderSimple(FragmentActivity activity, RecyclerView.Adapter adapter, View itemView) {
        super(activity, adapter, itemView);
    }

    @Override
    protected void onBind(UiMessage message) {
        notificationTextView.setText(message.message.digest());
    }
}

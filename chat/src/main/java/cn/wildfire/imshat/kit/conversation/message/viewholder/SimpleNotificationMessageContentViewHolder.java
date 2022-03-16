package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.LanguageStrConvert;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.notification.AddGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupNameNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupPortraitNotificationContent;
import cn.wildfirechat.message.notification.CreateGroupNotificationContent;
import cn.wildfirechat.message.notification.DismissGroupNotificationContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.ModifyGroupAliasNotificationContent;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.message.notification.QuitGroupNotificationContent;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.message.notification.TransferGroupOwnerNotificationContent;

@MessageContentType(value = {
        AddGroupMemberNotificationContent.class,
        ChangeGroupNameNotificationContent.class,
        ChangeGroupPortraitNotificationContent.class,
        CreateGroupNotificationContent.class,
        DismissGroupNotificationContent.class,
        DismissGroupNotificationContent.class,
        KickoffGroupMemberNotificationContent.class,
        ModifyGroupAliasNotificationContent.class,
        QuitGroupNotificationContent.class,
        TransferGroupOwnerNotificationContent.class,
        TipNotificationContent.class,
        RecallMessageContent.class,
        // TODO add more

})
@LayoutRes(resId = R.layout.conversation_item_notification)

public class SimpleNotificationMessageContentViewHolder extends MessageContentViewHolder {

    @Bind(R.id.notificationTextView)
    TextView notificationTextView;

    public SimpleNotificationMessageContentViewHolder(FragmentActivity activity, RecyclerView.Adapter adapter, View itemView) {
        super(activity, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message, int position) {
        super.onBind(message, position);
        onBind(message);
    }

    @Override
    public boolean contextMenuItemFilter(UiMessage uiMessage, String tag) {
        return true;
    }

    protected void onBind(UiMessage message) {
        String notification;
        try {
            notification = ((NotificationMessageContent) message.message.content).formatNotification(message.message);
        } catch (Exception e) {
            e.printStackTrace();
            notification = "message is invalid";
        }
        //adapter multy country language
        notification= LanguageStrConvert.convertLang(notification);
        Logger.e("-------SimpleNotificationMessageContentViewHolder------>"+notification);
        notificationTextView.setText(notification);

    }
}

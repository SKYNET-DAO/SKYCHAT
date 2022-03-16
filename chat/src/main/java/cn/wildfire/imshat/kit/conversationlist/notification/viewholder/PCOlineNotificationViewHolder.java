package cn.wildfire.imshat.kit.conversationlist.notification.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;

import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.annotation.StatusNotificationType;
import cn.wildfire.imshat.kit.conversationlist.notification.PCOnlineNotification;
import cn.wildfire.imshat.kit.conversationlist.notification.StatusNotification;
import cn.wildfirechat.imshat.R;

@LayoutRes(resId = R.layout.conversationlist_item_notification_pc_online)
@StatusNotificationType(PCOnlineNotification.class)
public class PCOlineNotificationViewHolder extends StatusNotificationViewHolder {
    public PCOlineNotificationViewHolder(Fragment fragment) {
        super(fragment);
    }

    @Override
    public void onBind(View view, StatusNotification notification) {

    }
}

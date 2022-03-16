package cn.wildfire.imshat.kit.conversationlist.notification.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;

import cn.wildfire.imshat.kit.conversationlist.notification.StatusNotification;

public abstract class StatusNotificationViewHolder {
    protected Fragment fragment;

    public StatusNotificationViewHolder(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void onBind(View view, StatusNotification notification);
}

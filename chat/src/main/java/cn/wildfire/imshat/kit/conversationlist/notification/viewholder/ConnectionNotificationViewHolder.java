package cn.wildfire.imshat.kit.conversationlist.notification.viewholder;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.annotation.StatusNotificationType;
import cn.wildfire.imshat.kit.conversationlist.notification.ConnectionStatusNotification;
import cn.wildfire.imshat.kit.conversationlist.notification.StatusNotification;
import cn.wildfirechat.imshat.R;

@LayoutRes(resId = R.layout.conversationlist_item_notification_connection_status)
@StatusNotificationType(ConnectionStatusNotification.class)
public class ConnectionNotificationViewHolder extends StatusNotificationViewHolder {
    public ConnectionNotificationViewHolder(Fragment fragment) {
        super(fragment);
    }

    @Bind(R.id.statusTextView)
    TextView statusTextView;

    @Override
    public void onBind(View view, StatusNotification notification) {
        String status = ((ConnectionStatusNotification) notification).getValue();
        statusTextView.setText(status);
    }

    @OnClick(R.id.statusTextView)
    public void onClick() {
       // Toast.makeText(fragment.getContext(), "status on Click", Toast.LENGTH_SHORT).show();
    }
}

package cn.wildfirechat.push.vivo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.vivo.push.PushClient;
import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.remote.ChatManager;

public class IVivoPushMessageReceiver extends OpenClientPushMessageReceiver {
    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage upsNotificationMessage) {
        long msgId;
        String customeContent = "";
        if (upsNotificationMessage != null) {
            msgId = upsNotificationMessage.getMsgId();
            customeContent = upsNotificationMessage.getSkipContent();
        }
    }
    @Override
    public void onReceiveRegId(Context context, String s) {
        if (TextUtils.isEmpty(s)) {
            String deviceToken=PushClient.getInstance(context).getRegId();
            Log.e("NPL","-----deviceToken-->"+deviceToken);
            if(!TextUtils.isEmpty(deviceToken)){
                ChatManager.Instance().setDeviceToken(deviceToken, ChatManager.PushType.VIVO);
            }
        } else {

            try {
                ChatManager.Instance().setDeviceToken(s, ChatManager.PushType.VIVO);
            } catch (NotInitializedExecption notInitializedExecption) {
                notInitializedExecption.printStackTrace();
            }
        }
    }
}


package cn.wildfirechat.push;

import android.content.Context;

class DefaultPushMessageHandler extends PushMessageHandler {
    @Override
    public void handleIMPushMessage(Context context, AndroidPushMessage pushMessage, PushService.PushServiceType pushServiceType) {

//        Intent intent = new Intent();
//        intent.setAction(PushConstant.PUSH_MESSAGE_ACTION);
//        intent.putExtra(PushConstant.PUSH_MESSAGE, pushMessage);
//        context.sendBroadcast(intent);
        //ChatManager.Instance().forceConnect();
    }

    //Application push data
    @Override
    public void handlePushMessageData(Context context, String pushData) {

    }
}

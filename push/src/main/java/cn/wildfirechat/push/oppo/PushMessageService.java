package cn.wildfirechat.push.oppo;

import android.content.Context;
import android.util.Log;

import com.coloros.mcssdk.PushService;
import com.coloros.mcssdk.mode.AppMessage;
import com.coloros.mcssdk.mode.CommandMessage;
import com.coloros.mcssdk.mode.SptDataMessage;

import cn.wildfirechat.push.utils.JsonUtil;


public class PushMessageService extends PushService {


    @Override
    public void processMessage(Context context, CommandMessage commandMessage) {
        super.processMessage(context, commandMessage);
        Log.e("---processMessage0---->", JsonUtil.toJson(commandMessage));
        //TestModeUtil.addLogString(PushMessageService.class.getSimpleName(), "Receive CommandMessage");
    }


    @Override
    public void processMessage(Context context, AppMessage appMessage) {
        super.processMessage(context, appMessage);
        String content = appMessage.getContent();
        Log.e("---processMessage1---->",content);
      //  TestModeUtil.addLogString(PushMessageService.class.getSimpleName(), "Receive AppMessage:" + content);
    }


    @Override
    public void processMessage(Context context, SptDataMessage sptDataMessage) {
        super.processMessage(context.getApplicationContext(), sptDataMessage);
        String content = sptDataMessage.getContent();
        Log.e("---processMessage2---->",content);
//        TestModeUtil.addLogString(PushMessageService.class.getSimpleName(), "Receive SptDataMessage:" + content);
    }
}

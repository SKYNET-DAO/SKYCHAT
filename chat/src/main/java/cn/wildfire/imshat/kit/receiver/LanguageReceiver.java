package cn.wildfire.imshat.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;

import cn.wildfire.imshat.wallet.activity.AppManager;

public class LanguageReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(getClass().getSimpleName(), "mReceiver  onReceive  intent.getAction(): "+intent.getAction());

        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            
            AppManager.getInstance().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}

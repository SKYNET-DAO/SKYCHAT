package cn.wildfire.imshat.kit.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.OnClick;
import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.app.main.SplashActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import com.android.base.utils.ACacheUtil;
import cn.wildfire.imshat.wallet.activity.AppManager;
import cn.wildfirechat.imshat.R;

public class SettingActivity extends WfcBaseActivity {

    @Override
    protected int contentLayout() {
        return R.layout.setting_activity;
    }

    @OnClick(R.id.exitOptionItemView)
    void exit() {
        ChatManagerHolder.gChatManager.disconnect(true);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.newMsgNotifyOptionItemView)
    void notifySetting() {
        gotoNotificationSetting(this);
    }


    @Override
    protected void afterViews() {
      setTitle(getString(R.string.str_settings));
    }

    public static void gotoNotificationSetting(Activity activity) {
        ApplicationInfo appInfo = activity.getApplicationInfo();
        String pkg = activity.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
                
                intent.putExtra("app_package", pkg);
                intent.putExtra("app_uid", uid);
                activity.startActivity(intent);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + pkg));
                activity.startActivity(intent);
            } else {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
           // MyLog.e(sTAG, e);
        }
    }



    @OnClick(R.id.aboutOptionItemView)
    void about() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.switchPeers)
    void switchPeers() {
       showSwitchPeers();
    }


    private void showSwitchPeers(){
        //pop pay dialog

        String defaultImhost=ACacheUtil.get().getAsString("imhost");
        if(TextUtils.isEmpty(defaultImhost)){
            defaultImhost=Config.Companion.getIM_SERVER_HOST();
        }

        int index=Config.Companion.getPeers().indexOf(defaultImhost);
        MaterialDialog payDialog = new MaterialDialog.Builder(this)
                .title(UIUtils.getString(R.string.str_switch_peer))
                .items(Config.Companion.getPeers())
                .itemsCallbackSingleChoice(index, (dialog, itemView, which, text) -> true)
                .cancelable(false)
                .positiveText(UIUtils.getString(R.string.str_switch))
                .negativeText(getString(R.string.str_cancel))
                .onPositive((d,w)->{
                    
                    AppManager.getInstance().SwitchPeers(Config.Companion.getPeers().get(d.getSelectedIndex()));
                })
                .build();
        payDialog.show();


    }


}

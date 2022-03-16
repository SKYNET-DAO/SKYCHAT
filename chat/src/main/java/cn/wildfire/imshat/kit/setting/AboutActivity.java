package cn.wildfire.imshat.kit.setting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcWebViewActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public class AboutActivity extends WfcBaseActivity {

    @Bind(R.id.infoTextView)
    TextView infoTextView;

    @Override
    protected int contentLayout() {
        return R.layout.activity_about;
    }

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_about));
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            String info = packageInfo.packageName + "\n"
                    + packageInfo.versionCode + " " + packageInfo.versionName + "\n"
                    + Config.Companion.getIM_SERVER_HOST() + " " + Config.Companion.getIM_SERVER_PORT() + "\n"
                    + Config.Companion.getAPP_SERVER_HOST() + " " + Config.Companion.getAPP_SERVER_PORT() + "\n"
                    + Config.Companion.getICE_ADDRESS() + " " + Config.Companion.getICE_USERNAME() + " " + Config.Companion.getICE_PASSWORD() + "\n";

            infoTextView.setText("V"+packageInfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.introOptionItemView)
    public void intro() {
        WfcWebViewActivity.loadUrl(this, UIUtils.getString(R.string.str_func_introduce), "https://github.com/SKYNET-DAO/SKYCHAT/wiki");
    }

    @OnClick(R.id.agreementOptionItemView)
    public void agreement() {
        WfcWebViewActivity.loadUrl(this, UIUtils.getString(R.string.str_xy), "https://github.com/SKYNET-DAO/SKYCHAT/wiki/skychat_user_agreement");
    }

    @OnClick(R.id.privacyOptionItemView)
    public void privacy() {
        WfcWebViewActivity.loadUrl(this, "SkychatIM Privacy protect policy", "https://github.com/SKYNET-DAO/SKYCHAT/wiki/skychat_user_privacy");
    }
}

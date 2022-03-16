package cn.wildfire.imshat.app.main;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.app.login.model.PCSession;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.net.OKHttpHelper;
import cn.wildfire.imshat.kit.net.SimpleCallback;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;

public class PCLoginActivity extends WfcBaseActivity {
    private String token;
    private PCSession pcSession;
    @Bind(R.id.confirmButton)
    Button confirmButton;

    @Override
    protected void beforeViews() {
        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token)) {
            finish();
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.pc_login_activity;
    }

    @Override
    protected void afterViews() {
        scanPCLogin(token);
    }

    @OnClick(R.id.confirmButton)
    void confirmPCLogin() {
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        confirmPCLogin(token, userViewModel.getUserId());
    }

    private void scanPCLogin(String token) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("Processing")
                .progress(true, 100)
                .build();
        dialog.show();
        String url = "http://" + Config.Companion.getAPP_SERVER_HOST() + ":" + Config.Companion.getAPP_SERVER_PORT() + "/scan_pc";
        url += "/" + token;
        OKHttpHelper.post(url, null, new SimpleCallback<PCSession>() {
            @Override
            public void onUiSuccess(PCSession pcSession) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();
                PCLoginActivity.this.pcSession = pcSession;
                if (pcSession.getStatus() == 1) {
                    confirmButton.setEnabled(true);
                } else {
                    Toast.makeText(PCLoginActivity.this, "status: " + pcSession.getStatus(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void confirmPCLogin(String token, String userId) {
        String url = "http://" + Config.Companion.getAPP_SERVER_HOST() + ":" + Config.Companion.getAPP_SERVER_PORT() + "/confirm_pc";

        Map<String, String> params = new HashMap<>(2);
        params.put("user_id", userId);
        params.put("token", token);
        OKHttpHelper.post(url, params, new SimpleCallback<PCSession>() {
            @Override
            public void onUiSuccess(PCSession pcSession) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, "Login OK", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

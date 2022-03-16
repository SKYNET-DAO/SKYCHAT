package cn.wildfire.imshat.app.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.ipfsclient.IPFSManager;
import com.android.wallet.constants.Constants;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.app.IpViewModel;
import cn.wildfire.imshat.app.login.SMSLoginActivity;
import cn.wildfire.imshat.app.login.model.LoginResult;
import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.net.OKHttpHelper;
import cn.wildfire.imshat.kit.net.SimpleCallback;
import com.android.base.utils.ACacheUtil;

import org.litepal.LitePal;

import cn.wildfire.imshat.kit.third.utils.UIUtils;

import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.net.api.ImRetrofitService;
import cn.wildfire.imshat.wallet.BitUtil;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfire.imshat.wallet.services.WalletService;
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel;
import cn.wildfirechat.imshat.BuildConfig;
import cn.wildfirechat.imshat.R;

import static cn.wildfire.imshat.net.AppConst.BASE_URL;
import static com.android.base.Config.PLUG_NETWORK_TEST;

public class SplashActivity extends WfcBaseActivity {

    private static String[] permissions = {
//            Manifest.permission.READ_PHONE_STATE,

//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,


            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,

            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final int REQUEST_CODE_DRAW_OVERLAY = 101;

    private SharedPreferences sharedPreferences;
    private String id;
    private String token;
    private WalletViewModel walletViewModel;
    private IpViewModel ipViewModel;
    private int index=0;
    String[] ips;

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void afterViews() {

        if(PLUG_NETWORK_TEST){
            ips=getResources().getStringArray(R.array.channel_test);

        }else{
            ips=getResources().getStringArray(R.array.channel_main);

        }
        walletViewModel= ViewModelProviders.of(this).get(WalletViewModel.class);
        ipViewModel= ViewModelProviders.of(this).get(IpViewModel.class);
        walletViewModel.getWalletLoadLiveData().observe(this,wallet -> {
            Logger.e("-----wallet----->"+wallet.toString());
            String root= BitUtil.INSTANCE.getMasterAddress1(wallet, Constants.NETWORK_PARAMETERS);
            login(root, "66666");
        });


        ipViewModel.onRandomIp().observe(this,randomIp -> {
            Logger.e("-----onRandomIp----->"+randomIp.getIp());
            BASE_URL=AppConst.httpPrefix+randomIp.getIp();
            //init ipfs
            String onlyIp=randomIp.getIp().split(":")[0];
            initIpfs(onlyIp);


        });


        ipViewModel.error().observe(this,errorEnvelope -> {
            Logger.e("-----error----->"+errorEnvelope.code+" "+errorEnvelope.message);

            new Handler().postDelayed(()->{
                if(index==ips.length-1){
                    index=0;
                }else{
                    index+=1;
                }
                String currentip_error=AppConst.httpPrefix+ips[index];
                Logger.e("-----error----->"+currentip_error);
                ipViewModel.getRandom(currentip_error);
            },1000);



        });

        ipViewModel.progress().observe(this,ok->{
            showLoading(ok,getString(R.string.str_link_switch),false);
        });



        if(ACacheUtil.get().getAsBoolean("frist",true)){
            startActivity(new Intent(this,GuiActivity.class));
            finish();
            return;
        }

        if(!this.isTaskRoot()){
            Logger.e("-------!isTaskRoot---->");
            showMain();
        }

      //  getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        hideStatusBar();

        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        id = sharedPreferences.getString("id", null);
        token = sharedPreferences.getString("token", null);
        if (checkPermission()) {
            if (checkOverlayPermission()) {
                new Handler().postDelayed(this::showNextScreen, 1000);
            }
        } else {
            requestPermissions(permissions, 100);
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_splash;
    }

    private boolean checkPermission() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.str_need_float_prom, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        if (checkOverlayPermission()) {
            showNextScreen();
        }
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.str_need_float_prom, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));

                List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (infos == null || infos.isEmpty()) {
                    return true;
                }
                startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, UIUtils.getString(R.string.str_auth_fail), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    showNextScreen();
                }
            }
        }
    }

    private void showNextScreen() {

        
        initChannelLine();

    }

    private void showMain() {

        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);



    }
    private void showLogin() {
        Intent intent;
        intent = new Intent(this, SMSLoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("CheckResult")
    private void walletLogin() {
      walletViewModel.getWalletLoadLiveData().loadWallet();
   }

    public  void login(String masteraddress, String code) {
        Logger.e("-----------login-masteraddress--->"+masteraddress);
        String phoneNumber = masteraddress;
        String authCode = code;

        
        String apphost= ACacheUtil.get().getAsString("apphost");
        if(TextUtils.isEmpty(apphost)){
            apphost=Config.Companion.getAPP_SERVER_HOST();
        }
       // Toast.makeText(this,"connect to "+apphost,Toast.LENGTH_SHORT).show();
        String url = "http://" +apphost + ":" + Config.Companion.getAPP_SERVER_PORT() + "/login";
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNumber);
        params.put("code", authCode);
        try {
            params.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.str_net_error, Toast.LENGTH_SHORT).show();
            return;
        }


        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.str_loading_wallet)
                .progress(true, 2000)
                .cancelable(false)
                .build();
        dialog.show();
        OKHttpHelper.post(url, params, new SimpleCallback<LoginResult>() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();

                Logger.e("------loginResult id and token---->"+loginResult.getUserId()+"  "+loginResult.getToken());
                ChatManagerHolder.gChatManager.connect(loginResult.getUserId(), loginResult.getToken());

                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("id", loginResult.getUserId())
                        .putString("token", loginResult.getToken())
                        .apply();

                finish();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);


            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(SplashActivity.this, R.string.str_loging_fail + code + " " + msg, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
    }



    @SuppressLint("CheckResult")
    private void initChannelLine() {


//        ImRetrofitService.getIp(this, AppConst.random)
//                .subscribe(bean -> {
//                    if(bean!=null&&bean.getData()!=null){
//                        Logger.e("-----getIp_onNext----->"+ JsonUtil.toJson(bean));
//                        String ipandport=bean.getData().getIp();
//                        BASE_URL="http://"+ipandport;
//                        //init ipfs
//                        String onlyIp=ipandport.split(":")[0];
//                        initIpfs(onlyIp);
//                    }
//                });



        String currentip=AppConst.httpPrefix+ips[index];
        Logger.e("----currentip=---->"+currentip);
        ipViewModel.getRandom(currentip);

    }

    private void initIpfs(String ip){
        try {
            String address="/ip4/"+ip+"/tcp/7800";
            Logger.e("----ipfs-address--->"+address);
            IPFSManager.getInstance().init(address);
            intoMain();
        }catch (Exception e){
            Logger.e("-----failed to init ipfs--->"+e.getMessage());
        }
    }



    private void intoMain(){

        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)) {
            Logger.e("------Login OK----->");
            showMain();
        } else {
            //showLogin();
            
            Logger.e("------recovery wallet----->");
            walletLogin();
        }

    }


}

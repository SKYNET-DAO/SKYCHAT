package cn.wildfirechat.push;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.coloros.mcssdk.PushManager;
import com.coloros.mcssdk.callback.PushAdapter;
import com.coloros.mcssdk.mode.ErrorCode;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.meizu.cloud.pushsdk.util.MzSystemUtils;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.push.utils.JsonUtil;
import cn.wildfirechat.remote.ChatManager;


public class PushService {
    private HuaweiApiClient HMSClient;
    private boolean hasHMSToken;
    private PushServiceType pushServiceType;
    private static PushService INST = new PushService();

    public enum PushServiceType {
        Unknown, Xiaomi, HMS, MeiZu, Oppo, Vivo
    }

    public static void init(Context gContext) {
        String sys = getSystem();
        Log.e("------PushService----", sys);
        if (SYS_EMUI.equals(sys) && INST.isHMSConfigured(gContext)) {
            INST.pushServiceType = PushServiceType.HMS;
            INST.initHMS(gContext);
        } else if (/*SYS_FLYME.equals(sys) && INST.isMZConfigured(gContext)*/MzSystemUtils.isBrandMeizu()) {
            INST.pushServiceType = PushServiceType.MeiZu;
            INST.initMZ(gContext);
        } else if (SYS_OPPO.equals(sys)) {
            Log.e("------SYS_OPPO---->",sys);
            INST.pushServiceType = PushServiceType.Oppo;
         //   INST.initOppoPush(gContext);
        } else if (SYS_VIVO.equals(sys)) {
            INST.pushServiceType = PushServiceType.Vivo;
            INST.initVivoPush(gContext);
        } else /*if (SYS_MIUI.equals(sys) && INST.isXiaomiConfigured(gContext))*/ {

            INST.pushServiceType = PushServiceType.Xiaomi;
            INST.initXiaomi(gContext);
        }
    }

    public static void clearNotification(Context context) {
        if (INST.pushServiceType == PushServiceType.Xiaomi) {
            MiPushClient.clearNotification(context);
        } else {
            // TODO
        }
    }

    public static void showMainActivity(Context context) {
        String action = "cn.wildfirechat.imshat.main";
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void didReceiveIMPushMessage(Context context, AndroidPushMessage pushMessage, PushServiceType pushServiceType) {
        PushMessageHandler.didReceiveIMPushMessage(context, pushMessage, pushServiceType);
    }

    public static void didReceivePushMessageData(Context context, String pushData) {
        PushMessageHandler.didReceivePushMessageData(context, pushData);
    }

    public static void destroy() {
        if (INST.HMSClient != null) {
            INST.HMSClient.disconnect();
            INST.HMSClient = null;
        }
    }

    private boolean initXiaomi(Context context) {

        String packageName = context.getPackageName();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String appid = appInfo.metaData.getString("MIPUSH_APPID").substring(7);
                String appkey = appInfo.metaData.getString("MIPUSH_APPKEY").substring(7);
                if (!TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey)) {
                    if (shouldInitXiaomi(context)) {
                        MiPushClient.registerPush(context, appid, appkey);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isXiaomiConfigured(Context context) {
        String packageName = context.getPackageName();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String appid = appInfo.metaData.getString("MIPUSH_APPID").substring(7);
                String appkey = appInfo.metaData.getString("MIPUSH_APPKEY").substring(7);
                return !TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean shouldInitXiaomi(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHMSConfigured(Context context) {
        String packageName = context.getPackageName();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                int hmsappid = appInfo.metaData.getInt("com.huawei.hms.client.appid", 0);
                Log.e("------PushService--->", hmsappid + "");
                return hmsappid > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initHMS(final Context context) {
        HMSClient = new HuaweiApiClient.Builder(context)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(new HuaweiApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected() {
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                if (!hasHMSToken && HMSClient != null) {
                                    PendingResult<TokenResult> tokenResult = HuaweiPush.HuaweiPushApi.getToken(HMSClient);
                                    tokenResult.setResultCallback(new ResultCallback<TokenResult>() {

                                        @Override
                                        public void onResult(TokenResult result) {

                                            hasHMSToken = true;
                                            Log.d("----pushserver-->", JsonUtil.toJson(result));

                                        }
                                    });
                                }
                            }
                        });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                if (HMSClient != null) {
                                    HMSClient.connect();
                                }
                            }
                        });

                    }
                })
                .addOnConnectionFailedListener(new HuaweiApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        android.util.Log.e("ChatManager", "init HMS feilure with code" + connectionResult.getErrorCode());
                    }
                })
                .build();


        HMSClient.connect();
    }

    private boolean isMZConfigured(Context context) {
        String packageName = context.getPackageName();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String appid = appInfo.metaData.getString("MEIZU_APP_ID");
                String appkey = appInfo.metaData.getString("MEIZU_APP_KEY");
                return !TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean initMZ(Context context) {
        String packageName = context.getPackageName();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String appId = "" + appInfo.metaData.get("MEIZU_PUSH_APP_ID");
                String appKey = appInfo.metaData.getString("MEIZU_PUSH_APP_KEY");
                if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appKey)) {
                    String pushId = com.meizu.cloud.pushsdk.PushManager.getPushId(context);
                    com.meizu.cloud.pushsdk.PushManager.register(context, String.valueOf(appId), appKey);
                    com.meizu.cloud.pushsdk.PushManager.switchPush(context, String.valueOf(appId), appKey, pushId, 1, true);
                    ChatManager.Instance().setDeviceToken(pushId, ChatManager.PushType.MeiZu);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    protected boolean initOppoPush(Context context) {

        if (PushManager.isSupportPush(context)) {
            Log.e("--isSupportPush->",1+"");
            String packageName = context.getPackageName();
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (appInfo.metaData != null) {
                    String appId = "" + appInfo.metaData.get("OPPO_PUSH_APP_ID");
                    String appKey = "" +appInfo.metaData.getString("OPPO_PUSH_APP_KEY");
                    String appSecret = "" +appInfo.metaData.getString("OPPO_PUSH_APP_SECRET");
                    Log.e("--appkey+appSecret->",appId+" "+appKey+" "+appSecret);
                    PushManager.getInstance().register(context, appKey, appSecret, new PushAdapter() {
                        @Override
                        public void onRegister(int i, String s) {
                            if (i == ErrorCode.SUCCESS) {

                                Log.e("---initOppoPush", "register successful，registerId=" + s);
                                ChatManager.Instance().setDeviceToken(s, ChatManager.PushType.OPPO);

                            } else {

                                Log.e("---initOppoPush", "Failed to register"+s);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();

            }


        }

        return false;
    }



    protected void initVivoPush(Context context) {

        PushClient.getInstance(context).initialize();

        PushClient.getInstance(context).turnOnPush(i -> {
            if (i == 0) {
                Log.e("initVivoPush", "Open push service successful.");

                String deviceToken=PushClient.getInstance(context).getRegId();
                Log.e("initVivoPush","-----initVivoPush-->"+deviceToken);
                if(!TextUtils.isEmpty(deviceToken)){
                    ChatManager.Instance().setDeviceToken(deviceToken, ChatManager.PushType.VIVO);
                }

            } else {
                Log.e("initVivoPush", "Open push service failed");
            }
        });
    }

    public static final String SYS_EMUI = "sys_emui";
    public static final String SYS_MIUI = "sys_miui";
    public static final String SYS_FLYME = "sys_flyme";
    public static final String SYS_OPPO = "sys_oppo";
    public static final String SYS_VIVO = "sys_vivo";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    public static String getSystem1() {
        String SYS = null;
        try {
            Properties prop = new Properties();

            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                SYS = SYS_MIUI;
            } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                    || prop.getProperty(KEY_EMUI_VERSION, null) != null
                    || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                SYS = SYS_EMUI;
            } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                SYS = SYS_FLYME;
            } else if (android.os.Build.BRAND.toLowerCase().contains("oppo")) {
                SYS = SYS_OPPO;
            } else if ((android.os.Build.BRAND.toLowerCase().contains("vivo"))) {
                SYS = SYS_VIVO;
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
                SYS = SYS_EMUI;
            } else if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
                SYS = SYS_MIUI;
            } else if (Build.MANUFACTURER.equalsIgnoreCase("meizu")) {
                SYS = SYS_FLYME;
            } else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
                SYS = SYS_OPPO;
            } else if (Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                SYS = SYS_VIVO;
            }

            return SYS;
        }
        return SYS;
    }


    public static String getSystem() {
        String SYS = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            if (!TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_CODE, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_NAME, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_INTERNAL_STORAGE, ""))) {
                SYS = SYS_MIUI;
            } else if (!TextUtils.isEmpty(getSystemProperty(KEY_EMUI_API_LEVEL, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_VERSION, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, ""))) {
                SYS = SYS_EMUI;
            } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                SYS = SYS_FLYME;
            }else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
                SYS = SYS_OPPO;
            } else if (Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                SYS = SYS_VIVO;
            }
            return SYS;
        } else {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                    SYS = SYS_MIUI;
                } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                        || prop.getProperty(KEY_EMUI_VERSION, null) != null
                        || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                    SYS = SYS_EMUI;
                } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                    SYS = SYS_FLYME;
                }
                if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
                    SYS = SYS_OPPO;
                } else if (Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                    SYS = SYS_VIVO;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return SYS;
            } finally {
                return SYS;
            }
        }
    }


    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static PushServiceType getPushServiceType() {
        return INST.pushServiceType;
    }
}

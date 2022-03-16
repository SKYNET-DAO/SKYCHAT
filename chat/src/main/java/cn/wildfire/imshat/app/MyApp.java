package cn.wildfire.imshat.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.base.BaseApp;
import com.android.ipfsclient.IPFSManager;
import com.coloros.mcssdk.PushManager;
import com.coloros.mcssdk.callback.PushAdapter;
import com.coloros.mcssdk.mode.ErrorCode;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;


import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.wildfire.imshat.app.beans.IPBean;
import cn.wildfire.imshat.app.third.location.viewholder.LocationMessageContentViewHolder;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.conversation.message.viewholder.MessageViewHolderManager;
import com.android.base.LanguageUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.vondear.rxtool.RxTool;

import org.apache.log4j.Level;

import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.net.api.ImRetrofitService;
import cn.wildfire.imshat.net.bean.RandomIp;
import cn.wildfire.imshat.net.helper.rxjavahelper.RxObserver;
import cn.wildfire.imshat.net.model.base.bean.ResponseData;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.BuildConfig;
import cn.wildfirechat.remote.ChatManager;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import okhttp3.OkHttpClient;

import static cn.wildfire.imshat.net.AppConst.BASE_URL;


public class MyApp extends BaseApp {

    public static WfcUIKit wfcUIKit;
    public static MyApp instance;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();

        Logger.e("---------MyApp--- onCreate()---->");
        
        if (getCurProcessName(this).equals("cn.wildfirechat.imshat")) {

            wfcUIKit = new WfcUIKit();
            wfcUIKit.init(this);
            MessageViewHolderManager.getInstance().registerMessageViewHolder(LocationMessageContentViewHolder.class);
            seuptwfcdirs();
            initOkgo();
//            initDbIp();


        }


//        configLog("------bitconinj----->");
        //init language
        LanguageUtil.initAppLanguage(this);

        DoraemonKit.install(this);

        RxTool.init(this);
        instance=this;



    }

    private void initDbIp(){
        new IPBean("43.249.31.49:8000").save();
    }





    private void initOkgo(){

        

        //----------------------------------------------------------------------------------------//

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        
        loggingInterceptor.setColorLevel(java.util.logging.Level.INFO);                               
        builder.addInterceptor(loggingInterceptor);                                 
        
        //builder.addInterceptor(new ChuckInterceptor(this));


        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   

        
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));              
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            


        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();

        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);

        OkGo.getInstance().init(this)                           
                .setOkHttpClient(builder.build())               
                .setCacheMode(CacheMode.NO_CACHE)               
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   
                .setRetryCount(3);                           

    }






    private void seuptwfcdirs() {
        File file = new File(Config.Companion.getVIDEO_SAVE_DIR());
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.Companion.getAUDIO_SAVE_DIR());
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.Companion.getFILE_SAVE_DIR());
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.Companion.getPHOTO_SAVE_DIR());
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);


        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }






    private void configLog(String logFileNamePrefix) {
        LogConfigurator logConfigurator = new LogConfigurator();

//      String filenamepath  = (Environment.getExternalStorageDirectory().getPath()
//                + File.separator + "log4jSkychat.txt");
     //   logConfigurator.setFileName(filenamepath);
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setUseFileAppender(false);
        logConfigurator.setLevel("------bitconinj----->", Level.ERROR);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 2);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.setLogCatPattern(" %n ----------------------------bitconinj start----------------------------- %n  Time:%d %n lever:%-5p %n Package:[%c{5}]-[line:%L] %n  Content:%m %n  ----------------------------bitconinj end-----------------------------  %n %n %n");
        logConfigurator.configure();
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
                                
                                Log.e("---initOppoPush", "Register success，registerId=" + s);
                                ChatManager.Instance().setDeviceToken(s, ChatManager.PushType.OPPO);

                            } else {
                                
                                Log.e("---initOppoPush", "Register Failed"+s);
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










}

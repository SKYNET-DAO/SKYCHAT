package cn.wildfire.imshat.kit;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.emoji.LQREmotionKit;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.kit.common.AppScopeViewModel;
import cn.wildfire.imshat.kit.voip.AsyncPlayer;
import cn.wildfire.imshat.kit.voip.SingleVoipCallActivity;
import com.android.base.utils.ACacheUtil;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.push.PushService;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;


public class WfcUIKit implements AVEngineKit.AVEngineCallback, OnReceiveMessageListener, OnRecallMessageListener {

    private  boolean isBackground = true;
    private static Application application;
    private static ViewModelProvider viewModelProvider;
    private ViewModelStore viewModelStore;

    public void init(Application application) {
        WfcUIKit.application = application;
        initWFClient(application);
        
        LQREmotionKit.init(application, (context, path, imageView) -> Glide.with(context).load(path).apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontAnimate()).into(imageView));

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onForeground() {
                PushService.clearNotification(application);
                WfcNotificationManager.getInstance().clearAllNotification(application);
                isBackground = false;

                

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onBackground() {
                isBackground = true;
            }
        });

        viewModelStore = new ViewModelStore();
        ViewModelProvider.Factory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        viewModelProvider = new ViewModelProvider(viewModelStore, factory);

    }

    private void initWFClient(Application application) {

        System.out.println("-----initWFClient---->");

        
        String host= ACacheUtil.get().getAsString("imhost");
        if(TextUtils.isEmpty(host)){
            host=Config.Companion.getIM_SERVER_HOST();
        }

        ChatManager.init(application, host, Config.Companion.getIM_SERVER_PORT());
        try {
            ChatManagerHolder.gChatManager = ChatManager.Instance();
            ChatManagerHolder.gChatManager.startLog();
            ChatManagerHolder.gChatManager.addOnReceiveMessageListener(this);
            ChatManagerHolder.gChatManager.addRecallMessageListener(this);
            PushService.init(application);

            ringPlayer = new AsyncPlayer(null);
            AVEngineKit.init(application, this);
            ChatManagerHolder.gAVEngine = AVEngineKit.Instance();
            ChatManagerHolder.gAVEngine.addIceServer(Config.Companion.getICE_ADDRESS(), Config.Companion.getICE_USERNAME(), Config.Companion.getICE_PASSWORD());

            SharedPreferences sp = application.getSharedPreferences("config", Context.MODE_PRIVATE);
            String id = sp.getString("id", null);
            String token = sp.getString("token", null);
            Logger.e("------id and token----->"+id+"  "+token);
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)) {
                System.out.println("-----initWFClient-connect--->"+id+"  "+token);
                ChatManagerHolder.gChatManager.connect(id, token);
            }
        } catch (NotInitializedExecption notInitializedExecption) {
            notInitializedExecption.printStackTrace();
        }
    }

    
    public static <T extends ViewModel> T getAppScopeViewModel(@NonNull Class<T> modelClass) {
        if (!AppScopeViewModel.class.isAssignableFrom(modelClass)) {
            throw new IllegalArgumentException("the model class should be subclass of AppScopeViewModel");
        }
        return viewModelProvider.get(modelClass);
    }

    @Override
    public void onReceiveCall(AVEngineKit.CallSession session) {
        onCall(application, session.getClientId(), false, session.isAudioOnly());
    }

    private AsyncPlayer ringPlayer;

    @Override
    public void shouldStartRing(boolean isIncomming) {
        if (isIncomming) {
            Uri uri = Uri.parse("android.resource://" + application.getPackageName() + "/" + R.raw.incoming_call_ring);
            ringPlayer.play(application, uri, true, AudioManager.STREAM_RING);
        } else {
            Uri uri = Uri.parse("android.resource://" + application.getPackageName() + "/" + R.raw.outgoing_call_ring);
            ringPlayer.play(application, uri, true, AudioManager.STREAM_RING);
        }
    }

    @Override
    public void shouldSopRing() {
        ringPlayer.stop();
    }

    // pls refer to https://stackoverflow.com/questions/11124119/android-starting-new-activity-from-application-class
    public static void onCall(Context context, String targetId, boolean isMo, boolean isAudioOnly) {
        Intent voip = new Intent(WfcIntent.ACTION_VOIP_SINGLE);
        voip.putExtra(SingleVoipCallActivity.EXTRA_MO, isMo);
        voip.putExtra(SingleVoipCallActivity.EXTRA_TARGET, targetId);
        voip.putExtra(SingleVoipCallActivity.EXTRA_AUDIO_ONLY, isAudioOnly);

        if (context instanceof Activity) {
            context.startActivity(voip);
        } else {
            Intent main = new Intent(WfcIntent.ACTION_MAIN);
            voip.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivities(context, 100, new Intent[]{main, voip}, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        if (isBackground) {
            

            if (messages == null) {
                return;
            }

            List<Message> msgs = new ArrayList<>(messages);
            long now = System.currentTimeMillis();
            long delta = ChatManager.Instance().getServerDeltaTime();
            Iterator<Message> iterator = msgs.iterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                if (message.content.getPersistFlag() == PersistFlag.No_Persist
                        || now - (message.serverTime - delta) > 10 * 1000) {
                    iterator.remove();
                }
            }
            WfcNotificationManager.getInstance().handleReceiveMessage(application, msgs);
        } else {
            // do nothing
        }
    }

    @Override
    public void onRecallMessage(Message message) {
        if (isBackground) {
            WfcNotificationManager.getInstance().handleRecallMessage(application, message);
        }
    }
}

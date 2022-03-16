package cn.wildfirechat.jobs;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.birbit.android.jobqueue.TagConstraint;
import com.tencent.mars.proto.ProtoLogic;


import java.security.Provider;

import cn.wildfirechat.client.ISendMessageCallback;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.ProtoMessage;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;
import cn.wildfirechat.utils.JsonUtil;
import cn.wildfirechat.utils.TimeUtils;

public class RetrySendMessageJob extends Job {

    public static final int PRIORITY = 1;
    private String text;
    private Message message;
    private SendMessageCallback callback;
    private ChatManager chatManager;
    public RetrySendMessageJob(ChatManager chatManager,Message message, SendMessageCallback callback) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(PRIORITY).requireNetwork().groupBy("1").addTags(message.messageId+""));
        this.message=message;
        this.callback=callback;
        this.chatManager=chatManager;
        android.util.Log.e("RetrySendMessageJob",message.messageId+"");

    }


    @Override
    public void onAdded() {
        Log.e("RetrySendMessageJob","onAdded");
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
    }
    @Override
    public void onRun() {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        // All work done here should be synchronous, a job is removed from the queue once
        // onRun() finishes.
//        Thread.sleep(3000);
       // long time=TimeUtils.ReqFailSimulationAddOneH(System.currentTimeMillis());
        Log.e("RetrySendMessageJob","---onRun"+" "+TimeUtils.getFormatTimeyyyyMMddHHmm(message.serverTime)+" "+JsonUtil.toJson(message));

        chatManager.deleteMessage1(message.messageId);
        chatManager.sendMessage(message,callback);


    }
    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {

        Log.e("RetrySendMessageJob","shouldReRunOnThrowable"+" "+runCount);

        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.createExponentialBackoff(runCount, 10);


    }

    @Override
    protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
        Log.e("RetrySendMessageJob","onCancel");
    }


}

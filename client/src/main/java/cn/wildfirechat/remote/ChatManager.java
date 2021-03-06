package cn.wildfirechat.remote;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.birbit.android.jobqueue.CancelResult;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.network.NetworkUtilImpl;
import com.orhanobut.logger.Logger;
import com.tencent.mars.proto.ProtoLogic;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.ErrorCode;
import cn.wildfirechat.UserSource;
import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.ICreateChannelCallback;
import cn.wildfirechat.client.IGeneralCallback;
import cn.wildfirechat.client.IGetRemoteMessageCallback;
import cn.wildfirechat.client.IOnChannelInfoUpdateListener;
import cn.wildfirechat.client.IOnConnectionStatusChangeListener;
import cn.wildfirechat.client.IOnFriendUpdateListener;
import cn.wildfirechat.client.IOnGroupInfoUpdateListener;
import cn.wildfirechat.client.IOnGroupMembersUpdateListener;
import cn.wildfirechat.client.IOnReceiveMessageListener;
import cn.wildfirechat.client.IOnSettingUpdateListener;
import cn.wildfirechat.client.IOnUserInfoUpdateListener;
import cn.wildfirechat.client.IRemoteClient;
import cn.wildfirechat.client.IUploadMediaCallback;
import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.jobs.RetrySendMessageJob;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.ChangeGroupNameNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupPortraitNotificationContent;
import cn.wildfirechat.message.notification.DismissGroupNotificationContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.QuitGroupNotificationContent;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.ChatRoomMembersInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.ModifyChannelInfoType;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.NullChannelInfo;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.utils.JsonUtil;
import cn.wildfirechat.utils.TimeUtils;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by WF Chat on 2017/12/11.
 */

public class ChatManager {
    private static final String TAG = ChatManager.class.getName();

    private String SERVER_HOST;
    private int SERVER_PORT;

    private static IRemoteClient mClient;

    private static ChatManager INST;
    private static Context gContext;

    private String userId;
    private String token;
    private Handler mainHandler;
    private Handler workHandler;
    private String deviceToken;
    private PushType pushType;
    private List<String> msgList = new ArrayList<>();

    private UserSource userSource;

    private boolean startLog;

    private boolean isBackground = true;
    private List<OnReceiveMessageListener> onReceiveMessageListeners = new ArrayList<>();
    private List<OnConnectionStatusChangeListener> onConnectionStatusChangeListeners = new ArrayList<>();
    public List<OnSendMessageListener> sendMessageListeners = new ArrayList<>();
    private List<OnGroupInfoUpdateListener> groupInfoUpdateListeners = new ArrayList<>();
    private List<OnGroupMembersUpdateListener> groupMembersUpdateListeners = new ArrayList<>();
    private List<OnUserInfoUpdateListener> userInfoUpdateListeners = new ArrayList<>();
    private List<OnSettingUpdateListener> settingUpdateListeners = new ArrayList<>();
    private List<OnFriendUpdateListener> friendUpdateListeners = new ArrayList<>();
    private List<OnConversationInfoUpdateListener> conversationInfoUpdateListeners = new ArrayList<>();
    private List<OnRecallMessageListener> recallMessageListeners = new ArrayList<>();
    private List<RemoveMessageListener> removeMessageListeners = new ArrayList<>();
    private List<OnChannelInfoUpdateListener> channelInfoUpdateListeners = new ArrayList<>();
    private List<OnMessageUpdateListener> messageUpdateListeners = new ArrayList<>();
    private List<OnClearMessageListener> clearMessageListeners = new ArrayList<>();
    private List<OnRemoveConversationListener> removeConversationListeners = new ArrayList<>();

    private List<IMServiceStatusListener> imServiceStatusListeners = new ArrayList<>();

    // key = userId
    private LruCache<String, UserInfo> userInfoCache;
    // key = memberId@groupId
    private LruCache<String, GroupMember> groupMemberCache;
    // key = userId@groupId
    private LruCache<String, UserInfo> groupUserCache;

    private static JobManager jobManager;

    public enum PushType {
        Xiaomi(1),
        HMS(2),
        MeiZu(3),
        VIVO(4),
        OPPO(5);

        private int value;

        PushType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }



    /**
     * ?????????????????????id
     *
     * @return ??????id
     */
    public String getUserId() {
        return userId;
    }

    public interface IGeneralCallback3 {
        void onSuccess(List<String> results);

        void onFailure(int errorCode);
    }

    private ChatManager(String serverHost, int serverPort) {
        this.SERVER_HOST = serverHost;
        this.SERVER_PORT = serverPort;
    }

    public static ChatManager Instance() throws NotInitializedExecption {
        if (INST == null) {
            throw new NotInitializedExecption();
        }
        return INST;
    }


    /**
     * ????????????must be called from the main thread
     *
     * @param context
     * @param serverHost
     * @param serverPort
     * @return
     */

    public static void init(Application context, String serverHost, int serverPort) {
        if (INST != null) {
            // TODO: Already initialized
            return;
        }
        gContext = context.getApplicationContext();
        configureJobManager();
        INST = new ChatManager(serverHost, serverPort);
        INST.mainHandler = new Handler();
        HandlerThread thread = new HandlerThread("workHandler");
        thread.start();
        INST.workHandler = new Handler(thread.getLooper());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onForeground() {
                INST.isBackground = false;
                if (mClient == null) {
                    return;
                }
                try {
                    mClient.setForeground(1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onBackground() {
                INST.isBackground = true;
                if (mClient == null) {
                    return;
                }
                try {
                    mClient.setForeground(0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        INST.checkRemoteService();

        SharedPreferences sp = gContext.getSharedPreferences("wildfirechat.config", Context.MODE_PRIVATE);
        INST.userId = sp.getString("userId", null);
        INST.token = sp.getString("token", null);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param userSource ???????????????
     */
    public void setUserSource(UserSource userSource) {
        this.userSource = userSource;
    }

    //App???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    public void forceConnect() {
        if (mClient != null) {
            try {
                mClient.setForeground(1);
                if (INST.isBackground) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mClient != null) {
                                try {
                                    mClient.setForeground(INST.isBackground ? 1 : 0);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, 3000);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param status ????????????
     */
    private void onConnectionStatusChange(final int status) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Iterator<OnConnectionStatusChangeListener> iterator = onConnectionStatusChangeListeners.iterator();
                OnConnectionStatusChangeListener listener;
                while (iterator.hasNext()) {
                    listener = iterator.next();
                    listener.onConnectionStatusChange(status);
                }
            }
        });
    }

    /**
     * ???????????????
     *
     * @param messageUid
     */
    private void onRecallMessage(final long messageUid) {
        Message message = getMessageByUid(messageUid);
        // ?????????????????????????????????
        if (message == null) {
            return;
        }
        ConversationInfo conversationInfo = getConversation(message.conversation);
        conversationInfo.lastMessage = message;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnRecallMessageListener listener : recallMessageListeners) {
                    listener.onRecallMessage(message);
                }
            }
        });
    }

    /**
     * ???????????????
     *
     * @param messages
     * @param hasMore  ?????????????????????????????????
     */
    /**
     * ???????????????
     *
     * @param messages
     * @param hasMore  ?????????????????????????????????
     */
    private void onReceiveMessage(final List<Message> messages, final boolean hasMore) {
        mainHandler.post(() -> {
            Iterator<OnReceiveMessageListener> iterator = onReceiveMessageListeners.iterator();
            OnReceiveMessageListener listener;
            while (iterator.hasNext()) {
                listener = iterator.next();
                listener.onReceiveMessage(messages, hasMore);
            }
            for (Message message : messages) {
                if ((message.content instanceof QuitGroupNotificationContent && ((QuitGroupNotificationContent) message.content).operator.equals(getUserId()))
                        || (message.content instanceof KickoffGroupMemberNotificationContent && ((KickoffGroupMemberNotificationContent) message.content).kickedMembers.contains(getUserId()))
                        || message.content instanceof DismissGroupNotificationContent) {
                    for (OnRemoveConversationListener l : removeConversationListeners) {
                        l.onConversationRemove(message.conversation);
                    }
                }
            }
        });


    }

    /**
     * ??????????????????
     *
     * @param userInfos
     */
    private void onUserInfoUpdate(List<UserInfo> userInfos) {
        if (userInfos == null || userInfos.isEmpty()) {
            return;
        }
        for (UserInfo info : userInfos) {
            userInfoCache.put(info.uid, info);
        }
        mainHandler.post(() -> {
            for (OnUserInfoUpdateListener listener : userInfoUpdateListeners) {
                listener.onUserInfoUpdate(userInfos);
            }
        });
    }

    /**
     * ???????????????
     *
     * @param groupInfos
     */
    private void onGroupInfoUpdated(List<GroupInfo> groupInfos) {
        if (groupInfos == null || groupInfos.isEmpty()) {
            return;
        }
        mainHandler.post(() -> {
            for (OnGroupInfoUpdateListener listener : groupInfoUpdateListeners) {
                listener.onGroupInfoUpdate(groupInfos);
            }

        });
    }

    private void onGroupMembersUpdate(String groupId, List<GroupMember> groupMembers) {
        if (groupMembers == null || groupMembers.isEmpty()) {
            return;
        }
        for (GroupMember member : groupMembers) {
            groupMemberCache.remove(groupMemberCacheKey(groupId, member.memberId));
            groupUserCache.remove(groupMemberCacheKey(groupId, member.memberId));
        }
        mainHandler.post(() -> {
            for (OnGroupMembersUpdateListener listener : groupMembersUpdateListeners) {
                listener.onGroupMembersUpdate(groupId, groupMembers);
            }
        });
    }

    private void onFriendListUpdated(List<String> friendList) {
        mainHandler.post(() -> {
            for (OnFriendUpdateListener listener : friendUpdateListeners) {
                listener.onFriendListUpdate(friendList);
            }
        });
        onUserInfoUpdate(getUserInfos(friendList, null));
    }

    private void onFriendReqeustUpdated() {

        mainHandler.post(() -> {
            for (OnFriendUpdateListener listener : friendUpdateListeners) {
                Log.e("ChatManager", "---------onFriendReqeustUpdated()-listener------->" + listener.toString());
                listener.onFriendRequestUpdate();
            }
        });
    }

    private void onSettingUpdated() {
        mainHandler.post(() -> {
            for (OnSettingUpdateListener listener : settingUpdateListeners) {
                listener.onSettingUpdate();
            }
        });
    }

    private void onChannelInfoUpdate(List<ChannelInfo> channelInfos) {
        mainHandler.post(() -> {
            for (OnChannelInfoUpdateListener listener : channelInfoUpdateListeners) {
                listener.onChannelInfoUpdate(channelInfos);
            }
        });
    }

    /**
     * ?????????????????????, ????????????{@link #removeOnReceiveMessageListener(OnReceiveMessageListener)}????????????
     *
     * @param listener
     */
    public void addOnReceiveMessageListener(OnReceiveMessageListener listener) {
        if (listener == null) {
            return;
        }
        onReceiveMessageListeners.add((listener));
    }

    /**
     * ??????????????????
     *
     * @param listener
     */
    public void removeOnReceiveMessageListener(OnReceiveMessageListener listener) {
        if (listener == null) {
            return;
        }
        onReceiveMessageListeners.remove(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addSendMessageListener(OnSendMessageListener listener) {
        if (listener == null) {
            return;
        }
        sendMessageListeners.add(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeSendMessageListener(OnSendMessageListener listener) {
        sendMessageListeners.remove(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addConnectionChangeListener(OnConnectionStatusChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (!onConnectionStatusChangeListeners.contains(listener)) {
            onConnectionStatusChangeListeners.add(listener);
        }
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeConnectionChangeListener(OnConnectionStatusChangeListener listener) {
        if (listener == null) {
            return;
        }
        onConnectionStatusChangeListeners.remove(listener);
    }

    /**
     * ???????????????????????????
     *
     * @param listener
     */
    public void addGroupInfoUpdateListener(OnGroupInfoUpdateListener listener) {
        if (listener == null) {
            return;
        }
        groupInfoUpdateListeners.add(listener);
    }

    /**
     * ?????????????????????
     *
     * @param listener
     */
    public void removeGroupInfoUpdateListener(OnGroupInfoUpdateListener listener) {
        groupInfoUpdateListeners.remove(listener);
    }

    public void addGroupMembersUpdateListener(OnGroupMembersUpdateListener listener) {
        if (listener != null) {
            groupMembersUpdateListeners.add(listener);
        }
    }

    public void removeGroupMembersUpdateListener(OnGroupMembersUpdateListener listener) {
        groupMembersUpdateListeners.remove(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void addUserInfoUpdateListener(OnUserInfoUpdateListener listener) {
        if (listener == null) {
            return;
        }
        userInfoUpdateListeners.add(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void removeUserInfoUpdateListener(OnUserInfoUpdateListener listener) {
        userInfoUpdateListeners.remove(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void addFriendUpdateListener(OnFriendUpdateListener listener) {
        if (listener == null) {
            return;
        }
        friendUpdateListeners.add(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeFriendUpdateListener(OnFriendUpdateListener listener) {
        friendUpdateListeners.remove(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void addSettingUpdateListener(OnSettingUpdateListener listener) {
        if (listener == null) {
            return;
        }
        settingUpdateListeners.add(listener);
    }

    /**
     * ???????????????????????????
     *
     * @param listener
     */
    public void removeSettingUpdateListener(OnSettingUpdateListener listener) {
        settingUpdateListeners.remove(listener);
    }

    /**
     * ??????clientId, ?????????clientId????????????????????????
     *
     * @return
     * @throws Exception
     */
    public String getClientId() throws Exception {
        if (!checkRemoteService()) {
            throw new Exception("mars service not connected");
        }
        return mClient.getClientId();
    }

    /**
     * ????????????
     *
     * @param channelId       ??????id????????????null????????????????????????id?????????????????????????????????id??????????????????id????????????
     * @param channelName     ????????????
     * @param channelPortrait ???????????????????????????
     * @param desc            ????????????
     * @param extra           ??????????????????????????????????????????????????????
     * @param callback        ???????????????????????????
     */
    public void createChannel(@Nullable String channelId, String channelName, String channelPortrait, String desc, String extra, final GeneralCallback2 callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }
        try {
            mClient.createChannel(channelId, channelName, channelPortrait, desc, extra, new ICreateChannelCallback.Stub() {
                @Override
                public void onSuccess(ChannelInfo channelInfo) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(channelInfo.channelId));
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onFail(errorCode));
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null) {
                mainHandler.post(() -> callback.onFail(ErrorCode.SERVICE_EXCEPTION));
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param channelId  ??????id
     * @param modifyType ????????????????????????????????????????????????
     * @param newValue   ???????????????
     * @param callback   ??????????????????
     */
    public void modifyChannelInfo(String channelId, ModifyChannelInfoType modifyType, String newValue, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.modifyChannelInfo(channelId, modifyType.ordinal(), newValue, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }


    /**
     * ??????????????????
     *
     * @param channelId
     * @param refresh   ??????????????????????????????true?????????????????????????????????????????????????????????????????????????????????{@link OnChannelInfoUpdateListener}????????????????????????
     * @return ????????????????????????null
     */
    public @Nullable
    ChannelInfo getChannelInfo(String channelId, boolean refresh) {
        if (!checkRemoteService()) {
            return new NullChannelInfo(channelId);
        }

        try {
            ChannelInfo channelInfo = mClient.getChannelInfo(channelId, refresh);
            if (channelInfo == null) {
                channelInfo = new NullChannelInfo(channelId);
            }
            return channelInfo;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param keyword  ???????????????
     * @param callback ??????????????????
     */
    public void searchChannel(String keyword, SearchChannelCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.searchChannel(keyword, new cn.wildfirechat.client.ISearchChannelCallback.Stub() {

                @Override
                public void onSuccess(final List<ChannelInfo> channelInfos) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(channelInfos);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null) {
                mainHandler.post(() -> callback.onFail(ErrorCode.SERVICE_EXCEPTION));
            }
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param channelId
     * @return true, ????????????false????????????
     */
    public boolean isListenedChannel(String channelId) {
        if (!checkRemoteService()) {
            return false;
        }

        try {
            return mClient.isListenedChannel(channelId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * ???????????????????????????
     *
     * @param channelId
     * @param listen    true????????????false???????????????
     * @param callback  ??????????????????
     */
    public void listenChannel(String channelId, boolean listen, GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.listenChannel(channelId, listen, new cn.wildfirechat.client.IGeneralCallback.Stub() {

                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    /**
     * ????????????
     *
     * @param channelId
     * @param callback
     */
    public void destoryChannel(String channelId, GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.destoryChannel(channelId, new cn.wildfirechat.client.IGeneralCallback.Stub() {

                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    /**
     * ????????????????????????id??????
     *
     * @return
     */
    public List<String> getMyChannels() {
        if (!checkRemoteService()) {
            return new ArrayList<>();
        }

        try {
            return mClient.getMyChannels();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * ????????????????????????id??????
     *
     * @return
     */
    public List<String> getListenedChannels() {
        if (!checkRemoteService()) {
            return new ArrayList<>();
        }

        try {
            return mClient.getListenedChannels();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addConversationInfoUpdateListener(OnConversationInfoUpdateListener listener) {
        if (listener == null) {
            return;
        }
        conversationInfoUpdateListeners.add(listener);
    }

    /**
     * ??????????????????
     *
     * @param listener
     */
    public void removeConversationInfoUpdateListener(OnConversationInfoUpdateListener listener) {
        conversationInfoUpdateListeners.remove(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addRecallMessageListener(OnRecallMessageListener listener) {
        if (listener == null) {
            return;
        }
        recallMessageListeners.add(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeRecallMessageListener(OnRecallMessageListener listener) {
        recallMessageListeners.remove(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void addRemoveMessageListener(RemoveMessageListener listener) {
        if (listener == null) {
            return;
        }
        removeMessageListeners.add(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeRemoveMessageListener(RemoveMessageListener listener) {
        removeMessageListeners.remove(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addChannelInfoUpdateListener(OnChannelInfoUpdateListener listener) {
        if (listener == null) {
            return;
        }
        channelInfoUpdateListeners.add(listener);
    }

    /**
     * ??????????????????????????????
     *
     * @param listener
     */
    public void removeChannelInfoListener(OnChannelInfoUpdateListener listener) {
        channelInfoUpdateListeners.remove(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void addOnMessageUpdateListener(OnMessageUpdateListener listener) {
        if (listener == null) {
            return;
        }
        messageUpdateListeners.add(listener);
    }

    /**
     * ????????????????????????
     *
     * @param listener
     */
    public void removeOnMessageUpdateListener(OnMessageUpdateListener listener) {
        messageUpdateListeners.remove(listener);
    }

    public void addClearMessageListener(OnClearMessageListener listener) {
        if (listener == null) {
            return;
        }

        clearMessageListeners.add(listener);
    }

    public void removeClearMessageListener(OnClearMessageListener listener) {
        clearMessageListeners.remove(listener);
    }

    public void addRemoveConversationListener(OnRemoveConversationListener listener) {
        if (listener == null) {
            return;
        }
        removeConversationListeners.add(listener);
    }

    public void removeRemoveConversationListener(OnRemoveConversationListener listener) {
        removeConversationListeners.remove(listener);
    }


    public void addIMServiceStatusListener(IMServiceStatusListener listener) {
        if (listener == null) {
            return;
        }
        imServiceStatusListeners.add(listener);
    }

    public void removeIMServiceStatusListener(IMServiceStatusListener listener) {
        imServiceStatusListeners.remove(listener);
    }

    private void validateMessageContent(Class<? extends MessageContent> msgContentClazz) {
        try {
            Constructor c = msgContentClazz.getConstructor();
            if (c.getModifiers() != Modifier.PUBLIC) {
                throw new IllegalArgumentException("the default constructor of your custom messageContent class should be public");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("custom messageContent class must have a default constructor");
        }
        ContentTag tag = msgContentClazz.getAnnotation(ContentTag.class);
        if (tag == null) {
            throw new IllegalArgumentException("custom messageContent class must have a ContentTag annotation");
        }
    }

    /**
     * ????????????????????????
     *
     * @param msgContentCls ?????????????????????????????????????????????????????????
     */
    public void registerMessageContent(Class<? extends MessageContent> msgContentCls) {

        validateMessageContent(msgContentCls);
        msgList.add(msgContentCls.getName());
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.registerMessageContent(msgContentCls.getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     *
     * @param conversation ????????????
     * @param sender       ???????????????id
     * @param content      ?????????
     * @param status       ????????????
     * @param notify       ??????????????????????????????????????????{@link #onReceiveMessage(List, boolean)}????????????
     * @param serverTime   ???????????????
     * @return
     */
    public Message insertMessage(Conversation conversation, String sender, MessageContent content, MessageStatus status, boolean notify, long serverTime) {
        if (!checkRemoteService()) {
            return null;
        }

        Message message = new Message();
        message.conversation = conversation;
        message.content = content;
        message.sender = sender;
        message.status = status;
        message.serverTime = serverTime;
        if (this.userId.equals(sender)) {
            message.direction = MessageDirection.Send;
        } else {
            message.direction = MessageDirection.Receive;
        }

        try {
            message = mClient.insertMessage(message, notify);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }

        return message;
    }

    /**
     * ????????????
     *
     * @param messageId     ??????id
     * @param newMsgContent ???????????????????????????????????????????????????
     * @return
     */
    public boolean updateMessage(long messageId, MessageContent newMsgContent) {
        if (!checkRemoteService()) {
            return false;
        }

        try {
            Message message = mClient.getMessage(messageId);
            message.content = newMsgContent;
            boolean result = mClient.updateMessage(message);
            mainHandler.post(() -> {
                for (OnMessageUpdateListener listener : messageUpdateListeners) {
                    listener.onMessageUpdate(message);
                }
            });
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ???????????????
     * userId???token??????????????????
     *
     * @param userId
     * @param token
     */
    public void connect(String userId, String token) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("userId and token must be empty!");
        }
        this.userId = userId;
        this.token = token;
        this.userInfoCache = new LruCache<>(1024);
        this.groupMemberCache = new LruCache<>(1024);
        this.groupUserCache = new LruCache<>(1024);

        if (mClient != null) {
            try {

                mClient.connect(this.userId, this.token);
                SharedPreferences sp = gContext.getSharedPreferences("wildfirechat.config", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("userId", userId)
                        .putString("token", token)
                        .commit();
                ;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param cleanSession ??????????????????session?????????????????????????????????????????????????????????
     */
    public void disconnect(boolean cleanSession) {
        if (mClient != null) {
            try {
                mClient.disconnect(cleanSession);
                SharedPreferences sp = gContext.getSharedPreferences("wildfirechat.config", Context.MODE_PRIVATE);
                sp.edit().clear().commit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.userId = null;
            this.token = null;
        }

    }

    /**
     * ????????????
     *
     * @param conversation
     * @param content
     * @param toUsers        ?????????????????????????????????????????????????????????????????????
     * @param expireDuration
     * @param callback
     */
    public void sendMessage(Conversation conversation, MessageContent content, String[] toUsers, int expireDuration, SendMessageCallback callback) {
        Message msg = new Message();
        msg.conversation = conversation;
        msg.content = content;
        msg.toUsers = toUsers;
        sendMessage(msg, expireDuration, callback);
    }

    /**
     * ????????????
     *
     * @param msg
     * @param callback ????????????????????????
     */
    public void sendMessage(final Message msg, final SendMessageCallback callback) {
        sendMessage(msg, 0, callback);
    }

    /**
     * ????????????
     *
     * @param msg            ??????
     * @param callback       ??????????????????
     * @param expireDuration 0, ???????????????????????????????????????????????????????????????????????????????????????
     */
    public void sendMessage(final Message msg, final int expireDuration, final SendMessageCallback callback) {
        msg.direction = MessageDirection.Send;
        msg.status = MessageStatus.Sending;
        msg.serverTime=TimeUtils.ReqFailSimulationAddOneH(System.currentTimeMillis());
        android.util.Log.e("----send()-sendMsg->","----??????????????????sendMessage--->"+msg.messageId+"  "+TimeUtils.getFormatTimeyyyyMMddHHmm(msg.serverTime));

        msg.sender = userId;
        if (!checkRemoteService()) {
            if (callback != null) {
                msg.status = MessageStatus.Send_Failure;
                callback.onFail(ErrorCode.SERVICE_DIED);
            }
            for (OnSendMessageListener listener : sendMessageListeners) {
                listener.onSendFail(msg, ErrorCode.SERVICE_DIED);
            }
            return;
        }


        //??????????????????????????????- modify by yzr
        if (msg.conversation.type == Conversation.ConversationType.Single) {//???????????????????????????????????????
            if (!ChatManager.Instance().isMyFriend(msg.conversation.target)) {
                if (callback != null) {
                    msg.status = MessageStatus.Send_Failure;
                    callback.onFail(ErrorCode.SERVICE_DIED);
                }
                for (OnSendMessageListener listener : sendMessageListeners) {
                    listener.onSendFail(msg, 240);
                }
                return;
            }

        }


        if (msg.content instanceof MediaMessageContent) {
            String localPath = ((MediaMessageContent) msg.content).localPath;
            if (!TextUtils.isEmpty(localPath)) {
                File file = new File(localPath);
                if (!file.exists()) {
                    if (callback != null) {
                        callback.onFail(ErrorCode.FILE_NOT_EXIST);
                    }
                    return;
                }

                if (file.length() > 15 * 1024 * 1024) {
                    Log.e(getClass().getSimpleName(),"------????????????40M---callback=->"+callback);
                    if (callback != null) {
                        callback.onFail(ErrorCode.FILE_TOO_LARGE);
                    }

                    for (OnSendMessageListener listener : sendMessageListeners) {
                        listener.onSendFail(msg, ErrorCode.FILE_TOO_LARGE);
                    }
                    return;
                }
            }
        }

        try {
            mClient.send(msg, new cn.wildfirechat.client.ISendMessageCallback.Stub() {
                @Override
                public void onSuccess(final long messageUid, final long timestamp) throws RemoteException {
                    android.util.Log.e("----send()-onSuccess->","----??????????????????-->"+msg.messageId+"  "+TimeUtils.getFormatTimeyyyyMMddHHmm(msg.serverTime));

                    msg.messageUid = messageUid;
                    msg.serverTime = timestamp;
                    msg.status = MessageStatus.Sent;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onSuccess(messageUid, timestamp);
                            }
                            for (OnSendMessageListener listener : sendMessageListeners) {
                                listener.onSendSuccess(msg);
                            }
                        }
                    });
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    msg.status = MessageStatus.Send_Failure;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            //add to retryjob if send fail
                            if(errorCode==-1||errorCode==-500||errorCode==-10090){
                                if(msg.messageId==0)return;
                                android.util.Log.e("----send()-onFailure->","---->??????????????????-??????????????????"+msg.messageId+" "+"  "+TimeUtils.getFormatTimeyyyyMMddHHmm(msg.serverTime));
                                jobManager.addJobInBackground(new RetrySendMessageJob(INST,msg,callback));
                                return;

                            }

                            if (callback != null) {
                                android.util.Log.e("----send()-onFailure->","---->??????????????????-callback!=null"+errorCode+"");
                                callback.onFail(errorCode);
                            }

                            for (OnSendMessageListener listener : sendMessageListeners) {
                                listener.onSendFail(msg , errorCode);
                            }
                        }
                    });
                }

                @Override
                public void onPrepared(final long messageId, final long savedTime) throws RemoteException {
                    android.util.Log.e("----send()-onPrepared->","---->??????????????????"+messageId+"  "+TimeUtils.getFormatTimeyyyyMMddHHmm(savedTime));
                    msg.messageId = messageId;
                    msg.serverTime = savedTime;
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onPrepare(messageId, savedTime);
                        }
                        for (OnSendMessageListener listener : sendMessageListeners) {
                            listener.onSendPrepare(msg, savedTime);
                        }
                    });
                }

                @Override
                public void onProgress(final long uploaded, final long total) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onProgress(uploaded, total));
                    }

                    mainHandler.post(() -> {
                        for (OnSendMessageListener listener : sendMessageListeners) {
                            listener.onProgress(msg, uploaded, total);
                        }
                    });
                }

                @Override
                public void onMediaUploaded(final String remoteUrl) throws RemoteException {
                    MediaMessageContent mediaMessageContent = (MediaMessageContent) msg.content;
                    mediaMessageContent.remoteUrl = remoteUrl;
                    if (msg.messageId == 0) {
                        return;
                    }
                    if (callback != null) {
                        mainHandler.post(() -> callback.onMediaUpload(remoteUrl));
                    }
                    mainHandler.post(() -> {
                        for (OnSendMessageListener listener : sendMessageListeners) {
                            listener.onMediaUpload(msg, remoteUrl);
                        }
                    });
                }
            }, expireDuration);
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null) {
                msg.status = MessageStatus.Send_Failure;
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
            }
            for (OnSendMessageListener listener : sendMessageListeners) {
                listener.onSendFail(msg, ErrorCode.SERVICE_EXCEPTION);
            }
        }
    }

    /**
     * ????????????
     *
     * @param msg      ??????????????????
     * @param callback ????????????
     */
    public void recallMessage(final Message msg, final GeneralCallback callback) {
        try {
            mClient.recall(msg.messageUid, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    msg.content = new RecallMessageContent(userId, msg.messageUid);
                    ((RecallMessageContent) msg.content).fromSelf = true;
                    callback.onSuccess();
                    for (OnRecallMessageListener listener : recallMessageListeners) {
                        listener.onRecallMessage(msg);
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    callback.onFail(errorCode);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (mClient != null) {
            gContext.unbindService(serviceConnection);
        }
    }

    /**
     * ????????????handler
     *
     * @return
     */
    public Handler getWorkHandler() {
        return workHandler;
    }


    /**
     * ???????????????handler
     *
     * @return
     */
    public Handler getMainHandler() {
        return mainHandler;
    }

    /**
     * ??????????????????
     *
     * @param conversationTypes ???????????????????????????
     * @param lines             ????????????????????????
     * @return
     */
    public List<ConversationInfo> getConversationList(List<Conversation.ConversationType> conversationTypes, List<Integer> lines) {
        if (!checkRemoteService()) {
            Log.e(TAG, "Remote service not available");
            return null;
        }

        if (conversationTypes == null || conversationTypes.size() == 0 ||
                lines == null || lines.size() == 0) {
            Log.e(TAG, "Invalid conversation type and lines");
            return null;
        }

        int[] intypes = new int[conversationTypes.size()];
        int[] inlines = new int[lines.size()];
        for (int i = 0; i < conversationTypes.size(); i++) {
            intypes[i] = conversationTypes.get(i).ordinal();
        }

        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            return mClient.getConversationList(intypes, inlines);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ??????????????????
     *
     * @param conversation
     * @return
     */
    public @Nullable
    ConversationInfo getConversation(Conversation conversation) {
        if (!checkRemoteService()) {
            Log.e(TAG, "Remote service not available");
            return null;
        }


        try {
            return mClient.getConversation(conversation.type.getValue(), conversation.target, conversation.line);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ??????????????????
     *
     * @param conversation
     * @param fromIndex    ????????????id(messageId)
     * @param before       true, ??????fromInde???????????????????????????????????????false?????????fromIndex???????????????????????????????????????????????????fromIndex???????????????
     * @param count        ??????????????????
     * @param withUser     ?????????????????????{@link cn.wildfirechat.model.Conversation.ConversationType#Channel}?????????, channel?????????????????????????????????????????????
     * @return
     */
    public List<Message> getMessages(Conversation conversation, long fromIndex, boolean before, int count, String withUser) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getMessages(conversation, fromIndex, before, count, withUser);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getRemoteMessages(Conversation conversation, long beforeMessageId, int count, GetRemoteMessageCallback callback) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.getRemoteMessages(conversation, beforeMessageId, count, new IGetRemoteMessageCallback.Stub() {
                @Override
                public void onSuccess(List<Message> messages) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> {
                            callback.onSuccess(messages);
                        });
                    }
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> {
                            callback.onFail(errorCode);
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????id???????????????
     *
     * @param messageId ??????id
     *                  <p>
     *                  ??????uid?????????uid?????????id?????????????????????????????????uid??????uid????????????????????????????????????id??????????????????
     *                  ???????????????????????????{@link cn.wildfirechat.message.core.PersistFlag}??????????????????????????????
     *                  {@link cn.wildfirechat.message.core.PersistFlag#Persist_And_Count}
     *                  ???{@link cn.wildfirechat.message.core.PersistFlag#Persist}?????????????????????id
     * @return
     */
    public Message getMessage(long messageId) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getMessage(messageId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????????uid???????????????
     *
     * @param messageUid ??????uid???
     *                   <p>
     *                   ??????uid?????????id?????????????????????????????????uid??????uid????????????????????????????????????id??????????????????
     *                   <p>
     *                   ???????????????????????????{@link cn.wildfirechat.message.core.PersistFlag}??????????????????????????????
     *                   {@link cn.wildfirechat.message.core.PersistFlag#Persist_And_Count}
     *                   ???{@link cn.wildfirechat.message.core.PersistFlag#Persist}?????????????????????id
     * @return
     */
    public Message getMessageByUid(long messageUid) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getMessageByUid(messageUid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @param conversation
     * @return
     */
    public UnreadCount getUnreadCount(Conversation conversation) {
        if (!checkRemoteService()) {
            return new UnreadCount();
        }

        try {
            return mClient.getUnreadCount(conversation.type.ordinal(), conversation.target, conversation.line);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new UnreadCount();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param conversationTypes
     * @param lines
     * @return
     */
    public UnreadCount getUnreadCountEx(List<Conversation.ConversationType> conversationTypes, List<Integer> lines) {
        if (!checkRemoteService()) {
            return new UnreadCount();
        }

        int[] intypes = new int[conversationTypes.size()];
        int[] inlines = new int[lines.size()];
        for (int i = 0; i < conversationTypes.size(); i++) {
            intypes[i] = conversationTypes.get(i).ordinal();
        }
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            return mClient.getUnreadCountEx(intypes, inlines);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new UnreadCount();
    }


    /**
     * ?????????????????????????????????
     *
     * @param conversation
     */
    public void clearUnreadStatus(Conversation conversation) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            UnreadCount unreadCount = getUnreadCount(conversation);
            if (unreadCount.unread == 0 && unreadCount.unreadMention == 0 && unreadCount.unreadMentionAll == 0) {
                return;
            }
            mClient.clearUnreadStatus(conversation.type.getValue(), conversation.target, conversation.line);
            ConversationInfo conversationInfo = getConversation(conversation);
            conversationInfo.unreadCount = new UnreadCount();
            for (OnConversationInfoUpdateListener listener : conversationInfoUpdateListeners) {
                listener.onConversationUnreadStatusClear(conversationInfo, unreadCount);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????audio??????media??????????????????????????????
     *
     * @param messageId
     */
    public void setMediaMessagePlayed(long messageId) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setMediaMessagePlayed(messageId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????????????????
     */
    public void clearAllUnreadStatus() {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.clearAllUnreadStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void clearMessages(Conversation conversation) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.clearMessages(conversation.type.getValue(), conversation.target, conversation.line);

            for (OnClearMessageListener listener : clearMessageListeners) {
                listener.onClearMessage(conversation);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     *
     * @param conversation
     * @param clearMsg     ??????????????????????????????????????????
     */
    public void removeConversation(Conversation conversation, boolean clearMsg) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.removeConversation(conversation.type.ordinal(), conversation.target, conversation.line, clearMsg);
            for (OnRemoveConversationListener listener : removeConversationListeners) {
                listener.onConversationRemove(conversation);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     *
     * @param conversation
     * @param top          true????????????false???????????????
     */
    public void setConversationTop(Conversation conversation, boolean top) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setConversationTop(conversation.type.ordinal(), conversation.target, conversation.line, top);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        ConversationInfo conversationInfo = getConversation(conversation);
        conversationInfo.isTop = top;
        for (OnConversationInfoUpdateListener listener : conversationInfoUpdateListeners) {
            listener.onConversationTopUpdate(conversationInfo, top);
        }
    }

    /**
     * ??????????????????
     *
     * @param conversation
     * @param draft
     */
    public void setConversationDraft(Conversation conversation, @Nullable String draft) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setConversationDraft(conversation.type.ordinal(), conversation.target, conversation.line, draft);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // ??????aidl??????setConversationDraft?????????oneway, ????????????????????????, ????????????setConversationDraft?????????????????????getConversation,
        // ????????????????????????????????????????????????????????????????????????
        // 1. ???oneway??????
        // 2. ????????????????????????
        // ???????????????????????????
        ConversationInfo conversationInfo = getConversation(conversation);
        if (conversationInfo == null) {
            return;
        }
        conversationInfo.draft = draft;
        for (OnConversationInfoUpdateListener listener : conversationInfoUpdateListeners) {
            listener.onConversationDraftUpdate(conversationInfo, draft);
        }
    }

    public void searchUser(String keyword, final SearchUserCallback callback) {
        if (userSource != null) {
            userSource.searchUser(keyword, callback);
            return;
        }
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.searchUser(keyword, new cn.wildfirechat.client.ISearchUserCallback.Stub() {
                @Override
                public void onSuccess(final List<UserInfo> userInfos) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(userInfos);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public boolean isMyFriend(String userId) {
        if (!checkRemoteService()) {
            return false;
        }

        try {
            return mClient.isMyFriend(userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getMyFriendList(boolean refresh) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getMyFriendList(refresh);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getGroupMemberDisplayName(String groupId, String memberId) {
        UserInfo userInfo = getUserInfo(memberId, groupId, false);
        if (userInfo == null) {
            return "<" + memberId + ">";
        }
        if (!TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (!TextUtils.isEmpty(userInfo.groupAlias)) {
            return userInfo.groupAlias;
        } else if (!TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        }
        return "<" + memberId + ">";
    }

    public String getUserDisplayName(UserInfo userInfo) {
        if (userInfo == null) {
            return "";
        }
        if (!TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (!TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        }
        return "<" + userInfo.uid + ">";
    }

    public String getUserDisplayName(String userId) {
        UserInfo userInfo = getUserInfo(userId, false);
        return getUserDisplayName(userInfo);
    }

    public String getFriendAlias(String userId) {
        if (!checkRemoteService()) {
            return null;
        }
        String alias;
        try {
            alias = mClient.getFriendAlias(userId);
            return alias;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setFriendAlias(String userId, String alias, GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null) {
                callback.onFail(ErrorCode.SERVICE_DIED);
            }
        }

        try {
            mClient.setFriendAlias(userId, alias, new IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(callback::onSuccess);
                    }

                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> {
                            callback.onFail(errorCode);
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<UserInfo> getMyFriendListInfo(boolean refresh) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getMyFriendListInfo(refresh);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadFriendRequestFromRemote() {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.loadFriendRequestFromRemote();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getFriendRequest(boolean incomming) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getFriendRequest(incomming);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearUnreadFriendRequestStatus() {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.clearUnreadFriendRequestStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadFriendRequestStatus() {
        if (!checkRemoteService()) {
            return 0;
        }

        try {
            return mClient.getUnreadFriendRequestStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void removeFriend(String userId, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.removeFriend(userId, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void sendFriendRequest(String userId, String reason, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.sendFriendRequest(userId, reason, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void handleFriendRequest(String userId, boolean accept, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.handleFriendRequest(userId, accept, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void setBlackList(String userId, boolean isBlacked, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.setBlackList(userId, isBlacked, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void deleteFriend(String userId, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.deleteFriend(userId, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }


    public @Nullable
    GroupInfo getGroupInfo(String groupId, boolean refresh) {
        if (!checkRemoteService()) {
            return new NullGroupInfo(groupId);
        }

        try {
            GroupInfo groupInfo = mClient.getGroupInfo(groupId, refresh);
            if (groupInfo == null) {
                groupInfo = new NullGroupInfo(groupId);
            }
            return groupInfo;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void joinChatRoom(String chatRoomId, GeneralCallback callback) {
        if (!checkRemoteService()) {
            callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }
        try {
            mClient.joinChatRoom(chatRoomId, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    mainHandler.post(() -> callback.onSuccess());
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    mainHandler.post(() -> callback.onFail(errorCode));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void quitChatRoom(String chatRoomId, GeneralCallback callback) {
        if (!checkRemoteService()) {
            callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }
        try {
            mClient.quitChatRoom(chatRoomId, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    mainHandler.post(() -> callback.onSuccess());
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    mainHandler.post(() -> callback.onFail(errorCode));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void getChatRoomInfo(String chatRoomId, long updateDt, GetChatRoomInfoCallback callback) {
        if (!checkRemoteService()) {
            callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.getChatRoomInfo(chatRoomId, updateDt, new cn.wildfirechat.client.IGetChatRoomInfoCallback.Stub() {
                @Override
                public void onSuccess(ChatRoomInfo chatRoomInfo) throws RemoteException {
                    mainHandler.post(() -> callback.onSuccess(chatRoomInfo));
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    mainHandler.post(() -> callback.onFail(errorCode));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void getChatRoomMembersInfo(String chatRoomId, int maxCount, GetChatRoomMembersInfoCallback callback) {
        if (!checkRemoteService()) {
            callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }
        try {
            mClient.getChatRoomMembersInfo(chatRoomId, maxCount, new cn.wildfirechat.client.IGetChatRoomMembersInfoCallback.Stub() {
                @Override
                public void onSuccess(ChatRoomMembersInfo chatRoomMembersInfo) throws RemoteException {
                    mainHandler.post(() -> callback.onSuccess(chatRoomMembersInfo));
                }

                @Override
                public void onFailure(int errorCode) throws RemoteException {
                    mainHandler.post(() -> callback.onFail(errorCode));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }


    public UserInfo getUserInfo(String userId, boolean refresh) {
        return getUserInfo(userId, null, refresh);
    }

    /**
     * ????????????????????????????????????????????????{@link UserInfo}???{@link NullUserInfo}
     *
     * @param userId
     * @param groupId
     * @param refresh
     * @return
     */
    public UserInfo getUserInfo(String userId, String groupId, boolean refresh) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        UserInfo userInfo;
        if (!refresh) {
            if (!TextUtils.isEmpty(groupId)) {
                userInfo = groupUserCache.get(groupMemberCacheKey(groupId, userId));
            } else {
                userInfo = userInfoCache.get(userId);
            }
            if (userInfo != null) {
                return userInfo;
            }
        }
        if (userSource != null) {
            userInfo = userSource.getUser(userId);
            if (userInfo == null) {
                userInfo = new NullUserInfo(userId);
            }
            return userInfo;
        }

        if (!checkRemoteService()) {
            return new NullUserInfo(userId);
        }

        try {
            userInfo = mClient.getUserInfo(userId, groupId, refresh);
            if (userInfo == null) {
                userInfo = new NullUserInfo(userId);
            } else {
                if (TextUtils.isEmpty(groupId)) {
                    userInfoCache.put(userId, userInfo);
                } else {
                    groupUserCache.put(groupMemberCacheKey(groupId, userId), userInfo);
                }
            }
            Log.e("--userInfo->", JsonUtil.toJson(userInfo));
            return userInfo;
        } catch (RemoteException e) {
            e.printStackTrace();
            return new NullUserInfo(userId);
        }
    }

    /**
     * ?????????list????????????????????????null
     *
     * @param userIds
     * @param groupId
     * @return
     */
    public List<UserInfo> getUserInfos(List<String> userIds, String groupId) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        if (userSource != null) {
            List<UserInfo> userInfos = new ArrayList<>();
            for (String userId : userIds) {
                userInfos.add(userSource.getUser(userId));
            }
            return userInfos;
        }

        if (!checkRemoteService()) {
            return null;
        }

        try {
            List<UserInfo> userInfos = mClient.getUserInfos(userIds, groupId);
            if (userInfos != null && userInfos.size() > 0) {
                for (UserInfo info : userInfos) {
                    if (info != null) {
                        if (TextUtils.isEmpty(groupId)) {
                            userInfoCache.put(info.uid, info);
                        } else {
                            groupUserCache.put(groupMemberCacheKey(groupId, info.uid), info);
                        }
                    }
                }
            }

            return userInfos;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void uploadMedia(byte[] data, int mediaType, final GeneralCallback2 callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        try {
            mClient.uploadMedia(data, mediaType, new IUploadMediaCallback.Stub() {
                @Override
                public void onSuccess(final String remoteUrl) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(remoteUrl);
                            }
                        });
                    }
                }

                @Override
                public void onProgress(final long uploaded, final long total) throws RemoteException {

                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    // ?????????????????????????????????????????????
    public void modifyMyInfo(List<ModifyMyInfoEntry> values, final GeneralCallback callback) {
        userInfoCache.remove(userId);
        if (userSource != null) {
            userSource.modifyMyInfo(values, callback);
            return;
        }
        if (!checkRemoteService()) {
            if (callback != null) {
                callback.onFail(ErrorCode.SERVICE_DIED);
            }
            return;
        }

        try {
            mClient.modifyMyInfo(values, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }

                    UserInfo userInfo = getUserInfo(userId, false);
                    onUserInfoUpdate(Collections.singletonList(userInfo));
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }

    }

    public boolean deleteMessage(Message message) {
        if (!checkRemoteService()) {
            return false;
        }

        try {
            mClient.deleteMessage(message.messageId);
            for (RemoveMessageListener listener : removeMessageListeners) {
                listener.onMessagedRemove(message);
            }
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }

    }



    public boolean deleteMessage1(long messageId) {
        if (!checkRemoteService()) {
            return false;
        }
        try {
            Message message = getMessage(messageId);
            mClient.deleteMessage(messageId);//??????????????????
            mainHandler.post(()->{
                for (RemoveMessageListener listener : removeMessageListeners) {
                    if (message != null) {
                        listener.onMessagedRemove(message);//??????ui
                    }
                }

            });

            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }

    }


    public List<ConversationSearchResult> searchConversation(String keyword, List<Conversation.ConversationType> conversationTypes, List<Integer> lines) {
        if (!checkRemoteService()) {
            return null;
        }

        int[] intypes = new int[conversationTypes.size()];
        int[] inlines = new int[lines.size()];
        for (int i = 0; i < conversationTypes.size(); i++) {
            intypes[i] = conversationTypes.get(i).ordinal();
        }
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            return mClient.searchConversation(keyword, intypes, inlines);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Message> searchMessage(Conversation conversation, String keyword) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.searchMessage(conversation, keyword);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<GroupSearchResult> searchGroups(String keyword) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.searchGroups(keyword);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserInfo> searchFriends(String keyword) {
        //1.frist query local
       List<UserInfo> localFriends= getMyFriendListInfo(false);
       List<UserInfo> out=new ArrayList<>();

       //2.filter keyword
       for (UserInfo friend:localFriends){
           if(friend.friendAlias.contains(keyword)){
               out.add(friend);
           }
       }

       if(out.size()>0){

           return out;
       }else{
           if (!checkRemoteService()) {
               return null;
           }

           try {
               return mClient.searchFriends(keyword);
           } catch (RemoteException e) {
               e.printStackTrace();
               return null;
           }
       }


    }

    public void createGroup(String groupId, String groupName, String groupPortrait, GroupInfo.GroupType groupType, List<String> memberIds, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback2 callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.createGroup(groupId, groupName, groupPortrait, groupType.value(), memberIds, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback2.Stub() {
                @Override
                public void onSuccess(final String result) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(result);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void addGroupMembers(String groupId, List<String> memberIds, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.addGroupMembers(groupId, memberIds, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    private MessagePayload content2Payload(MessageContent content) {
        if (content == null) {
            return null;
        }
        MessagePayload payload = content.encode();
        payload.contentType = content.getClass().getAnnotation(ContentTag.class).type();
        return payload;
    }

    public void removeGroupMembers(String groupId, List<String> memberIds, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.removeGroupMembers(groupId, memberIds, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    // FIXME: 2018/5/19 notifyMsg???????????????????????????????????????????????????????????????
    public void quitGroup(String groupId, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }
        try {
            mClient.quitGroup(groupId, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void dismissGroup(String groupId, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.dismissGroup(groupId, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void modifyGroupInfo(String groupId, ModifyGroupInfoType modifyType, String newValue, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }
        try {
            mClient.modifyGroupInfo(groupId, modifyType.ordinal(), newValue, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    // Attention
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    public void modifyGroupAlias(String groupId, String alias, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }
        try {
            mClient.modifyGroupAlias(groupId, alias, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                groupMemberCache.remove(groupMemberCacheKey(groupId, userId));
                                groupUserCache.remove(groupMemberCacheKey(groupId, userId));
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public List<GroupMember> getGroupMembers(String groupId, boolean forceUpdate) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getGroupMembers(groupId, forceUpdate);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String groupMemberCacheKey(String groupId, String memberId) {
        return memberId + "@" + groupId;
    }

    public GroupMember getGroupMember(String groupId, String memberId) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(memberId)) {
            return null;
        }
        String key = groupMemberCacheKey(groupId, memberId);
        GroupMember groupMember = groupMemberCache.get(key);
        if (groupMember != null) {
            return groupMember;
        }

        if (!checkRemoteService()) {
            return null;
        }

        try {
            groupMember = mClient.getGroupMember(groupId, memberId);
            groupMemberCache.put(key, groupMember);
            return groupMember;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void transferGroup(String groupId, String newOwner, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.transferGroup(groupId, newOwner, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
        }
    }

    public void setGroupManager(String groupId, boolean isSet, List<String> memberIds, List<Integer> lines, MessageContent notifyMsg, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            if (callback != null)
                callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }

        int[] inlines = new int[lines.size()];
        for (int j = 0; j < lines.size(); j++) {
            inlines[j] = lines.get(j);
        }

        try {
            mClient.setGroupManager(groupId, isSet, memberIds, inlines, content2Payload(notifyMsg), new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess());
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onFail(errorCode));
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFail(ErrorCode.SERVICE_EXCEPTION);
            }
        }
    }

    public String getUserSetting(int scope, String key) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return mClient.getUserSetting(scope, key);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getUserSettings(int scope) {
        if (!checkRemoteService()) {
            return null;
        }

        try {
            return (Map<String, String>) mClient.getUserSettings(scope);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getMyGroups(final GetGroupsCallback callback) {
        if (callback == null) {
            return;
        }
        if (!checkRemoteService()) {
            callback.onFail(ErrorCode.SERVICE_DIED);
            return;
        }
        workHandler.post(() -> {
            Map<String, String> groupIdMap = getUserSettings(UserSettingScope.FavoriteGroup);
            String fav = "1";
            new HashMap<String, String>(1);
            List<GroupInfo> groups = new ArrayList<>();
            if (groupIdMap != null && !groupIdMap.isEmpty()) {
                for (Map.Entry<String, String> entry : groupIdMap.entrySet()) {
                    if (entry.getValue().equals(fav)) {

                        GroupInfo info = getGroupInfo(entry.getKey(), false);
                        Logger.e("-----info---->"+JsonUtil.toJson(info));
                        if (!(info instanceof NullGroupInfo)) {
                            groups.add(getGroupInfo(entry.getKey(), false));
                        }

                    }
                }
            }
            mainHandler.post(() -> callback.onSuccess(groups));
        });
    }

    public void setUserSetting(int scope, String key, String value, final GeneralCallback callback) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setUserSetting(scope, key, value, new cn.wildfirechat.client.IGeneralCallback.Stub() {
                @Override
                public void onSuccess() throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final int errorCode) throws RemoteException {
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail(errorCode);
                            }
                        });
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setConversationSilent(Conversation conversation, boolean silent) {
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setConversationSilent(conversation.type.ordinal(), conversation.target, conversation.line, silent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ConversationInfo conversationInfo = getConversation(conversation);
        for (OnConversationInfoUpdateListener listener : conversationInfoUpdateListeners) {
            listener.onConversationSilentUpdate(conversationInfo, silent);
        }
    }

    /**
     * @return ??????????????? - ????????????
     */
    public long getServerDeltaTime() {
        if (!checkRemoteService()) {
            return 0L;
        }

        try {
            return mClient.getServerDeltaTime();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public void startLog() {
        startLog = true;
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.startLog();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
    }

    public void stopLog() {
        startLog = false;
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.stopLog();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
    }

    public void setDeviceToken(String token, PushType pushType) {
        deviceToken = token;
        this.pushType = pushType;
        if (!checkRemoteService()) {
            return;
        }

        try {
            mClient.setDeviceToken(token, pushType.value());
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
    }


    public boolean checkRemoteService() {
        if (INST != null) {
            if (mClient != null) {
                return true;
            }

            Intent intent = new Intent(gContext, ClientService.class);
            boolean result = gContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            if (!result) {
                Log.e(TAG, "Bind service failure");
            }
        } else {
            Log.e(TAG, "Chat manager not initialized");
        }

        return false;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mClient = IRemoteClient.Stub.asInterface(iBinder);
            try {
                mClient.setServerAddress(SERVER_HOST, SERVER_PORT);
                for (String msgName : msgList) {
                    mClient.registerMessageContent(msgName);
                }

                if (startLog) {
                    startLog();
                } else {
                    stopLog();
                }

                if (!TextUtils.isEmpty(deviceToken)) {
                    mClient.setDeviceToken(deviceToken, pushType.value());
                }

                mClient.setForeground(1);
                mClient.setOnReceiveMessageListener(new IOnReceiveMessageListener.Stub() {
                    @Override
                    public void onReceive(List<Message> messages, boolean hasMore) throws RemoteException {
                        onReceiveMessage(messages, hasMore);
                    }

                    @Override
                    public void onRecall(long messageUid) throws RemoteException {
                        onRecallMessage(messageUid);
                    }
                });
                mClient.setOnConnectionStatusChangeListener(new IOnConnectionStatusChangeListener.Stub() {
                    @Override
                    public void onConnectionStatusChange(int connectionStatus) throws RemoteException {
                        ChatManager.this.onConnectionStatusChange(connectionStatus);
                    }
                });

                mClient.setOnUserInfoUpdateListener(new IOnUserInfoUpdateListener.Stub() {
                    @Override
                    public void onUserInfoUpdated(List<UserInfo> userInfos) throws RemoteException {
                        ChatManager.this.onUserInfoUpdate(userInfos);
                    }
                });
                mClient.setOnGroupInfoUpdateListener(new IOnGroupInfoUpdateListener.Stub() {
                    @Override
                    public void onGroupInfoUpdated(List<GroupInfo> groupInfos) throws RemoteException {
                        ChatManager.this.onGroupInfoUpdated(groupInfos);
                    }
                });
                mClient.setOnGroupMembersUpdateListener(new IOnGroupMembersUpdateListener.Stub() {
                    @Override
                    public void onGroupMembersUpdated(String groupId, List<GroupMember> members) throws RemoteException {
                        ChatManager.this.onGroupMembersUpdate(groupId, members);
                    }
                });
                mClient.setOnFriendUpdateListener(new IOnFriendUpdateListener.Stub() {
                    @Override
                    public void onFriendListUpdated(List<String> friendList) throws RemoteException {
                        ChatManager.this.onFriendListUpdated(friendList);
                    }

                    @Override
                    public void onFriendRequestUpdated() throws RemoteException {

                        ChatManager.this.onFriendReqeustUpdated();
                    }
                });
                mClient.setOnSettingUpdateListener(new IOnSettingUpdateListener.Stub() {
                    @Override
                    public void onSettingUpdated() throws RemoteException {
                        ChatManager.this.onSettingUpdated();
                    }
                });
                mClient.setOnChannelInfoUpdateListener(new IOnChannelInfoUpdateListener.Stub() {
                    @Override
                    public void onChannelInfoUpdated(List<ChannelInfo> channelInfos) throws RemoteException {
                        ChatManager.this.onChannelInfoUpdate(channelInfos);
                    }
                });

                if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(token)) {
                    mClient.connect(userId, token);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            mainHandler.post(() -> {
                for (IMServiceStatusListener listener : imServiceStatusListeners) {
                    listener.onServiceConnected();
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("chatManager", "onServiceDisconnected");
            mClient = null;
            checkRemoteService();
            mainHandler.post(() -> {
                for (IMServiceStatusListener listener : imServiceStatusListeners) {
                    listener.onServiceConnected();
                }
            });
        }
    };



    private static void configureJobManager() {
        //3. JobManager?????????????????????Builder??????
        com.birbit.android.jobqueue.config.Configuration configuration = new Configuration.Builder(gContext)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "------JobManager---->";
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        android.util.Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        android.util.Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        android.util.Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String s, Object... objects) {

                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .networkUtil(new NetworkUtilImpl(gContext))
                .build();


         jobManager= new JobManager(configuration);
    }

    public JobManager getJobManager(){

        return  this.jobManager;

    }






}

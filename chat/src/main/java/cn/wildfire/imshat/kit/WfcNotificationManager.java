package cn.wildfire.imshat.kit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.orhanobut.logger.Logger;

import org.simple.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.wildfire.imshat.app.MyApp;
import cn.wildfire.imshat.app.main.MainActivity;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;
import me.leolin.shortcutbadger.ShortcutBadger;

import static androidx.core.app.NotificationCompat.CATEGORY_MESSAGE;
import static androidx.core.app.NotificationCompat.DEFAULT_ALL;
import static cn.wildfirechat.message.core.PersistFlag.Persist_And_Count;
import static cn.wildfirechat.model.Conversation.ConversationType.Single;

public class WfcNotificationManager {
    private Context mContext;
    private WfcNotificationManager(Context context) {

        this.mContext=context;
    }

    private static WfcNotificationManager notificationManager;

    public synchronized static WfcNotificationManager getInstance() {
        if (notificationManager == null) {
            notificationManager = new WfcNotificationManager(MyApp.getContext());
        }
        return notificationManager;
    }

    public void clearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationConversations.clear();
    }

    private void showNotification(Context context, String tag, int id, String title, String content, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "chat_notification";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "chat message",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.enableLights(true); 
            channel.setLightColor(Color.RED); 
            channel.setShowBadge(true); 
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher3)
                .setAutoCancel(true)
                .setCategory(CATEGORY_MESSAGE)
                .setDefaults(DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(content);
        notificationManager.notify(tag, id, builder.build());


    }

    public void handleRecallMessage(Context context, Message message) {
        handleReceiveMessage(context, Collections.singletonList(message));
    }

    public void handleReceiveMessage(Context context, List<Message> messages) {

        if (messages == null || messages.isEmpty()) {
            return;
        }

        for (Message message : messages) {
            if (message.direction == MessageDirection.Send || (message.content.getPersistFlag() != Persist_And_Count && !(message.content instanceof RecallMessageContent))) {
                continue;
            }
            String pushContent = message.content.encode().pushContent;
            if (TextUtils.isEmpty(pushContent)) {
                if (message.content.getType() == MessageContentType.ContentType_Text) {
                    TextMessageContent textMessageContent = (TextMessageContent) message.content;
                    pushContent = textMessageContent.getContent();
                } else if (message.content.getType() == MessageContentType.ContentType_Image) {
                    pushContent =mContext.getString(R.string.str_img_tip);
                } else if (message.content.getType() == MessageContentType.ContentType_Voice) {
                    pushContent = mContext.getString(R.string.str_audio_tip);
                } else if (message.content.getType() == MessageContentType.ContentType_Recall) {
                    pushContent = mContext.getString(R.string.str_reset_msg);
                }
            }

            int unreadCount = ChatManager.Instance().getUnreadCount(message.conversation).unread;
            if (unreadCount > 1) {
                pushContent = "[" + unreadCount + mContext.getString(R.string.str_tiao) + pushContent;

            }

            
            String title = "";
            if (message.conversation.type == Single) {
                String name = ChatManager.Instance().getUserDisplayName(message.conversation.target);
                title = TextUtils.isEmpty(name) ? mContext.getString(R.string.new_message) : name;
            } else if (message.conversation.type == Conversation.ConversationType.Group) {
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.conversation.target, false);
                title = groupInfo == null ? mContext.getString(R.string.group_cheat) : groupInfo.name;
            } else {
                title = mContext.getString(R.string.new_message);
            }
            Intent mainIntent = new Intent(context, MainActivity.class);
            Intent conversationIntent = new Intent(context, ConversationActivity.class);
            conversationIntent.putExtra("conversation", message.conversation);
            PendingIntent pendingIntent = PendingIntent.getActivities(context, notificationId(message.conversation), new Intent[]{mainIntent, conversationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
            String tag = "wfc notification tag";
            showNotification(context, tag, notificationId(message.conversation), title, pushContent, pendingIntent);

        }
    }




    private List<Conversation> notificationConversations = new ArrayList<>();

    private int notificationId(Conversation conversation) {
        if (!notificationConversations.contains(conversation)) {
            notificationConversations.add(conversation);
        }
        return notificationConversations.indexOf(conversation);
    }
}

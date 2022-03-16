package cn.wildfirechat.message.notification;

import android.os.Parcel;

import com.android.base.BaseApp;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.client.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_QUIT_GROUP;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_QUIT_GROUP, flag = PersistFlag.Persist)
public class QuitGroupNotificationContent extends GroupNotificationMessageContent {
    public String operator;

    public QuitGroupNotificationContent() {
    }

    @Override
    public String formatNotification(Message message) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(BaseApp.getContext().getString(R.string.str_group_quit));
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operator));
            sb.append(BaseApp.getContext().getString(R.string.str_group_exit));
        }

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = new MessagePayload();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operator);
            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.content != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                operator = jsonObject.optString("o");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.operator);
        dest.writeString(this.groupId);
        dest.writeByte(this.fromSelf ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mentionedType);
        dest.writeStringList(this.mentionedTargets);
    }

    protected QuitGroupNotificationContent(Parcel in) {
        this.operator = in.readString();
        this.groupId = in.readString();
        this.fromSelf = in.readByte() != 0;
        this.mentionedType = in.readInt();
        this.mentionedTargets = in.createStringArrayList();
    }

    public static final Creator<QuitGroupNotificationContent> CREATOR = new Creator<QuitGroupNotificationContent>() {
        @Override
        public QuitGroupNotificationContent createFromParcel(Parcel source) {
            return new QuitGroupNotificationContent(source);
        }

        @Override
        public QuitGroupNotificationContent[] newArray(int size) {
            return new QuitGroupNotificationContent[size];
        }
    };
}

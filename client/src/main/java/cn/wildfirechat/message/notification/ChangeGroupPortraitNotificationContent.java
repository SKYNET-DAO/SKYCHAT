package cn.wildfirechat.message.notification;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.client.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_CHANGE_GROUP_PORTRAIT;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_CHANGE_GROUP_PORTRAIT, flag = PersistFlag.Persist)
public class ChangeGroupPortraitNotificationContent extends GroupNotificationMessageContent {
    public String operateUser;

    public ChangeGroupPortraitNotificationContent() {
    }

    @Override
    public String formatNotification(Message message) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(R.string.str_nin);
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operateUser));
        }
        sb.append(R.string.str_update_avatar);

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = new MessagePayload();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operateUser);
            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                operateUser = jsonObject.optString("o");
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
        dest.writeString(this.operateUser);
        dest.writeString(this.groupId);
        dest.writeByte(this.fromSelf ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mentionedType);
        dest.writeStringList(this.mentionedTargets);
    }

    protected ChangeGroupPortraitNotificationContent(Parcel in) {
        this.operateUser = in.readString();
        this.groupId = in.readString();
        this.fromSelf = in.readByte() != 0;
        this.mentionedType = in.readInt();
        this.mentionedTargets = in.createStringArrayList();
    }

    public static final Creator<ChangeGroupPortraitNotificationContent> CREATOR = new Creator<ChangeGroupPortraitNotificationContent>() {
        @Override
        public ChangeGroupPortraitNotificationContent createFromParcel(Parcel source) {
            return new ChangeGroupPortraitNotificationContent(source);
        }

        @Override
        public ChangeGroupPortraitNotificationContent[] newArray(int size) {
            return new ChangeGroupPortraitNotificationContent[size];
        }
    };
}

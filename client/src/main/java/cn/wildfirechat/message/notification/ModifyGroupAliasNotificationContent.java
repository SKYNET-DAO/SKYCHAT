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

import static cn.wildfirechat.message.core.MessageContentType.ContentType_MODIFY_GROUP_ALIAS;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_MODIFY_GROUP_ALIAS, flag = PersistFlag.Persist)
public class ModifyGroupAliasNotificationContent extends GroupNotificationMessageContent {
    public String operateUser;
    public String alias;

    public ModifyGroupAliasNotificationContent() {
    }

    @Override
    public String formatNotification(Message message) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(BaseApp.getContext().getString(com.android.base.R.string.str_nin));
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operateUser));
        }
        sb.append(BaseApp.getContext().getString(R.string.str_modify_group_card));
        sb.append(alias);

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = new MessagePayload();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operateUser);
            objWrite.put("n", alias);

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
                alias = jsonObject.optString("n");
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
        dest.writeString(this.alias);
        dest.writeString(this.groupId);
        dest.writeByte(this.fromSelf ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mentionedType);
        dest.writeStringList(this.mentionedTargets);
    }

    protected ModifyGroupAliasNotificationContent(Parcel in) {
        this.operateUser = in.readString();
        this.alias = in.readString();
        this.groupId = in.readString();
        this.fromSelf = in.readByte() != 0;
        this.mentionedType = in.readInt();
        this.mentionedTargets = in.createStringArrayList();
    }

    public static final Creator<ModifyGroupAliasNotificationContent> CREATOR = new Creator<ModifyGroupAliasNotificationContent>() {
        @Override
        public ModifyGroupAliasNotificationContent createFromParcel(Parcel source) {
            return new ModifyGroupAliasNotificationContent(source);
        }

        @Override
        public ModifyGroupAliasNotificationContent[] newArray(int size) {
            return new ModifyGroupAliasNotificationContent[size];
        }
    };
}

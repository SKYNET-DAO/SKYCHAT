package cn.wildfire.imshat.kit.conversationlist.viewholder;

import android.view.View;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.annotation.ConversationInfoType;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;

@ConversationInfoType(type = Conversation.ConversationType.Group, line = 0)
@EnableContextMenu
public class GroupConversationViewHolder extends ConversationViewHolder {

    public GroupConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        GroupInfo groupInfo = ChatManagerHolder.gChatManager.getGroupInfo(conversationInfo.conversation.target, false);
        String name;
        String portrait;
        if (groupInfo != null) {
            name = groupInfo.name;
            portrait = groupInfo.portrait;
        } else {
            name = "Group<" + conversationInfo.conversation.target + ">";
            portrait = null;
        }
        GlideApp
                .with(fragment)
                .load(portrait)
                .placeholder(R.mipmap.icon_add_group)
                .transforms(new CenterCrop(), new RoundedCorners(10))
                .into(portraitImageView);
        nameTextView.setText(name);
    }

}

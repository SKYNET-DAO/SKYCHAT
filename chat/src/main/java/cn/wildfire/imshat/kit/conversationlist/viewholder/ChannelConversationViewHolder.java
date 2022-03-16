package cn.wildfire.imshat.kit.conversationlist.viewholder;

import android.view.View;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.annotation.ConversationContextMenuItem;
import cn.wildfire.imshat.kit.annotation.ConversationInfoType;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.remote.ChatManager;

@ConversationInfoType(type = Conversation.ConversationType.Channel, line = 0)
@EnableContextMenu
public class ChannelConversationViewHolder extends ConversationViewHolder {

    public ChannelConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        ChannelInfo channelInfo = ChatManager.Instance().getChannelInfo(conversationInfo.conversation.target, false);
        String name;
        String portrait;
        if (channelInfo != null) {
            name = channelInfo.name;
            portrait = channelInfo.portrait;
        } else {
            name = "Channel<" + conversationInfo.conversation.target + ">";
            portrait = null;
        }
        nameTextView.setText(name);
        GlideApp
                .with(fragment)
                .load(portrait)
                .placeholder(R.mipmap.ic_channel)
                .transforms(new CenterCrop(), new RoundedCorners(10))
                .into(portraitImageView);
    }

    @ConversationContextMenuItem(tag = ConversationContextMenuItemTags.TAG_UNSUBSCRIBE,
            title = "",
            titleResId = R.string.str_cancel_receiver_channel,
            confirm = true,
            confirmPrompt = "",
            confirmPromptResId = R.string.str_cancel_channel_conforim,
            priority = 0)
    public void unSubscribeChannel(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.unSubscribeChannel(conversationInfo);
    }
}

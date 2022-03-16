package cn.wildfire.imshat.kit.conversationlist.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.annotation.ConversationInfoType;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;

@ConversationInfoType(type = Conversation.ConversationType.Single, line = 0)
@EnableContextMenu
public class SingleConversationViewHolder extends ConversationViewHolder {
    public SingleConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversationInfo.conversation.target, false);
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        String name = userViewModel.getUserDisplayName(userInfo);
        String portrait;
        portrait = userInfo.portrait;
        GlideApp
                .with(fragment)
                .load(portrait)
                .placeholder(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(10))
                .into(portraitImageView);
        nameTextView.setText(name);
    }

}

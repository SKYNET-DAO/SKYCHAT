package cn.wildfire.imshat.kit.conversationlist.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.lqr.emoji.MoonUtils;
import com.orhanobut.logger.Logger;

import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.annotation.ConversationContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfire.imshat.kit.conversation.Draft;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.group.GroupViewModel;
import cn.wildfire.imshat.kit.third.utils.TimeUtils;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.LanguageStrConvert;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;

@SuppressWarnings("unused")
public abstract class ConversationViewHolder extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected View itemView;
    protected ConversationInfo conversationInfo;
    protected int position;
    protected RecyclerView.Adapter adapter;
    protected ConversationListViewModel conversationListViewModel;

    @Bind(R.id.nameTextView)
    protected TextView nameTextView;
    @Bind(R.id.timeTextView)
    protected TextView timeTextView;
    @Bind(R.id.portraitImageView)
    protected ImageView portraitImageView;
    @Bind(R.id.slient)
    protected ImageView silentImageView;
    @Bind(R.id.unreadCountTextView)
    protected TextView unreadCountTextView;
    @Bind(R.id.contentTextView)
    protected TextView contentTextView;
    @Bind(R.id.promptTextView)
    protected TextView promptTextView;

    @Bind(R.id.statusImageView)
    protected ImageView statusImageView;

    public ConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.itemView = itemView;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
        conversationListViewModel = ViewModelProviders
                .of(fragment.getActivity(), new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
    }

    final public void onBind(ConversationInfo conversationInfo, int position) {
        this.conversationInfo = conversationInfo;
        this.position = position;
        onBind(conversationInfo);
    }


    protected abstract void onBindConversationInfo(ConversationInfo conversationInfo);

    public void onBind(ConversationInfo conversationInfo) {

        LanguageStrConvert.convertLang(conversationInfo);

        onBindConversationInfo(conversationInfo);

//
//        if (conversationInfo.lastMessage!=null&&conversationInfo.lastMessage.direction == MessageDirection.Receive) {//modify by yzr,fix the time has problem when receiver msg
//
//           long result= TimeUtils.calculateTime(conversationInfo.lastMessage.serverTime);
//            timeTextView.setText(TimeUtils.getMsgFormatTime(result));


//        } else {
  //      Logger.e("-------ConversationViewHolder-onBind-------->", cn.wildfire.imshat.wallet.utils.TimeUtils.getFormatTimeyyyyMMddHHmm(conversationInfo.lastMessage.serverTime));
        if(conversationInfo.lastMessage!=null){
            timeTextView.setText(TimeUtils.getMsgFormatTime(TimeUtils.calculateTime(conversationInfo.lastMessage.serverTime)));

        }
//        }
        silentImageView.setVisibility(conversationInfo.isSilent ? View.VISIBLE : View.GONE);
        statusImageView.setVisibility(View.GONE);

        itemView.setBackgroundResource(conversationInfo.isTop ? R.drawable.selector_stick_top_item : R.drawable.selector_common_item);
        if (conversationInfo.unreadCount.unread > 0) {
            unreadCountTextView.setVisibility(View.VISIBLE);
            unreadCountTextView.setText(conversationInfo.unreadCount.unread > 99 ? "99+" : conversationInfo.unreadCount.unread + "");
        } else {
            unreadCountTextView.setVisibility(View.GONE);
        }

        Draft draft = Draft.fromDraftJson(conversationInfo.draft);
        if (draft != null && !TextUtils.isEmpty(draft.getContent())) {
            MoonUtils.identifyFaceExpression(fragment.getActivity(), contentTextView, draft.getContent(), ImageSpan.ALIGN_BOTTOM);
            setViewVisibility(R.id.promptTextView, View.VISIBLE);
            setViewVisibility(R.id.contentTextView, View.VISIBLE);
        } else {
            if (conversationInfo.unreadCount.unreadMentionAll > 0 || conversationInfo.unreadCount.unreadMention > 0) {
                promptTextView.setText(UIUtils.getString(R.string.str_at_me));
            } else {
                setViewVisibility(R.id.promptTextView, View.GONE);
            }
            setViewVisibility(R.id.contentTextView, View.VISIBLE);
            if (conversationInfo.lastMessage != null && conversationInfo.lastMessage.content != null) {
                String content = "";
                Message lastMessage = conversationInfo.lastMessage;
                // the message maybe invalid
                try {
                    if (conversationInfo.conversation.type == Conversation.ConversationType.Group && lastMessage.direction == MessageDirection.Receive) {
                        GroupViewModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
                        String senderDisplayName = groupViewModel.getGroupMemberDisplayName(conversationInfo.conversation.target, conversationInfo.lastMessage.sender);
                        content = senderDisplayName + ":" + lastMessage.digest();
                    } else {
                        content = lastMessage.digest();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MoonUtils.identifyFaceExpression(fragment.getActivity(), contentTextView, content, ImageSpan.ALIGN_BOTTOM);

                switch (lastMessage.status) {
                    case Sending:
                        statusImageView.setVisibility(View.VISIBLE);
                        // TODO update sending image resource
                        statusImageView.setImageResource(R.mipmap.ic_sending);
                        break;
                    case Send_Failure:
                        statusImageView.setVisibility(View.VISIBLE);
                        statusImageView.setImageResource(R.mipmap.img_error);
                        break;
                    default:
                        statusImageView.setVisibility(View.GONE);
                        break;
                }

            } else {
                contentTextView.setText("");
            }
        }
    }

    public void onClick(View itemView) {
        conversationListViewModel.clearConversationUnreadStatus(conversationInfo);
        Intent intent = new Intent(fragment.getActivity(), ConversationActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        fragment.startActivity(intent);
    }

    @ConversationContextMenuItem(tag = ConversationContextMenuItemTags.TAG_REMOVE,
            title ="",
            titleResId = R.string.str_del_conversation,
            confirm = true,
            confirmPrompt ="",
            confirmPromptResId = R.string.str_confirm_del,
            priority = 0)
    public void removeConversation(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.removeConversation(conversationInfo);

    }

    @ConversationContextMenuItem(tag = ConversationContextMenuItemTags.TAG_REMOVE,
            titleResId = R.string.str_clear_conversation,
            title ="",
            confirm = true,
            confirmPromptResId = R.string.str_clear_conversation_ok,
            confirmPrompt ="",
            priority = 0)
    public void clearMessages(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.clearMessages(conversationInfo.conversation);
    }

    @ConversationContextMenuItem(tag = ConversationContextMenuItemTags.TAG_TOP,titleResId = R.string.str_top,title = "", priority = 1)
    public void stickConversationTop(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.setConversationTop(conversationInfo, true);
    }

    @ConversationContextMenuItem(tag = ConversationContextMenuItemTags.TAG_CANCEL_TOP,titleResId = R.string.str_top_cannel,title = "", priority = 2)
    public void cancelStickConversationTop(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.setConversationTop(conversationInfo, false);
    }


    public boolean contextMenuItemFilter(ConversationInfo conversationInfo, String itemTag) {
        if (ConversationContextMenuItemTags.TAG_TOP.equals(itemTag)) {
            return conversationInfo.isTop;
        }

        if (ConversationContextMenuItemTags.TAG_CANCEL_TOP.equals(itemTag)) {
            return !conversationInfo.isTop;
        }
        return false;
    }

    protected <T extends View> T getView(int viewId) {
        View view;
        view = itemView.findViewById(viewId);
        return (T) view;
    }

    protected ConversationViewHolder setViewVisibility(int viewId, int visibility) {
        View view = itemView.findViewById(viewId);
        view.setVisibility(visibility);
        return this;
    }

}

package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.conversation.ConversationMessageAdapter;
import cn.wildfire.imshat.kit.conversation.ConversationViewModel;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.TimeUtils;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.Message;

public abstract class MessageContentViewHolder extends RecyclerView.ViewHolder {
    public FragmentActivity context;
    protected View itemView;
    protected UiMessage message;
    protected int position;
    protected RecyclerView.Adapter adapter;
    protected ConversationViewModel conversationViewModel;

    @Bind(R.id.timeTextView)
    TextView timeTextView;


    public MessageContentViewHolder(FragmentActivity activity, RecyclerView.Adapter adapter, View itemView) {
        super(itemView);
        this.context = activity;
        this.itemView = itemView;
        this.adapter = adapter;
        conversationViewModel = ViewModelProviders.of(context).get(ConversationViewModel.class);
        ButterKnife.bind(this, itemView);
    }

    public void onBind(UiMessage message, int position) {
        setMessageTime(message.message, position);
    }

    /**
     * @param uiMessage
     * @param tag
     * @return 
     */

    public abstract boolean contextMenuItemFilter(UiMessage uiMessage, String tag);

    public void onViewRecycled() {
        // you can do some clean up here
    }

    protected void setMessageTime(Message item, int position) {
        Logger.e("-------MessageContentViewHolder.setMessageTime---->", cn.wildfire.imshat.wallet.utils.TimeUtils.getFormatTimeyyyyMMddHHmm(item.serverTime));

        Logger.e("------MessageContentViewHolder.setMessageTime----->"+ JsonUtil.toJson(item));
        long msgTime = item.serverTime;
        if (position > 0) {
            Message preMsg = ((ConversationMessageAdapter) adapter).getItem(position - 1).message;
            long preMsgTime = preMsg.serverTime;
            if (msgTime - preMsgTime > (5 * 60 * 1000)) {
                timeTextView.setVisibility(View.VISIBLE);

                timeTextView.setText(TimeUtils.getMsgFormatTime(TimeUtils.calculateTime(msgTime)));
            } else {
                timeTextView.setVisibility(View.GONE);
            }
        } else {
            timeTextView.setVisibility(View.VISIBLE);
            timeTextView.setText(TimeUtils.getMsgFormatTime(TimeUtils.calculateTime(msgTime)));
        }
    }


}

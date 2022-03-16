package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.base.BaseApp;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.annotation.ReceiveLayoutRes;
import cn.wildfire.imshat.kit.annotation.SendLayoutRes;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.CallStartMessageContent;

@MessageContentType(CallStartMessageContent.class)
@ReceiveLayoutRes(resId = R.layout.conversation_item_voip_receive)
@SendLayoutRes(resId = R.layout.conversation_item_voip_send)
@EnableContextMenu
public class VoipMessageViewHolder extends NormalMessageContentViewHolder {
    @Bind(R.id.contentTextView)
    TextView textView;

    public VoipMessageViewHolder(FragmentActivity activity, RecyclerView.Adapter adapter, View itemView) {
        super(activity, adapter, itemView);
        ButterKnife.bind(this, itemView);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBind(UiMessage message) {
        CallStartMessageContent content = (CallStartMessageContent) message.message.content;
        if (content.getStatus() == 0) {
            textView.setText(BaseApp.getContext().getString(R.string.str_target_noreceiver));
        } else if (content.getStatus() == 1) {
            textView.setText(BaseApp.getContext().getString(R.string.str_celling));
        } else {
            String text;
            if (content.getConnectTime() > 0) {
                long duration = (content.getEndTime() - content.getConnectTime()) / 1000;
                if (duration > 3600) {
                    text = String.format(BaseApp.getContext().getString(R.string.str_cell_leng), duration / 3600, (duration % 3600) / 60, (duration % 60));

                } else {
                    text = String.format(BaseApp.getContext().getString(R.string.str_cell_leng), duration / 60, (duration % 60));
                }
            } else {
                text = BaseApp.getContext().getString(R.string.str_target_noreceiver);
            }
            textView.setText(text);
        }
    }

    @OnClick(R.id.contentTextView)
    public void call(View view) {
        if (((CallStartMessageContent) message.message.content).getStatus() == 1) {
            return;
        }
        WfcUIKit.onCall(context, message.message.conversation.target, true, false);
    }
}

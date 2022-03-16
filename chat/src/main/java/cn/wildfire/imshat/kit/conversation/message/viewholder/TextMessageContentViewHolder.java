package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lqr.emoji.MoonUtils;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.annotation.MessageContextMenuItem;
import cn.wildfire.imshat.kit.annotation.ReceiveLayoutRes;
import cn.wildfire.imshat.kit.annotation.SendLayoutRes;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.widget.AutoLinkTextView;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TextMessageContent;

@MessageContentType(TextMessageContent.class)
@SendLayoutRes(resId = R.layout.conversation_item_text_send)
@ReceiveLayoutRes(resId = R.layout.conversation_item_text_receive)
@EnableContextMenu
public class TextMessageContentViewHolder extends NormalMessageContentViewHolder {
    @Bind(R.id.contentTextView)
    TextView contentTextView;

    public TextMessageContentViewHolder(FragmentActivity activity, RecyclerView.Adapter adapter, View itemView) {
        super(activity, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        MoonUtils.identifyFaceExpression(context, contentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
    }

    @OnClick(R.id.contentTextView)
    public void onClickTest(View view) {
//        Toast.makeText(context, "onTextMessage click: " + ((TextMessageContent) message.message.content).getContent(), Toast.LENGTH_SHORT).show();
    }


    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_CLIP,titleResId = R.string.str_copy,title = "", confirm = false, priority = 12)
    public void clip(View itemView, UiMessage message) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        TextMessageContent content = (TextMessageContent) message.message.content;
        ClipData clipData = ClipData.newPlainText("messageContent", content.getContent());
        clipboardManager.setPrimaryClip(clipData);
    }
}

package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import butterknife.Bind;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.annotation.ReceiveLayoutRes;
import cn.wildfire.imshat.kit.annotation.SendLayoutRes;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.StickerMessageContent;

@MessageContentType(StickerMessageContent.class)
@SendLayoutRes(resId = R.layout.conversation_item_sticker_send)
@ReceiveLayoutRes(resId = R.layout.conversation_item_sticker_receive)
@EnableContextMenu
public class StickerMessageContentViewHolder extends NormalMessageContentViewHolder {
    private String path;
    @Bind(R.id.stickerImageView)
    ImageView imageView;

    public StickerMessageContentViewHolder(FragmentActivity context, RecyclerView.Adapter adapter, View itemView) {
        super(context, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        StickerMessageContent stickerMessage = (StickerMessageContent) message.message.content;
        imageView.getLayoutParams().width = UIUtils.dip2Px(stickerMessage.width > 150 ? 150 : stickerMessage.width);
        imageView.getLayoutParams().height = UIUtils.dip2Px(stickerMessage.height > 150 ? 150 : stickerMessage.height);

        if (!TextUtils.isEmpty(stickerMessage.localPath)) {
            if (stickerMessage.localPath.equals(path)) {
                return;
            }
            GlideApp.with(context).load(stickerMessage.localPath)
                    .into(imageView);
            path = stickerMessage.localPath;
        } else {
            CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);
            progressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
            progressDrawable.start();
            GlideApp.with(context)
                    .load(stickerMessage.remoteUrl)
                    .placeholder(progressDrawable)
                    .into(imageView);
        }
    }

    
    public void onClick(View view) {
        // TODO
    }

}

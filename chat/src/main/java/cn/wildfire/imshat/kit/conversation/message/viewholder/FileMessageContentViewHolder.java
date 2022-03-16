package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.annotation.LayoutRes;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.FileSizeUtil;
import cn.wildfire.imshat.kit.utils.Path.FileUtils;
import cn.wildfire.imshat.kit.widget.BubbleImageView;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.core.MessageStatus;

@MessageContentType(FileMessageContent.class)
@LayoutRes(resId = R.layout.conversation_item_file_send)
@EnableContextMenu
public class FileMessageContentViewHolder extends MediaMessageContentViewHolder {

    @Bind(R.id.imageView)
    BubbleImageView imageView;
    @Bind(R.id.filename)
    TextView filename;
    @Bind(R.id.filesize)
    TextView filesize;




    private FileMessageContent fileMessageContent;

    public FileMessageContentViewHolder(FragmentActivity context, RecyclerView.Adapter adapter, View itemView) {
        super(context, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        super.onBind(message);
        fileMessageContent = (FileMessageContent) message.message.content;
        if (message.message.status == MessageStatus.Sending || message.isDownloading) {
            imageView.setPercent(message.progress);
            imageView.setProgressVisible(true);
            imageView.showShadow(false);
        } else {
            imageView.setProgressVisible(false);
            imageView.showShadow(false);
        }
        Glide.with(context).load(R.drawable.ic_file)
             .into(imageView);

        filename.setText(fileMessageContent.getName());

        filesize.setText(FileSizeUtil.FormetFileSize(fileMessageContent.getSize()));

    }

    @OnClick(R.id.root)
    public void onClick(View view) {
        if (message.isDownloading) {
            return;
        }
        File file = conversationViewModel.mediaMessageContentFile(message);
        if (file == null) {
            return;
        }

        if (file.exists()) {
            Intent intent = FileUtils.getViewIntent(context, file);
            ComponentName cn = intent.resolveActivity(context.getPackageManager());
            if (cn == null) {
                Toast.makeText(context, UIUtils.getString(R.string.str_open_not_file), Toast.LENGTH_SHORT).show();
                return;
            }
            context.startActivity(intent);
        } else {
            conversationViewModel.downloadMedia(message, file);
        }
    }
}

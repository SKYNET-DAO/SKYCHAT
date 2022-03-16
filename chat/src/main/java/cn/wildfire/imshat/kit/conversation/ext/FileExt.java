package cn.wildfire.imshat.kit.conversation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.File;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.imshat.kit.third.utils.ImageUtils;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.Path.FileUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

public class FileExt extends ConversationExt {

    /**
     * @param containerView 
     * @param conversation
     */
    @ExtContextMenuItem(titleResId = R.string.file,title = "")
    public void pickFile(View containerView, Conversation conversation) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_FILE);
        conversationViewModel.sendMessage(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtils.getPath(context, uri);
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(context, UIUtils.getString(R.string.str_file_select_fail), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String type = path.substring(path.lastIndexOf(".")).toLowerCase();
                File file = new File(path);
                switch (type) {
                    case ".png":
                    case ".jpg":
                    case ".jpeg":
                        File imageFileThumb = ImageUtils.genThumbImgFile(path);
                        Logger.e("-------imageFileThumb----->"+imageFileThumb.getPath());
                        conversationViewModel.sendImgMsg(imageFileThumb, file);
                        break;
                    case ".3gp":
                    case ".mpg":
                    case ".mpeg":
                    case ".mpe":
                    case ".mp4":
                    case ".avi":
                    case ".mov":
                        conversationViewModel.sendVideoMsg(file);
                        break;
                    case ".gif":
                        conversationViewModel.sendFileMsg(file);
                        break;
                    default:
                        conversationViewModel.sendFileMsg(file);
                        break;
                }
            }catch (Exception e){//no prefix
                conversationViewModel.sendFileMsg(new File(path));
            }


        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_file;
    }

    @Override
    public String title(Context context) {
        return UIUtils.getString(R.string.file);
    }
}

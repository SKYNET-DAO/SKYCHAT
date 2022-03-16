package cn.wildfire.imshat.kit.conversation.ext;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.imshat.kit.preview.TakePhotoActivity;
import cn.wildfire.imshat.kit.third.utils.ImageUtils;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

import static android.app.Activity.RESULT_OK;

public class ShootExt extends ConversationExt {

    /**
     * @param containerView 
     * @param conversation
     */
    @ExtContextMenuItem(titleResId = R.string.shot,title = "")
    public void shoot(View containerView, Conversation conversation) {
        Intent intent = new Intent(context, TakePhotoActivity.class);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
        conversationViewModel.sendMessage(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("path");
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(context, UIUtils.getString(R.string.str_picture_error), Toast.LENGTH_SHORT).show();
                return;
            }
            if (data.getBooleanExtra("take_photo", true)) {
                
                conversationViewModel.sendImgMsg(ImageUtils.genThumbImgFile(path), new File(path));
            } else {
                
                conversationViewModel.sendVideoMsg(new File(path));
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_shot;
    }

    @Override
    public String title(Context context) {
        return UIUtils.getString(R.string.shot);
    }
}

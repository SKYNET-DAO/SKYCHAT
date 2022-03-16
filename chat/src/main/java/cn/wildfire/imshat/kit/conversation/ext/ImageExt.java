package cn.wildfire.imshat.kit.conversation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.imshat.kit.third.utils.ImageUtils;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

public class ImageExt extends ConversationExt {

    /**
     * @param containerView 
     * @param conversation
     */
    @ExtContextMenuItem(titleResId =R.string.str_picture, title = "")
    public void pickImage(View containerView, Conversation conversation) {
        Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(9).buildPickIntent(context);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
        conversationViewModel.sendMessage(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                
                boolean compress = data.getBooleanExtra(ImagePicker.EXTRA_COMPRESS, true);
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                for (ImageItem imageItem : images) {
                    File imageFileThumb;
                    File imageFileSource;
                    

                    imageFileSource = new File(imageItem.path);
                    imageFileThumb = ImageUtils.genThumbImgFile(imageItem.path);

                    conversationViewModel.sendImgMsg(imageFileThumb, imageFileSource);
                }
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_pic;
    }

    @Override
    public String title(Context context) {
        return UIUtils.getString(R.string.str_picture);
    }
}

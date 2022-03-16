package cn.wildfire.imshat.kit.conversation.ext;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.imshat.app.third.location.data.LocationData;
import cn.wildfire.imshat.app.third.location.ui.activity.MyLocationActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

import static android.app.Activity.RESULT_OK;

public class LocationExt extends ConversationExt {

    /**
     * @param containerView 
     * @param conversation
     */
    @ExtContextMenuItem(titleResId =R.string.str_location,title = "")
    public void pickLocation(View containerView, Conversation conversation) {
        Intent intent = new Intent(context, MyLocationActivity.class);
        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_LOCATION);
        conversationViewModel.sendMessage(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            LocationData locationData = (LocationData) data.getSerializableExtra("location");
            conversationViewModel.sendLocationMessage(locationData);
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_location;
    }

    @Override
    public String title(Context context) {
        return UIUtils.getString(R.string.str_location);
    }
}

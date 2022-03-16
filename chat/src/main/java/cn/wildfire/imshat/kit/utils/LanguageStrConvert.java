package cn.wildfire.imshat.kit.utils;

import android.text.TextUtils;

import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.ConversationInfo;

public class LanguageStrConvert {

    public static void convertLang(ConversationInfo conversationInfo) {

        
        if (conversationInfo!=null&&conversationInfo.lastMessage!=null&&conversationInfo.lastMessage.content instanceof TipNotificationContent) {
            TipNotificationContent tipcontent = (TipNotificationContent) conversationInfo.lastMessage.content;
            if (tipcontent.tip.equals("你们已经成为好友了，现在可以开始聊天了")) {
                tipcontent.tip = UIUtils.getString(R.string.str_notify_already_start);
            } else if (tipcontent.tip.equals("以上是打招呼信息")) {
                tipcontent.tip = UIUtils.getString(R.string.str_notify_tip);
            }

        }
    }



    public static String convertLang(String originStr) {

        
        if (originStr.equals("你们已经成为好友了，现在可以开始聊天了")) {
            originStr = UIUtils.getString(R.string.str_notify_already_start);
        } else if (originStr.equals("以上是打招呼信息")) {
            originStr = UIUtils.getString(R.string.str_notify_tip);
        }
        return originStr;
    }

}

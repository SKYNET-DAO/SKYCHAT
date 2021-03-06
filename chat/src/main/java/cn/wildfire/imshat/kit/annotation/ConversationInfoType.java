package cn.wildfire.imshat.kit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cn.wildfirechat.model.Conversation;


@Retention(RetentionPolicy.RUNTIME)
public @interface ConversationInfoType {
    Conversation.ConversationType type();

    int line();
}

package cn.wildfire.imshat.kit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import androidx.annotation.StringRes;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtContextMenuItem {
    String title() default "";

    @StringRes int titleResId() default 0;
}

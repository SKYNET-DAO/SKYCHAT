package cn.wildfire.imshat.kit.annotation;

import androidx.annotation.AttrRes;
import androidx.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import androidx.annotation.StringRes;



@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageContextMenuItem {
    String title();

    @StringRes int titleResId() default 0;

 
    String tag();

    int priority() default 0;


    boolean confirm() default false;


    String confirmPrompt() default "";
    @StringRes int confirmPromptResId() default 0;
}

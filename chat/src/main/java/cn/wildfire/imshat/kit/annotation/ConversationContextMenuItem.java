package cn.wildfire.imshat.kit.annotation;

import androidx.annotation.StringRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.wildfirechat.model.ConversationInfo;



@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConversationContextMenuItem {
    String title();

    @StringRes int titleResId() default 0;
    String tag();

    int priority() default 0;


    boolean confirm() default false;


    String confirmPrompt() default "";

    @StringRes int confirmPromptResId() default 0;
}

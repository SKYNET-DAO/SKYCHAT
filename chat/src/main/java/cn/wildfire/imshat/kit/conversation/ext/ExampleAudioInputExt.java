package cn.wildfire.imshat.kit.conversation.ext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import cn.wildfire.imshat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExt;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;


public class ExampleAudioInputExt extends ConversationExt {

    /**
     * @param containerView 
     * @param conversation
     */
    @ExtContextMenuItem(title = "Example")
    public void image(View containerView, Conversation conversation) {
        FrameLayout frameLayout = (FrameLayout) containerView;
        View view = LayoutInflater.from(context).inflate(R.layout.conversatioin_ext_example_layout, frameLayout, false);
        frameLayout.addView(view);
        extension.disableHideOnScroll();

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extension.reset();
            }
        });
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_voice;
    }

    @Override
    public String title(Context context) {
        return "Example";
    }
}

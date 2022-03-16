package cn.wildfire.imshat.kit.conversation.ext.core;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import cn.wildfire.imshat.kit.conversation.ConversationViewModel;
import cn.wildfirechat.model.Conversation;

public abstract class ConversationExt {
    protected FragmentActivity context;
    protected ConversationExtension extension;
    private int index;
    protected Conversation conversation;
    protected ConversationViewModel conversationViewModel;

    public abstract int priority();

    public abstract int iconResId();

    public abstract String title(Context context);


    public boolean filter(Conversation conversation) {
        return false;
    }



    protected final void onBind(FragmentActivity activity, ConversationViewModel conversationViewModel, Conversation conversation, ConversationExtension conversationExtension, int index) {
        this.context = activity;
        this.conversationViewModel = conversationViewModel;
        this.conversation = conversation;
        this.extension = conversationExtension;
        this.index = index;
    }

    protected final void onDestroy() {
        this.context = null;
        this.conversationViewModel = null;
        this.conversation = null;
        this.extension = null;
    }

    protected final void startActivity(Intent intent) {
        context.startActivity(intent);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new IllegalStateException("show override this method");
    }

    protected final void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode < 0 || requestCode > 256) {
            throw new IllegalArgumentException("request code should in [0, 256]");
        }
        extension.startActivityForResult(intent, requestCode, index);
    }
}

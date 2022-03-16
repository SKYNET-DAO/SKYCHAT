package cn.wildfire.imshat.kit.conversation.message.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.kit.annotation.EnableContextMenu;
import cn.wildfire.imshat.kit.annotation.MessageContentType;
import cn.wildfire.imshat.kit.annotation.ReceiveLayoutRes;
import cn.wildfire.imshat.kit.annotation.SendLayoutRes;
import cn.wildfire.imshat.kit.audio.AudioPlayManager;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;

import androidx.annotation.Nullable;

@MessageContentType(SoundMessageContent.class)
@SendLayoutRes(resId = R.layout.conversation_item_audio_send)
@ReceiveLayoutRes(resId = R.layout.conversation_item_audio_receive)
@EnableContextMenu
public class AudioMessageContentViewHolder extends MediaMessageContentViewHolder {
    @Bind(R.id.audioImageView)
    ImageView ivAudio;
    @Bind(R.id.durationTextView)
    TextView durationTextView;
    @Bind(R.id.audioContentLayout)
    RelativeLayout contentLayout;

    @Nullable
    @Bind(R.id.playStatusIndicator)
    View playStatusIndicator;

    public AudioMessageContentViewHolder(FragmentActivity context, RecyclerView.Adapter adapter, View itemView) {
        super(context, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        super.onBind(message);
        SoundMessageContent voiceMessage = (SoundMessageContent) message.message.content;
//        int increment = UIUtils.getDisplayWidth() / 2 / Config.Companion.getDEFAULT_MAX_AUDIO_RECORD_TIME_SECOND() * voiceMessage.getDuration();
        int increment = UIUtils.getDisplayWidth() / 3 / Config.Companion.getDEFAULT_MAX_AUDIO_RECORD_TIME_SECOND() * voiceMessage.getDuration();

        durationTextView.setText(voiceMessage.getDuration() + "''");
        ViewGroup.LayoutParams params = contentLayout.getLayoutParams();
        params.width = UIUtils.dip2Px(64) + UIUtils.dip2Px(increment);
        contentLayout.setLayoutParams(params);

        if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
            playStatusIndicator.setVisibility(View.VISIBLE);
        } else {
            playStatusIndicator.setVisibility(View.GONE);
        }

        AnimationDrawable animation;
        if (message.isPlaying) {
            animation = (AnimationDrawable) ivAudio.getBackground();
            if (!animation.isRunning()) {
                animation.start();
            }
        } else {
            
            ivAudio.setBackground(null);
            if (message.message.direction == MessageDirection.Send) {
                ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
            } else {
                ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
            }
        }

        
        if (message.progress == 100) {
            message.progress = 0;
            itemView.post(() -> {
                conversationViewModel.playAudioMessage(message);
            });
        }
    }

    @Override
    public void onViewRecycled() {
        


    }

    @OnClick(R.id.audioContentLayout)
    public void onClick(View view) {
        File file = conversationViewModel.mediaMessageContentFile(message);
        if (file == null) {
            return;
        }
        if (file.exists()) {
            conversationViewModel.playAudioMessage(message);
        } else {
            if (message.isDownloading) {
                return;
            }
            conversationViewModel.downloadMedia(message, file);
        }
    }

}

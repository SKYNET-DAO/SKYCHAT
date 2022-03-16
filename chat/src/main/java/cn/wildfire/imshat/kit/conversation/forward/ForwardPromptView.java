package cn.wildfire.imshat.kit.conversation.forward;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.VideoMessageContent;

public class ForwardPromptView extends LinearLayout {
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.contentTextView)
    TextView contentTextView;
    @Bind(R.id.contentImageView)
    ImageView contentImageView;
    @Bind(R.id.editText)
    EditText editText;

    public ForwardPromptView(Context context) {
        super(context);
        init();
    }

    public ForwardPromptView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForwardPromptView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ForwardPromptView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.forward_prompt_dialog, this, true);
        ButterKnife.bind(this, view);
    }

    public void bind(String targetName, String targetPortrait, Message message) {
        nameTextView.setText(targetName);
        Glide.with(getContext()).load(targetPortrait).apply(new RequestOptions().placeholder(R.mipmap.icon_add_group).centerCrop()).into(portraitImageView);
        if (message.content instanceof ImageMessageContent) {
            contentTextView.setVisibility(GONE);
            contentImageView.setVisibility(VISIBLE);
            Bitmap bitmap = ((ImageMessageContent) message.content).getThumbnail();
            contentImageView.getLayoutParams().width = UIUtils.dip2Px(bitmap.getWidth());
            contentImageView.getLayoutParams().height = UIUtils.dip2Px(bitmap.getHeight());
            contentImageView.setImageBitmap(bitmap);
        } else if (message.content instanceof VideoMessageContent) {
            contentTextView.setVisibility(GONE);
            contentImageView.setVisibility(VISIBLE);
//            Bitmap bitmap = ((ImageMessageContent) message.content).getThumbnail();
            Bitmap bitmap = ((VideoMessageContent) message.content).getThumbnail();
            contentImageView.getLayoutParams().width = UIUtils.dip2Px(bitmap.getWidth());
            contentImageView.getLayoutParams().height = UIUtils.dip2Px(bitmap.getHeight());
            contentImageView.setImageBitmap(bitmap);
        } else {
            contentImageView.setVisibility(GONE);
            contentTextView.setVisibility(VISIBLE);
            contentTextView.setText(message.digest());
        }
        invalidate();
    }

    public String getEditText() {
        return editText.getText().toString().trim();
    }
}

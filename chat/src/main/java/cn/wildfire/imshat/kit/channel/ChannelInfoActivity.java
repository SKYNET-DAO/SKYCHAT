package cn.wildfire.imshat.kit.channel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.common.OperateResult;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.ChannelInfo;

public class ChannelInfoActivity extends AppCompatActivity {
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.channelNameTextView)
    TextView channelTextView;
    @Bind(R.id.channelDescTextView)
    TextView channelDescTextView;
    @Bind(R.id.followChannelButton)
    Button followChannelButton;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private boolean isFollowed = false;
    private ChannelViewModel channelViewModel;
    private ChannelInfo channelInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_info_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        Intent intent = getIntent();
        channelInfo = intent.getParcelableExtra("channelInfo");
        channelViewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);

        if (channelInfo == null) {
            String channelId = intent.getStringExtra("channelId");
            if (!TextUtils.isEmpty(channelId)) {
                channelInfo = channelViewModel.getChannelInfo(channelId, true);
            }
        }
        if (channelInfo == null) {
            finish();
            return;
        }

        
        Glide.with(this).load(channelInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.icon_add_group)).into(portraitImageView);
        channelTextView.setText(channelInfo.name);
        channelDescTextView.setText(TextUtils.isEmpty(channelInfo.desc) ? UIUtils.getString(R.string.str_channel_nothing) : channelInfo.desc);


        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        if (channelInfo.owner.equals(userViewModel.getUserId())) {
            followChannelButton.setVisibility(View.GONE);
            return;
        }

        isFollowed = channelViewModel.isListenedChannel(channelInfo.channelId);
        if (isFollowed) {
            followChannelButton.setText(UIUtils.getString(R.string.str_cancel_receiver_channel));
        } else {
            followChannelButton.setText(UIUtils.getString(R.string.str_receiver_target_channel));
        }
    }

    @OnClick(R.id.followChannelButton)
    void followChannelButtonClick() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(isFollowed ? UIUtils.getString(R.string.str_channel_receivering) :UIUtils.getString(R.string.str_channel_receiver))
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();
        channelViewModel.listenChannel(channelInfo.channelId, !isFollowed).observe(this, new Observer<OperateResult<Boolean>>() {
            @Override
            public void onChanged(@Nullable OperateResult<Boolean> booleanOperateResult) {
                dialog.dismiss();
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(ChannelInfoActivity.this, isFollowed ? UIUtils.getString(R.string.str_channel_receiver_cancel_success) : UIUtils.getString(R.string.str_receiver_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChannelInfoActivity.this, isFollowed ? UIUtils.getString(R.string.str_cancel_fail) : UIUtils.getString(R.string.str_receiver_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

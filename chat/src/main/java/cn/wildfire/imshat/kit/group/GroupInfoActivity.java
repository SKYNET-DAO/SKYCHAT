package cn.wildfire.imshat.kit.group;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;
import java.util.List;

import androidx.lifecycle.ViewModelProviders;
import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;

public class GroupInfoActivity extends WfcBaseActivity {
    private String userId;
    private String groupId;
    private GroupInfo groupInfo;
    private boolean isJoined;
    private GroupViewModel groupViewModel;
    @Bind(R.id.groupNameTextView)
    TextView groupNameTextView;
    @Bind(R.id.portraitImageView)
    ImageView groupPortraitImageView;
    @Bind(R.id.actionButton)
    Button actionButton;

    private MaterialDialog dialog;
    private int actionCount = 0;

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_groupinfo));
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);

        groupInfo = groupViewModel.getGroupInfo(groupId, true);
        if (groupInfo == null) {
            groupViewModel.groupInfoUpdateLiveData().observe(this, groupInfos -> {
                for (GroupInfo info : groupInfos) {
                    if (info.target.equals(groupId)) {
                        this.groupInfo = info;
                        showGroupInfo(info);
                        dismissLoading();
                    }
                }
            });
        } else {
            showGroupInfo(groupInfo);
        }
        List<GroupMember> groupMembers = groupViewModel.getGroupMembers(groupId, true);
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        userId = userViewModel.getUserId();
        if (groupMembers == null || groupMembers.isEmpty()) {
            showLoading();
            groupViewModel.groupMembersUpdateLiveData().observe(this, members -> {
                if (members.get(0).groupId.equals(groupId)) {

                    List<GroupMember> gMembers = groupViewModel.getGroupMembers(groupId, false);
                    for (GroupMember member : gMembers) {
                        if (member.type != GroupMember.GroupMemberType.Removed && member.memberId.equals(userId)) {
                            this.isJoined = true;
                        }
                        dismissLoading();
                        updateActionButtonStatus();
                    }
                }
            });
        } else {
            for (GroupMember member : groupMembers) {
                if (member.type != GroupMember.GroupMemberType.Removed &&member.memberId.equals(userId)) {
                    this.isJoined = true;
                }
            }
            updateActionButtonStatus();
        }
        showGroupInfo(groupInfo);
    }

    private void updateActionButtonStatus() {
        if (isJoined) {
            actionButton.setText("Enter group chat");
        } else {
            actionButton.setText("join group chat");
        }
    }

    private void showLoading() {
        actionCount++;
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(this)
                    .progress(true, 100)
                    .build();
            dialog.show();
        }
    }

    private void dismissLoading() {
        if (dialog == null || !dialog.isShowing()) {
            return;
        }
        actionCount--;
        if (actionCount <= 0) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void showGroupInfo(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return;
        }

        GlideApp.with(this)
                .load(groupInfo.portrait)
                .placeholder(R.mipmap.icon_add_group)
                .into(groupPortraitImageView);
        
        groupNameTextView.setText(groupViewModel.getGroupInfo(groupInfo.target,true).name);
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_info_activity;
    }

    @OnClick(R.id.actionButton)
    void action() {


        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("Joining...")
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();

        groupViewModel.addGroupMember(groupInfo, Collections.singletonList(userId)).observe(this,isjoin->{
            dialog.dismiss();
            if(isjoin){
                Toast.makeText(this,this.getString(R.string.str_join_success),Toast.LENGTH_SHORT).show();
                finish();
            }
            else
                Toast.makeText(this,this.getString(R.string.str_join_fail),Toast.LENGTH_SHORT).show();

        });

    }
}

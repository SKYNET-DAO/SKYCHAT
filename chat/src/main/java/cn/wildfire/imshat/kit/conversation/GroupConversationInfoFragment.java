package cn.wildfire.imshat.kit.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.lqr.optionitemview.OptionItemView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import cn.wildfire.imshat.app.main.MainActivity;
import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.WfcScheme;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.group.AddGroupMemberActivity;
import cn.wildfire.imshat.kit.group.GroupViewModel;
import cn.wildfire.imshat.kit.group.RemoveGroupMemberActivity;
import cn.wildfire.imshat.kit.group.SetGroupNameActivity;
import cn.wildfire.imshat.kit.qrcode.QRCodeActivity;
import cn.wildfire.imshat.kit.search.SearchMessageActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.UserSettingScope;

public class GroupConversationInfoFragment extends Fragment implements ConversationMemberAdapter.OnMemberClickListener, CompoundButton.OnCheckedChangeListener {

    // group
    @Bind(R.id.groupLinearLayout_0)
    LinearLayout groupLinearLayout_0;
    @Bind(R.id.groupNameOptionItemView)
    OptionItemView groupNameOptionItemView;
    @Bind(R.id.groupQRCodeOptionItemView)
    OptionItemView groupQRCodeOptionItemView;
    @Bind(R.id.groupNoticeLinearLayout)
    LinearLayout noticeLinearLayout;
    @Bind(R.id.groupNoticeTextView)
    TextView noticeTextView;
    @Bind(R.id.groupManageOptionItemView)
    OptionItemView groupManageOptionItemView;
    @Bind(R.id.groupManageDividerLine)
    View groupManageDividerLine;

    @Bind(R.id.groupLinearLayout_1)
    LinearLayout groupLinearLayout_1;
    @Bind(R.id.myGroupNickNameOptionItemView)
    OptionItemView myGroupNickNameOptionItemView;
    @Bind(R.id.showGroupMemberAliasSwitchButton)
    SwitchButton showGroupMemberNickNameSwitchButton;

    @Bind(R.id.quitButton)
    Button quitGroupButton;

    @Bind(R.id.markGroupLinearLayout)
    LinearLayout markGroupLinearLayout;
    @Bind(R.id.markGroupSwitchButton)
    SwitchButton markGroupSwitchButton;

    // common
    @Bind(R.id.memberRecyclerView)
    RecyclerView memberReclerView;
    @Bind(R.id.stickTopSwitchButton)
    SwitchButton stickTopSwitchButton;
    @Bind(R.id.silentSwitchButton)
    SwitchButton silentSwitchButton;

    private ConversationInfo conversationInfo;
    private ConversationMemberAdapter conversationMemberAdapter;
    private ConversationViewModel conversationViewModel;
    private UserViewModel userViewModel;
    private ConversationListViewModel conversationListViewModel;

    private GroupViewModel groupViewModel;
    private GroupInfo groupInfo;
    // me in group
    private GroupMember groupMember;


    public static GroupConversationInfoFragment newInstance(ConversationInfo conversationInfo) {
        GroupConversationInfoFragment fragment = new GroupConversationInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("conversationInfo", conversationInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        conversationInfo = args.getParcelable("conversationInfo");
        assert conversationInfo != null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_info_group_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        Logger.e("------init()----->");
        conversationViewModel = ViewModelProviders.of(this, new ConversationViewModelFactory(conversationInfo.conversation)).get(ConversationViewModel.class);
        conversationListViewModel=ViewModelProviders.of(this,new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Group), Arrays.asList(0))).get(ConversationListViewModel.class);
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        groupLinearLayout_0.setVisibility(View.VISIBLE);
        groupLinearLayout_1.setVisibility(View.VISIBLE);
        markGroupLinearLayout.setVisibility(View.VISIBLE);
        markGroupSwitchButton.setOnCheckedChangeListener(this);
        quitGroupButton.setVisibility(View.VISIBLE);

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);

        loadAndShowGroupMembers(true);



        if (groupMember == null || groupInfo == null) {
            Toast.makeText(getActivity(), getString(R.string.str_not_include_group), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }


        showGroupMemberNickNameSwitchButton.setChecked("1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target)));
        showGroupMemberNickNameSwitchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target, isChecked ? "1" : "0");
        });

        myGroupNickNameOptionItemView.setRightText(groupMember.alias);
        groupNameOptionItemView.setRightText(groupInfo.name);

        stickTopSwitchButton.setChecked(conversationInfo.isTop);
        silentSwitchButton.setChecked(conversationInfo.isSilent);
        stickTopSwitchButton.setOnCheckedChangeListener(this);
        silentSwitchButton.setOnCheckedChangeListener(this);

        if (groupInfo != null && ChatManagerHolder.gChatManager.getUserId().equals(groupInfo.owner)) {
            quitGroupButton.setText(R.string.delete_and_dismiss);
        } else {
            quitGroupButton.setText(R.string.delete_and_exit);
        }

        observerFavGroupsUpdate();
        observerGroupInfoUpdate();
        observerGroupMembersUpdate();
    }

    private void observerFavGroupsUpdate() {
        groupViewModel.getMyGroups().observe(this, listOperateResult -> {
            if (listOperateResult.isSuccess()) {
                for (GroupInfo info : listOperateResult.getResult()) {
                    if (groupInfo.target.equals(info.target)) {
                        markGroupSwitchButton.setChecked(true);
                        break;
                    }
                }
            }
        });
    }

    private void observerGroupMembersUpdate() {
        groupViewModel.groupMembersUpdateLiveData().observe(this, groupMembers -> {
            loadAndShowGroupMembers(false);
        });
    }

    private void observerGroupInfoUpdate() {
        groupViewModel.groupInfoUpdateLiveData().observe(this, groupInfos -> {
            for (GroupInfo groupInfo : groupInfos) {
                if (groupInfo.target.equals(this.groupInfo.target)) {
                    groupNameOptionItemView.setRightText(groupInfo.name);
                    loadAndShowGroupMembers(false);
                    break;
                }
            }

        });
    }

    private void loadAndShowGroupMembers(boolean refresh) {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        String userId = userViewModel.getUserId();
        List<GroupMember> groupMembers = groupViewModel.getGroupMembers(conversationInfo.conversation.target, refresh);
        List<String> memberIds = new ArrayList<>();
        for (GroupMember member : groupMembers) {
            if (member.memberId.equals(userId)) {
                groupMember = member;
            }
            if (member.type != GroupMember.GroupMemberType.Removed) {
                memberIds.add(member.memberId);
            }
        }
        groupInfo = groupViewModel.getGroupInfo(conversationInfo.conversation.target, true);

        if (groupMember == null || groupInfo == null) {
            Toast.makeText(getActivity(), getString(R.string.str_not_include_group), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        boolean enableRemoveMember = false;
        if (groupMember.type != GroupMember.GroupMemberType.Normal || userId.equals(groupInfo.owner)) {
            Logger.e("-------add and remove----->"+enableRemoveMember);
            enableRemoveMember = true;
        }
        conversationMemberAdapter = new ConversationMemberAdapter(true, enableRemoveMember);
        List<UserInfo> members = contactViewModel.getContacts(memberIds, groupInfo.target);
        conversationMemberAdapter.setMembers(members);
        conversationMemberAdapter.setOnMemberClickListener(this);
        memberReclerView.setAdapter(conversationMemberAdapter);
        memberReclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        memberReclerView.setNestedScrollingEnabled(false);
        memberReclerView.setHasFixedSize(true);
        memberReclerView.setFocusable(false);
        //show groupmember
        if(groupInfo!=null)getActivity().setTitle(getString(R.string.str_conversation_detail)+"("+groupInfo.memberCount+")");


    }

    @OnClick(R.id.groupNameOptionItemView)
    void updateGroupName() {
        //member not to modify groupname
        if(groupInfo.owner.equals(userViewModel.getUserId())){
            Intent intent = new Intent(getActivity(), SetGroupNameActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
        }

    }

    @OnClick(R.id.groupNoticeLinearLayout)
    void updateGroupNotice() {
        // TODO
    }

    @OnClick(R.id.groupManageOptionItemView)
    void manageGroup() {
        // TODO
    }

    @OnClick(R.id.myGroupNickNameOptionItemView)
    void updateMyGroupAlias() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .input(getString(R.string.str_group_nickname), groupMember.alias, false, (dialog1, input) -> {
                    groupViewModel.modifyMyGroupAlias(groupInfo.target, input.toString().trim())
                            .observe(GroupConversationInfoFragment.this, operateResult -> {
                                if (operateResult.isSuccess()) {
                                    myGroupNickNameOptionItemView.setRightText(input.toString().trim());
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.str_group_nickname_modify_faile) + operateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .negativeText(getString(R.string.str_cancel))
                .positiveText(getString(R.string.str_ok))
                .onPositive((dialog12, which) -> {
                    dialog12.dismiss();
                })
                .build();
        dialog.show();
    }

    @OnClick(R.id.quitButton)
    void quitGroup() {

      new MaterialDialog.Builder(getActivity())
                .content(getString(R.string.str_quit_group))
                .positiveText(R.string.str_ok).onPositive((d,w)->{
                    if (groupInfo != null && userViewModel.getUserId().equals(groupInfo.owner)) {
                        groupViewModel.dismissGroup(conversationInfo.conversation.target, Collections.singletonList(0)).observe(this, aBoolean -> {
                            d.dismiss();
                            if (aBoolean != null && aBoolean) {
                                //modify by yzr sometime the groupconversation not removed
                                conversationListViewModel.removeConversation(conversationInfo);
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.str_exiting_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        groupViewModel.quitGroup(conversationInfo.conversation.target, Collections.singletonList(0)).observe(this, aBoolean -> {
                            d.dismiss();
                            if (aBoolean != null && aBoolean) {
                                //modify by yzr sometime the groupconversation not removed
                                conversationListViewModel.removeConversation(conversationInfo);
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(),  getString(R.string.str_exiting_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .negativeText(R.string.str_cancel)
                .build().show();



    }

    @OnClick(R.id.clearMessagesOptionItemView)
    void clearMessage() {

        new MaterialDialog.Builder(getActivity())
                .content(R.string.clear_chat_log)
                .negativeText(UIUtils.getString(R.string.str_cancel))
                .positiveText(UIUtils.getString(R.string.str_ok))
                .onPositive((dialog, which) -> {
                    conversationViewModel.clearConversationMessage(conversationInfo.conversation);
                })
                .build()
                .show();

    }

    @OnClick(R.id.groupQRCodeOptionItemView)
    void showGroupQRCode() {
        String qrCodeValue = WfcScheme.QR_CODE_PREFIX_GROUP + groupInfo.target;
        Intent intent = QRCodeActivity.buildQRCodeIntent(getActivity(), getString(R.string.str_group_scan), groupInfo.portrait, qrCodeValue);
        startActivity(intent);
    }

    @OnClick(R.id.searchMessageOptionItemView)
    void searchGroupMessage() {
        Intent intent = new Intent(getActivity(), SearchMessageActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        startActivity(intent);
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    @Override
    public void onAddMemberClick() {
        Intent intent = new Intent(getActivity(), AddGroupMemberActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }

    @Override
    public void onRemoveMemberClick() {
        if (groupInfo != null) {
            Intent intent = new Intent(getActivity(), RemoveGroupMemberActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
        }
    }

    private void stickTop(boolean top) {
        ConversationListViewModel conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
        conversationListViewModel.setConversationTop(conversationInfo, top);
    }

    private void markGroup(boolean mark) {
        groupViewModel.setFavGroup(groupInfo.target, mark);
    }

    private void silent(boolean silent) {
        conversationViewModel.setConversationSilent(conversationInfo.conversation, silent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.markGroupSwitchButton:
                markGroup(isChecked);
                break;
            case R.id.stickTopSwitchButton:
                stickTop(isChecked);
                break;
            case R.id.silentSwitchButton:
                silent(isChecked);
                break;
            default:
                break;
        }

    }
}

package cn.wildfire.imshat.kit.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.contact.pick.PickConversationTargetActivity;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.group.AddGroupMemberActivity;
import cn.wildfire.imshat.kit.search.SearchMessageActivity;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;

public class SingleConversationInfoFragment extends Fragment implements ConversationMemberAdapter.OnMemberClickListener, CompoundButton.OnCheckedChangeListener {

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

    private static final int REQUEST_ADD_MEMBER = 100;

    public static SingleConversationInfoFragment newInstance(ConversationInfo conversationInfo) {
        SingleConversationInfoFragment fragment = new SingleConversationInfoFragment();
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
        View view = inflater.inflate(R.layout.conversation_info_single_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        conversationViewModel = ViewModelProviders.of(this, new ConversationViewModelFactory(conversationInfo.conversation)).get(ConversationViewModel.class);
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
     //   String userId = userViewModel.getUserId();
//        conversationMemberAdapter = new ConversationMemberAdapter(true, false);
        String userId = conversationInfo.conversation.target;
        conversationMemberAdapter = new ConversationMemberAdapter(false, false);
        List<UserInfo> members = Collections.singletonList(userViewModel.getUserInfo(userId, false));
        conversationMemberAdapter.setMembers(members);
        conversationMemberAdapter.setOnMemberClickListener(this);

        memberReclerView.setAdapter(conversationMemberAdapter);
        memberReclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        stickTopSwitchButton.setChecked(conversationInfo.isTop);
        silentSwitchButton.setChecked(conversationInfo.isSilent);
        stickTopSwitchButton.setOnCheckedChangeListener(this);
        silentSwitchButton.setOnCheckedChangeListener(this);
    }

    @OnClick(R.id.clearMessagesOptionItemView)
    void clearMessage() {
        conversationViewModel.clearConversationMessage(conversationInfo.conversation);
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
        Intent intent = new Intent(getActivity(), CreateConversationActivity.class);
        ArrayList<String> participants = new ArrayList<>();
        participants.add(conversationInfo.conversation.target);
        intent.putExtra(PickConversationTargetActivity.CURRENT_PARTICIPANTS, participants);
        startActivity(intent);
    }

    @Override
    public void onRemoveMemberClick() {
        // do nothing
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_MEMBER:
                if (resultCode == AddGroupMemberActivity.RESULT_ADD_SUCCESS) {
                    List<String> memberIds = data.getStringArrayListExtra("memberIds");
                    addGroupMember(memberIds);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void addGroupMember(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }
        List<UserInfo> userInfos = userViewModel.getUserInfos(memberIds);
        if (userInfos == null) {
            return;
        }
        conversationMemberAdapter.addMembers(userInfos);
    }

    private void stickTop(boolean top) {
        ConversationListViewModel conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
        conversationListViewModel.setConversationTop(conversationInfo, top);
    }

    private void silent(boolean silent) {
        conversationViewModel.setConversationSilent(conversationInfo.conversation, silent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
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

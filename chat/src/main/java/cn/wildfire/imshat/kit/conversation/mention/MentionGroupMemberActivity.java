package cn.wildfire.imshat.kit.conversation.mention;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupInfo;

public class MentionGroupMemberActivity extends WfcBaseActivity {
    private GroupInfo groupInfo;

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_select_member));
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, MentionGroupMemberFragment.newInstance(groupInfo))
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
}

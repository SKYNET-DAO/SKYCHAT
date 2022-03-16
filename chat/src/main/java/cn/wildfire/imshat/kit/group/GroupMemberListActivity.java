package cn.wildfire.imshat.kit.group;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupInfo;

public class GroupMemberListActivity extends WfcBaseActivity {
    private GroupInfo groupInfo;

    @Override
    protected void afterViews() {
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, GroupMemberListFragment.newInstance(groupInfo))
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
}

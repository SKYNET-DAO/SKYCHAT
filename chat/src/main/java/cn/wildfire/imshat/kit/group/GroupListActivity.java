package cn.wildfire.imshat.kit.group;

import android.content.Intent;

import java.util.ArrayList;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupInfo;

public class GroupListActivity extends WfcBaseActivity {

    private boolean forResult;
   
    public static final String INTENT_FOR_RESULT = "forResult";

    
    // TODO
    public static final String MODE_SINGLE = "single";
    public static final String MODE_MULTI = "multi";

    // TODO activity or fragment?
    public static Intent buildIntent(boolean pickForResult, boolean isMultiMode) {

        return null;
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_member_list));
        forResult = getIntent().getBooleanExtra(INTENT_FOR_RESULT, false);
        GroupListFragment fragment = new GroupListFragment();
        if (forResult) {
            fragment.setOnGroupItemClickListener(groupInfo -> {
                Intent intent = new Intent();
                // TODO 
                ArrayList<GroupInfo> groupInfos = new ArrayList<>();
                groupInfos.add(groupInfo);
                intent.putParcelableArrayListExtra("groupInfos", groupInfos);
                setResult(RESULT_OK, intent);
                finish();
            });
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }
}

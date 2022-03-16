package cn.wildfire.imshat.kit.chatroom;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfirechat.imshat.R;

public class ChatRoomListActivity extends WfcBaseActivity {

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_chatroom_list));
        getSupportFragmentManager().
                beginTransaction()
                .replace(R.id.containerFrameLayout, new ChatRoomListFragment())
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
}

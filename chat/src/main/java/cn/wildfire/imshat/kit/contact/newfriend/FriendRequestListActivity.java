package cn.wildfire.imshat.kit.contact.newfriend;

import android.content.Intent;
import android.view.MenuItem;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public class FriendRequestListActivity extends WfcBaseActivity {

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_request_friend));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, new FriendRequestListFragment())
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected int menu() {
        return R.menu.contact_friend_request;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            addContact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void addContact() {
        Intent intent = new Intent(this, SearchUserActivity.class);
        startActivity(intent);
    }
}

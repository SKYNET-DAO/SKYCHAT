package cn.wildfire.imshat.kit.group;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.contact.pick.PickContactViewModel;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupInfo;

public class AddGroupMemberActivity extends WfcBaseActivity {
    private MenuItem menuItem;

    private GroupInfo groupInfo;
    public static final int RESULT_ADD_SUCCESS = 2;
    public static final int RESULT_ADD_FAIL = 3;

    private PickContactViewModel pickContactViewModel;
    private GroupViewModel groupViewModel;
    private Observer<UIUserInfo> contactCheckStatusUpdateLiveDataObserver = new Observer<UIUserInfo>() {
        @Override
        public void onChanged(@Nullable UIUserInfo userInfo) {
            List<UIUserInfo> list = pickContactViewModel.getCheckedContacts();
            if (list == null || list.isEmpty()) {
                menuItem.setTitle(getString(R.string.str_ok));
                menuItem.setEnabled(false);
            } else {
                menuItem.setTitle(getString(R.string.str_ok)+"(" + list.size() + ")");
                menuItem.setEnabled(true);
            }
        }
    };

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_add_member));
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        if (groupInfo == null) {
            finish();
            return;
        }

        pickContactViewModel = ViewModelProviders.of(this).get(PickContactViewModel.class);
        pickContactViewModel.contactCheckStatusUpdateLiveData().observeForever(contactCheckStatusUpdateLiveDataObserver);
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, AddGroupMemberFragment.newInstance(groupInfo))
                .commit();
    }

    @Override
    protected int menu() {
        return R.menu.group_add_member;
    }

    @Override
    protected void afterMenus(Menu menu) {
        menuItem = menu.findItem(R.id.add);
        super.afterMenus(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            addMember();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pickContactViewModel.contactCheckStatusUpdateLiveData().removeObserver(contactCheckStatusUpdateLiveDataObserver);
    }


    void addMember() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.str_adding))
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();
        List<UIUserInfo> checkedUsers = pickContactViewModel.getCheckedContacts();
        if (checkedUsers != null && !checkedUsers.isEmpty()) {
            ArrayList<String> checkedIds = new ArrayList<>(checkedUsers.size());
            for (UIUserInfo user : checkedUsers) {
                checkedIds.add(user.getUserInfo().uid);
            }
            groupViewModel.addGroupMember(groupInfo, checkedIds).observe(this, result -> {
                dialog.dismiss();
                Intent intent = new Intent();
                if (result) {
                    intent.putStringArrayListExtra("memberIds", checkedIds);
                    setResult(RESULT_ADD_SUCCESS, intent);
                    UIUtils.showToast(UIUtils.getString(R.string.add_member_success));
                } else {
                    UIUtils.showToast(UIUtils.getString(R.string.add_member_fail));
                    setResult(RESULT_ADD_FAIL);
                }
                finish();
            });

        }
    }

}

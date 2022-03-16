package cn.wildfire.imshat.kit.group;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModelProviders;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public class RemoveGroupMemberActivity extends BasePickGroupMemberActivity {
    private MenuItem menuItem;
    public static final int RESULT_REMOVE_SUCCESS = 2;
    public static final int RESULT_REMOVE_FAIL = 3;
    private GroupViewModel groupViewModel;
    private List<UIUserInfo> checkedGroupMembers;

    @Override
    protected void onGroupMemberChecked(List<UIUserInfo> checkedUserInfos) {
        this.checkedGroupMembers = checkedUserInfos;
        if (checkedUserInfos == null || checkedUserInfos.isEmpty()) {
            menuItem.setTitle(getString(R.string.str_delete));
            menuItem.setEnabled(false);
        } else {
            menuItem.setTitle(getString(R.string.str_delete)+"(" + checkedUserInfos.size() + ")");
            menuItem.setEnabled(true);
        }
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        setTitle(getString(R.string.str_del_member));
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
    }

    @Override
    protected int menu() {
        return R.menu.group_remove_member;
    }

    @Override
    protected void afterMenus(Menu menu) {
        menuItem = menu.findItem(R.id.remove);
        menuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.remove) {
            removeMember(checkedGroupMembers);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void removeMember(List<UIUserInfo> checkedUsers) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.str_remove_member))
                .positiveText(R.string.str_ok).onPositive((d,w)->{
                    if (checkedUsers != null && !checkedUsers.isEmpty()) {
                        ArrayList<String> checkedIds = new ArrayList<>(checkedUsers.size());
                        for (UIUserInfo user : checkedUsers) {
                            checkedIds.add(user.getUserInfo().uid);
                        }
                        groupViewModel.removeGroupMember(groupInfo, checkedIds).observe(this, result -> {
                            d.dismiss();
                            if (result) {
                                Intent intent = new Intent();
                                intent.putStringArrayListExtra("memberIds", checkedIds);
                                setResult(RESULT_REMOVE_SUCCESS, intent);
                                UIUtils.showToast(UIUtils.getString(R.string.del_member_success));
                            } else {
                                setResult(RESULT_REMOVE_FAIL);
                                UIUtils.showToast(UIUtils.getString(R.string.del_member_fail));
                            }
                            finish();
                        });

                    }

                })
                .negativeText(R.string.str_cancel)
                .build();
        dialog.show();

    }

}

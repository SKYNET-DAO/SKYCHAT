package cn.wildfire.imshat.kit.user;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;

import butterknife.Bind;
import butterknife.OnTextChanged;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.common.OperateResult;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.edittext.EditTextUtils;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.UserInfo;

import static cn.wildfirechat.model.ModifyMyInfoType.Modify_DisplayName;

public class ChangeMyNameActivity extends WfcBaseActivity {

    private MenuItem confirmMenuItem;
    @Bind(R.id.nameEditText)
    EditText nameEditText;

    private UserViewModel userViewModel;
    private UserInfo userInfo;

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_setting_nickname));
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);

        userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        if (userInfo == null) {
            Toast.makeText(this, UIUtils.getString(R.string.user_no_found), Toast.LENGTH_SHORT).show();
            finish();
        }
        initView();
    }

    @Override
    protected int contentLayout() {
        return R.layout.user_change_my_name_activity;
    }

    @Override
    protected int menu() {
        return R.menu.user_change_my_name;
    }

    @Override
    protected void afterMenus(Menu menu) {
        confirmMenuItem = menu.findItem(R.id.save);
        confirmMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            changeMyName();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        if (userInfo != null) {
//            nameEditText.setText(userInfo.displayName);
            EditTextUtils.setNormalFormat(nameEditText);
            nameEditText.setText("");
        }
        nameEditText.setSelection(nameEditText.getText().toString().trim().length());
    }

    @OnTextChanged(value = R.id.nameEditText, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void inputNewName(CharSequence s, int start, int before, int count) {
        if (confirmMenuItem != null) {
            if (nameEditText.getText().toString().trim().length() > 0) {
                confirmMenuItem.setEnabled(true);
            } else {
                confirmMenuItem.setEnabled(false);
            }
        }
    }


    private void changeMyName() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.str_modifying))
                .progress(true, 100)
                .build();
        dialog.show();
        String nickName = nameEditText.getText().toString().trim();
        ModifyMyInfoEntry entry = new ModifyMyInfoEntry(Modify_DisplayName, nickName);
        userViewModel.modifyMyInfo(Collections.singletonList(entry)).observe(this, new Observer<OperateResult<Boolean>>() {
            @Override
            public void onChanged(@Nullable OperateResult<Boolean> booleanOperateResult) {
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(ChangeMyNameActivity.this, getString(R.string.str_modify_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangeMyNameActivity.this, getString(R.string.str_modify_fail), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                finish();
            }
        });
    }
}

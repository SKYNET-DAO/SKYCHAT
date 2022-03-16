package cn.wildfire.imshat.kit.conversation.forward;

import android.content.Intent;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.wildfire.imshat.kit.common.OperateResult;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.contact.pick.PickConversationTargetActivity;
import cn.wildfire.imshat.kit.group.GroupViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.GroupInfo;

public class PickConversationTargetToForwardActivity extends PickConversationTargetActivity {

    private boolean singleMode = true;

    @Override
    protected void onContactPicked(List<UIUserInfo> initialCheckedUserInfos, List<UIUserInfo> newlyCheckedUserInfos) {
        if (singleMode && newlyCheckedUserInfos.size() > 1) {

            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content(getString(R.string.str_creating))
                    .progress(true, 100)
                    .cancelable(true)
                    .build();
            dialog.show();
            GroupViewModel groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
            groupViewModel.createGroup(this, newlyCheckedUserInfos)
                    .observe(this, new Observer<OperateResult<String>>() {
                        @Override
                        public void onChanged(@Nullable OperateResult<String> result) {
                            dialog.dismiss();
                            if (result.isSuccess()) {
                                GroupInfo groupInfo = groupViewModel.getGroupInfo(result.getResult(), false);
                                Intent intent = new Intent();
                                intent.putExtra("groupInfo", groupInfo);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(PickConversationTargetToForwardActivity.this, "create group error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Intent intent = new Intent();
            intent.putExtra("userInfo", newlyCheckedUserInfos.get(0).getUserInfo());
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    @Override
    public void onGroupPicked(List<GroupInfo> groupInfos) {
        Intent intent = new Intent();
        intent.putExtra("groupInfo", groupInfos.get(0));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        setTitle(getString(R.string.str_select_conversation));
    }
}

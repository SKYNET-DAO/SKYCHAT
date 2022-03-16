package cn.wildfire.imshat.kit.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.orhanobut.logger.Logger;

import java.util.Arrays;

import cn.wildfire.imshat.app.main.MainActivity;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.conversation.ConversationViewModel;
import cn.wildfire.imshat.kit.conversation.ConversationViewModelFactory;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;

public class UserInfoActivity extends WfcBaseActivity {
   private UserInfo userInfo=null;
   private UserViewModel userViewModel;
   private  ConversationListViewModel conversationListViewModel;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {

        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        setTitle(getString(R.string.str_userinfo));
         userInfo = getIntent().getParcelableExtra("userInfo");
        Logger.e("--------UserInfoActivity.userInfo------->"+ JsonUtil.toJson(userInfo));
        if (userInfo == null) {
            finish();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrameLayout, UserInfoFragment.newInstance(userInfo))
                    .commit();
        }

         conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single), Arrays.asList(0)))
                .get(ConversationListViewModel.class);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    protected int menu() {
        return R.menu.user_info;
    }

    @Override
    protected void afterMenus(Menu menu) {
        super.afterMenus(menu);
        ContactViewModel contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);
        if (!contactViewModel.isFriend(userInfo.uid)) {
            MenuItem item = menu.findItem(R.id.delete);
            item.setEnabled(false);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            MoreOptionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void MoreOptionDialog(){

        @SuppressLint("ResourceType")
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.str_del_contact)
                .positiveText(R.string.str_ok)
                .negativeText(R.string.str_cancel)
                .cancelable(false)
                .onPositive((dialog1, which) -> {
                    delFriendOpt();
                }).build();
        dialog.show();


    }



    private void delFriendOpt(){
        ContactViewModel contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);
        contactViewModel.deleteFriend(userInfo.uid).observe(
                this, booleanOperateResult -> {
                    if (booleanOperateResult.isSuccess()) {
                        
                        conversationListViewModel.loadUnreadCount();
                        finish();

                    } else {
                        Toast.makeText(this, UIUtils.getString(R.string.str_del_error) + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


}

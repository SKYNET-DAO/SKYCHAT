package cn.wildfire.imshat.kit.contact.newfriend;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.UserInfo;

public class InviteFriendActivity extends WfcBaseActivity {
    @Bind(R.id.introTextView)
    TextView introTextView;

    private UserInfo userInfo;
    private   UserInfo me;

    @Override
    protected void afterViews() {
        super.afterViews();
        setTitle(getString(R.string.str_request_friend1));
        userInfo = getIntent().getParcelableExtra("userInfo");
        if (userInfo == null) {
            finish();
        }
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
         me = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        introTextView.setText(UIUtils.getString(R.string.str_my_is) + (me == null ? "" : me.displayName));
    }

    @Override
    protected int contentLayout() {
        return R.layout.contact_invite_activity;
    }

    @Override
    protected int menu() {
        return R.menu.contact_invite;
    }

    @OnClick(R.id.clearImageButton)
    void clear() {
        introTextView.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            invite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void invite() {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        contactViewModel.invite(userInfo.uid,introTextView.getText().toString())
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean) {
                            Logger.e("-----sent success----->"+userInfo.uid);
                            Toast.makeText(InviteFriendActivity.this, UIUtils.getString(R.string.str_friend_req_sended), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(InviteFriendActivity.this, UIUtils.getString(R.string.str_req_add_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

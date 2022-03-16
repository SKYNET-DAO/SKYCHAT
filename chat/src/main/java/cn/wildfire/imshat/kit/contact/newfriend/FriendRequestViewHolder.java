package cn.wildfire.imshat.kit.contact.newfriend;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.search.SearchViewModel;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
    private FriendRequestListFragment fragment;
    private FriendRequestListAdapter adapter;
    private FriendRequest friendRequest;
    private UserViewModel userViewModel;
    private ContactViewModel contactViewModel;

    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.introTextView)
    TextView introTextView;
    @Bind(R.id.acceptButton)
    Button acceptButton;
    @Bind(R.id.acceptStatusTextView)
    TextView acceptStatusTextView;

    public FriendRequestViewHolder(FriendRequestListFragment fragment, FriendRequestListAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);

    }

    @OnClick(R.id.acceptButton)
    void accept() {
        Logger.e("-------accept request-------->"+friendRequest.target);
        contactViewModel.acceptFriendRequest(friendRequest.target).observe(fragment, aBoolean -> {
            if (aBoolean) {
                this.friendRequest.status = 1;
                acceptButton.setVisibility(View.GONE);
                contactViewModel.contactListUpdatedLiveData().postValue(new Object());
            } else {
                Toast.makeText(fragment.getActivity(), UIUtils.getString(R.string.str_op_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBind(FriendRequest friendRequest) {
        Logger.e("------friendRequest-------->"+ JsonUtil.toJson(friendRequest));
        this.friendRequest = friendRequest;
        UserInfo userInfo0= ViewModelProviders.of(fragment).get(UserViewModel.class).getUserInfo(friendRequest.target,false);
//
        Logger.e("------friendRequest--->userInfo 00-------->"+ JsonUtil.toJson(userInfo0));
        UserInfo userInfo = userViewModel.getUserInfo(friendRequest.target, false);
        Logger.e("------friendRequest--->userInfo-------->"+ JsonUtil.toJson(userInfo));



//        if (friendRequest.reason.contains(":")&&!TextUtils.isEmpty(friendRequest.reason)) {
//            introTextView.setText(friendRequest.reason.split(":")[1]);
//            nameTextView.setText(friendRequest.reason.split(":")[0]);
//        }else{
//
//
//        }



        if (userInfo != null && !TextUtils.isEmpty(userInfo.displayName)) {

            //modify by yzr -- fix：try to get userinfo with refresh
           UserInfo refreshUserInfo= userViewModel.getUserInfo(userInfo.uid,true);
           if(refreshUserInfo!=null){
               nameTextView.setText(refreshUserInfo.displayName);
           }else{
               nameTextView.setText(userInfo.displayName);
           }

        }else {
            nameTextView.setText("<" + friendRequest.target + ">");
        }

        // TODO status

        switch (friendRequest.status) {
            case 0:
                acceptButton.setVisibility(View.VISIBLE);
                acceptStatusTextView.setVisibility(View.GONE);
                break;
            case 1:
                acceptButton.setVisibility(View.GONE);
                acceptStatusTextView.setText(UIUtils.getString(R.string.str_added));
                break;
            default:
                acceptButton.setVisibility(View.GONE);
                acceptStatusTextView.setText(UIUtils.getString(R.string.str_refused));
                break;
        }

        if (userInfo != null) {
            Glide.with(fragment).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.avatar_def).centerCrop()).into(portraitImageView);
        }
    }

}

package cn.wildfire.imshat.kit.contact.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.contact.ContactAdapter;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfirechat.imshat.R;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected ContactAdapter adapter;
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.categoryTextView)
    TextView categoryTextView;

    protected UIUserInfo userInfo;

    public ContactViewHolder(Fragment fragment, ContactAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    public void onBind(UIUserInfo userInfo) {
        this.userInfo = userInfo;
        if (userInfo.isShowCategory()) {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryTextView.setText(userInfo.getCategory());
        } else {
            categoryTextView.setVisibility(View.GONE);
        }
        UserViewModel userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        String displayName= userViewModel.getUserDisplayName(userInfo.getUserInfo());
        String displayAlias=userViewModel.getFriendAlias(userInfo.getUserInfo().uid);
        Logger.e("-------displayName---displayAlias-->"+displayName+" "+displayAlias);
        if(!TextUtils.isEmpty(displayAlias)){
            nameTextView.setText(displayAlias);
        }else{
            nameTextView.setText(displayName);
        }

        GlideApp.with(fragment).load(userInfo.getUserInfo().portrait).error(R.mipmap.default_header).into(portraitImageView);
    }

    public UIUserInfo getBindContact() {
        return userInfo;
    }
}

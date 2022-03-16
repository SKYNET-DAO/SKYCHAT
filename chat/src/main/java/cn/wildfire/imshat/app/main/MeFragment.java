package cn.wildfire.imshat.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.optionitemview.OptionItemView;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.setting.SettingActivity;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.language.LanguageActivity;
import cn.wildfire.imshat.wallet.JsonUtil;
//import cn.wildfire.chat.wallet.activity.WalletActivity;
import cn.wildfire.imshat.wallet.activity.TrustWalletActivity;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.UserInfo;

public class MeFragment extends Fragment {

    @Bind(R.id.meLinearLayout)
    LinearLayout meLinearLayout;
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.accountTextView)
    TextView accountTextView;

    @Bind(R.id.notificationOptionItemView)
    OptionItemView notificationOptionItem;

    @Bind(R.id.settintOptionItemView)
    OptionItemView settingOptionItem;

    @Bind(R.id.walletOptionItemView)
    OptionItemView walletOptionItem;


    @Bind(R.id.languageOptionItemView1)
    OptionItemView languageOptionItem;



    private UserViewModel userViewModel;
    private UserInfo userInfo;

    private Observer<List<UserInfo>> userInfoLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (userInfos == null) {
                return;
            }
            for (UserInfo info : userInfos) {
                if (info.uid.equals(userViewModel.getUserId())) {
                    userInfo = info;
                    Logger.e("----MeFragment---onChanged---->"+JsonUtil.toJson(info));
                    updateUserInfo(userInfo);
                    ((MainActivity)getActivity()).checkDisplayName(userInfo);

                    break;
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_me, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }



    private void updateUserInfo(UserInfo userInfo) {
        Glide.with(this).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.avatar_def).centerCrop()).into(portraitImageView);
        nameTextView.setText(userInfo.displayName);
        accountTextView.setText("Account: " + userInfo.name);

    }

    private void init() {
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        userViewModel.getUserInfoAsync(userViewModel.getUserId(), true)
                .observe(this, info -> {
                    userInfo = info;
                    if (userInfo != null) {
                     //    Logger.e("-----MeFragment--onChanged1---->"+JsonUtil.toJson(info));
                        updateUserInfo(userInfo);
                    }
                });
        userViewModel.userInfoLiveData().observeForever(userInfoLiveDataObserver);

    }


    @Override
    public void onResume() {
        super.onResume();
        Logger.e("-----onResume------>");
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        userViewModel.userInfoLiveData().removeObserver(userInfoLiveDataObserver);
    }

    @OnClick(R.id.meLinearLayout)
    void showMyInfo() {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    @OnClick(R.id.settintOptionItemView)
    void setting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }


    @OnClick(R.id.walletOptionItemView)
    void wallet() {
        Intent intent = new Intent(getActivity(), TrustWalletActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.languageOptionItemView1)
    void switchLanguage() {
        Intent intent = new Intent(getActivity(), LanguageActivity.class);
        startActivity(intent);
    }


}

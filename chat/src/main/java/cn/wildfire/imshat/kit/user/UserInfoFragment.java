package cn.wildfire.imshat.kit.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.optionitemview.OptionItemView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.WfcScheme;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.common.OperateResult;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.contact.newfriend.InviteFriendActivity;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.imshat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.imshat.kit.qrcode.QRCodeActivity;
import cn.wildfire.imshat.kit.third.utils.ImageUtils;
import cn.wildfire.imshat.wallet.BitUtil;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class UserInfoFragment extends Fragment {
    @Bind(R.id.portraitImageView)
    ImageView portraitImageView;
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.mobileTextView)
    TextView mobileTextView;
    //@Bind(R.id.nickyName)
  //  TextView nickyNameTextView;
    @Bind(R.id.chatButton)
    Button chatButton;
    @Bind(R.id.voipChatButton)
    Button voipChatButton;
    @Bind(R.id.inviteButton)
    Button inviteButton;
    @Bind(R.id.aliasOptionItemView)
    OptionItemView aliasOptionItemView;

    @Bind(R.id.qrCodeOptionItemView)
    OptionItemView qrCodeOptionItemView;

    private UserInfo userInfo;
    private UserViewModel userViewModel;
    private ContactViewModel contactViewModel;
    private  ConversationListViewModel  conversationListViewModel;

    public static UserInfoFragment newInstance(UserInfo userInfo) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("userInfo", userInfo);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        userInfo = args.getParcelable("userInfo");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        contactViewModel = WfcUIKit.getAppScopeViewModel(ContactViewModel.class);

          conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single), Arrays.asList(0)))
                .get(ConversationListViewModel.class);

        String selfUid = userViewModel.getUserId();
        if (!TextUtils.isEmpty(selfUid)&&selfUid.equals(userInfo.uid)) {
            // self
            chatButton.setVisibility(View.GONE);
            voipChatButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.GONE);
            qrCodeOptionItemView.setVisibility(View.VISIBLE);
            aliasOptionItemView.setVisibility(View.VISIBLE);
        } else if (contactViewModel.isFriend(userInfo.uid)) {
            // friend
            chatButton.setVisibility(View.VISIBLE);
         //   voipChatButton.setVisibility(View.VISIBLE);
            inviteButton.setVisibility(View.GONE);

        } else {
            // stranger
            chatButton.setVisibility(View.GONE);
            voipChatButton.setVisibility(View.GONE);
            inviteButton.setVisibility(View.VISIBLE);
            aliasOptionItemView.setVisibility(View.GONE);
            
            Conversation conversation=new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0);
            ChatManager.Instance().clearUnreadStatus(conversation);
            ChatManager.Instance().removeConversation(conversation, true);
            conversationListViewModel.loadUnreadCount();

        }

        setUserInfo(userInfo);
        userViewModel.userInfoLiveData().observe(this, userInfos -> {
            for (UserInfo info : userInfos) {
                if (userInfo.uid.equals(info.uid)) {
                    userInfo = info;
                    setUserInfo(info);
                    break;
                }
            }
        });
        userViewModel.getUserInfo(userInfo.uid, true);

    }

    private void setUserInfo(UserInfo userInfo) {
        Glide.with(this).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.avatar_def).centerCrop()).into(portraitImageView);

        nameTextView.setText(userViewModel.getUserDisplayName(userInfo));
//        if(!TextUtils.isEmpty(userInfo.friendAlias)){
//            nickyNameTextView.setText(userInfo.friendAlias);
//        }else{
//            nickyNameTextView.setText("");
//        }
//        nameTextView.setVisibility(View.GONE);
      //  nickyNameTextView.setText(userViewModel.getUserDisplayName(userInfo));
      //  nickyNameTextView.setText(userInfo.displayName);
        if (ChatManager.Instance().isMyFriend(userInfo.uid)) {
            mobileTextView.setText(getString(R.string.str_tel) + userInfo.mobile);
          //  mobileTextView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.chatButton)
    void chat() {
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.voipChatButton)
    void voipChat() {
        WfcUIKit.onCall(getActivity(), userInfo.uid, true, false);
    }

    @OnClick(R.id.aliasOptionItemView)
    void alias() {
        String selfUid = userViewModel.getUserId();
        if (selfUid.equals(userInfo.uid)) {
            Intent intent = new Intent(getActivity(), ChangeMyNameActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), SetAliasActivity.class);
            intent.putExtra("userId", userInfo.uid);
            startActivity(intent);
        }
    }

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    @OnClick(R.id.portraitImageView)
    void portrait() {
        if (userInfo.uid.equals(userViewModel.getUserId())) {
            updatePortrait();
        } else {
            // TODO show big portrait
        }
    }

    private void updatePortrait() {
	
        ImagePicker.picker().showCamera(true).pick(this, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            String imagePath = ImageUtils.genThumbImgFile(images.get(0).path).getAbsolutePath();
            MutableLiveData<OperateResult<Boolean>> result = userViewModel.updateUserPortrait(imagePath);
            result.observe(this, booleanOperateResult -> {
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(getActivity(), this.getString(R.string.str_updaeAvatar_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), this.getString(R.string.str_updaeAvatar_fail) + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.inviteButton)
    void invite() {
        Intent intent = new Intent(getActivity(), InviteFriendActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.qrCodeOptionItemView)
    void showMyQRCode() {
        UserInfo userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);

        Logger.e("----------showMyQRCode-userInfo--->"+ JsonUtil.toJson(userInfo));
        if(userInfo!=null&& !TextUtils.isEmpty(userInfo.displayName)&&!BitUtil.INSTANCE.isBTCValidAddress(userInfo.displayName)){
            String qrCodeValue = WfcScheme.QR_CODE_PREFIX_USER + userInfo.uid;
            startActivity(QRCodeActivity.buildQRCodeIntent(getActivity(), this.getString(R.string.str_scan), userInfo.portrait, qrCodeValue));
        }else{
            Toast.makeText(getActivity(),this.getString(R.string.str_not_address),Toast.LENGTH_SHORT).show();
        }

    }
}

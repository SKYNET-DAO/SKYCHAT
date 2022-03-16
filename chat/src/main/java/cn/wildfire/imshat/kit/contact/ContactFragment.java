package cn.wildfire.imshat.kit.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.orhanobut.logger.Logger;

import java.util.List;

import cn.wildfire.imshat.app.main.MainActivity;
import cn.wildfire.imshat.kit.IMServiceStatusViewModel;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.channel.ChannelListActivity;
import cn.wildfire.imshat.kit.channel.ChannelViewModel;
import cn.wildfire.imshat.kit.contact.model.ContactCountFooterValue;
import cn.wildfire.imshat.kit.contact.model.FriendRequestValue;
import cn.wildfire.imshat.kit.contact.model.GroupValue;
import cn.wildfire.imshat.kit.contact.model.HeaderValue;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.imshat.kit.contact.viewholder.footer.ContactCountViewHolder;
import cn.wildfire.imshat.kit.contact.viewholder.header.ChannelViewHolder;
import cn.wildfire.imshat.kit.contact.viewholder.header.FriendRequestViewHolder;
import cn.wildfire.imshat.kit.contact.viewholder.header.GroupViewHolder;
import cn.wildfire.imshat.kit.group.GroupListActivity;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.kit.widget.QuickIndexBar;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetGroupsCallback;

public class ContactFragment extends BaseContactFragment implements QuickIndexBar.OnLetterUpdateListener {
    private UserViewModel userViewModel;
    private IMServiceStatusViewModel imServiceStatusViewModel;
    private ChannelViewModel channelViewModel;

    private Observer<Integer> friendRequestUpdateLiveDataObserver = count -> {
        Logger.e("---------ContactFragment.friendRequestUpdateLiveDataObserver------->");
        FriendRequestValue requestValue = new FriendRequestValue(count == null ? 0 : count);
        contactAdapter.updateHeader(0, requestValue);
    };

    private Observer<Object> contactListUpdateLiveDataObserver = o -> {
        Logger.e("------Contacts-contactListUpdateLiveDataObserver--->"+JsonUtil.toJson(o));
        loadContacts();
    };

    private Observer<Boolean> imStatusLiveDataObserver = status -> {
        if (status && (contactAdapter != null && (contactAdapter.contacts == null || contactAdapter.contacts.size() == 0))) {
            loadContacts();
        }
    };

    private void loadContacts() {
        contactViewModel.getContactsAsync(false)
                .observe(this, userInfos -> {

                    if(userInfos==null)return;
                    Logger.e("------contacts-loadContacts----->"+JsonUtil.toJson(userInfos));
                    contactAdapter.setContacts(userInfoToUIUserInfo(userInfos));
                    contactAdapter.notifyDataSetChanged();

                    for (UserInfo info : userInfos) {
                        if (info.name == null || info.displayName == null) {
                            userViewModel.getUserInfo(info.uid, true);
                        }
                    }
                });




    }

    private Observer<List<UserInfo>> userInfoLiveDataObserver = userInfos -> {
        Logger.e("-----contacts-update user profile-userInfoLiveDataObserver---->");
        contactAdapter.updateContacts(userInfoToUIUserInfo(userInfos));
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        contactViewModel.contactListUpdatedLiveData().observeForever(contactListUpdateLiveDataObserver);
        contactViewModel.friendRequestUpdatedLiveData().observeForever(friendRequestUpdateLiveDataObserver);

        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        userViewModel.userInfoLiveData().observeForever(userInfoLiveDataObserver);
        channelViewModel = ViewModelProviders.of(getActivity()).get(ChannelViewModel.class);
        imServiceStatusViewModel = WfcUIKit.getAppScopeViewModel(IMServiceStatusViewModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observeForever(imStatusLiveDataObserver);
//        init();
        return view;
    }

    
    private void init(){

       channelViewModel.getMyChannels();
       channelViewModel.getListenedChannels();

        ChatManager.Instance().getMyGroups(new GetGroupsCallback() {
            @Override
            public void onSuccess(List<GroupInfo> groupInfos) {

            }

            @Override
            public void onFail(int errorCode) {
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contactViewModel.contactListUpdatedLiveData().removeObserver(contactListUpdateLiveDataObserver);
        contactViewModel.friendRequestUpdatedLiveData().removeObserver(friendRequestUpdateLiveDataObserver);
        userViewModel.userInfoLiveData().removeObserver(userInfoLiveDataObserver);
        imServiceStatusViewModel.imServiceStatusLiveData().removeObserver(imStatusLiveDataObserver);
    }

    @Override
    public void initHeaderViewHolders() {
        addHeaderViewHolder(FriendRequestViewHolder.class, new FriendRequestValue(contactViewModel.getUnreadFriendRequestCount()));
        addHeaderViewHolder(GroupViewHolder.class, new GroupValue());
       // addHeaderViewHolder(ChannelViewHolder.class, new HeaderValue());
    }

    @Override
    public void initFooterViewHolders() {
        addFooterViewHolder(ContactCountViewHolder.class, new ContactCountFooterValue());
    }

    @Override
    public void onContactClick(UIUserInfo userInfo) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo.getUserInfo());
        startActivity(intent);
    }

    @Override
    public void onHeaderClick(int index) {
        switch (index) {
            case 0:
                ((MainActivity) getActivity()).hideUnreadFriendRequestBadgeView();
                showFriendRequest();
                break;
            case 1:
                showGroupList();
                break;
            case 2:
                showChannelList();
                break;
            default:
                break;
        }
    }

    private void showFriendRequest() {
        FriendRequestValue value = new FriendRequestValue(0);
        contactAdapter.updateHeader(0, value);

        contactViewModel.clearUnreadFriendRequestStatus();
        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
        startActivity(intent);
    }

    private void showGroupList() {
        Intent intent = new Intent(getActivity(), GroupListActivity.class);
        startActivity(intent);
    }

    private void showChannelList() {
        Intent intent = new Intent(getActivity(), ChannelListActivity.class);
        startActivity(intent);
    }


    @Override
    public void onResume() {//fix  maybe the contact not refresh with del friend
        super.onResume();
        loadContacts();
    }
}

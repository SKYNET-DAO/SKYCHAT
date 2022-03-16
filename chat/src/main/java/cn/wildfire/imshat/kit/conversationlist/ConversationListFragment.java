package cn.wildfire.imshat.kit.conversationlist;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.base.utils.ACacheUtil;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.wildfire.imshat.kit.IMServiceStatusViewModel;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.conversationlist.notification.ConnectionStatusNotification;
import cn.wildfire.imshat.kit.conversationlist.notification.StatusNotificationViewModel;
import cn.wildfire.imshat.kit.group.GroupViewModel;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class ConversationListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;
    private static final List<Conversation.ConversationType> types = Arrays.asList(Conversation.ConversationType.Single,
            Conversation.ConversationType.Group,
            Conversation.ConversationType.Channel);
    private static final List<Integer> lines = Arrays.asList(0);

    private ConversationListViewModel conversationListViewModel;
    private IMServiceStatusViewModel imServiceStatusViewModel;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private Observer<ConversationInfo> conversationInfoObserver = new Observer<ConversationInfo>() {
        @Override
        public void onChanged(@Nullable ConversationInfo conversationInfo) {
            // just handle what we care about
            if (types.contains(conversationInfo.conversation.type) && lines.contains(conversationInfo.conversation.line)) {
               Logger.e("------conversationInfoObserver----->"+JsonUtil.toJson(conversationInfo.lastMessage));

                adapter.submitConversationInfo(conversationInfo);

                // scroll or not?
//                 recyclerView.scrollToPosition(0);
            }
        }
    };

    private Observer<Conversation> conversationRemovedObserver = new Observer<Conversation>() {
        @Override
        public void onChanged(@Nullable Conversation conversation) {
            if (conversation == null) {
                return;
            }
            if (types.contains(conversation.type) && lines.contains(conversation.line)) {
                Logger.e("----------ConversationListFragment.conversationRemovedObserver--->"+JsonUtil.toJson(conversation));
                adapter.removeConversation(conversation);
            }
        }
    };

    // 会话同步
    private Observer<Object> settingUpdateObserver = o -> reloadConversations();

    private Observer<List<UserInfo>> userInfoLiveDataObserver = (userInfos) -> {
        adapter.updateUserInfos(userInfos);
    };

    private Observer<Boolean> imStatusLiveDataObserver = (connected) -> {
        if (connected) {
            if (adapter != null && (adapter.getConversationInfos() == null || adapter.getConversationInfos().size() == 0)) {
                reloadConversations();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversationlist_frament, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        init();
        return view;
    }

    private void init() {
        adapter = new ConversationListAdapter(this);
        conversationListViewModel = ViewModelProviders
                .of(getActivity(), new ConversationListViewModelFactory(types, lines))
                .get(ConversationListViewModel.class);

        Logger.e("---init() getconversationlist--->"+"init()");
        conversationListViewModel.getConversationListAsync(types, lines)
                .observe(this, conversationInfos -> {
                    Logger.e("-------conversationInfos--->"+JsonUtil.toJson(conversationInfos));
                    adapter.setConversationInfos(conversationInfos);
                    adapter.notifyDataSetChanged();

                });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //modify by yzr
       // DividerItemDecoration itemDecor = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        //itemDecor.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recyclerview_horizontal_divider));
        //recyclerView.addItemDecoration(itemDecor);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        conversationListViewModel.conversationInfoLiveData().observeForever(conversationInfoObserver);
        conversationListViewModel.conversationRemovedLiveData().observeForever(conversationRemovedObserver);
        conversationListViewModel.settingUpdateLiveData().observeForever(settingUpdateObserver);

        imServiceStatusViewModel = WfcUIKit.getAppScopeViewModel(IMServiceStatusViewModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observeForever(imStatusLiveDataObserver);

        userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
        userViewModel.userInfoLiveData().observeForever(userInfoLiveDataObserver);

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);

        StatusNotificationViewModel statusNotificationViewModel = WfcUIKit.getAppScopeViewModel(StatusNotificationViewModel.class);
        statusNotificationViewModel.statusNotificationLiveData().observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                adapter.updateStatusNotification(statusNotificationViewModel.getNotificationItems());
            }
        });
        conversationListViewModel.connectionStatusLiveData().observe(this, status -> {
            ConnectionStatusNotification connectionStatusNotification = new ConnectionStatusNotification();
            switch (status) {
                case ConnectionStatus.ConnectionStatusConnecting:
                    connectionStatusNotification.setValue(UIUtils.getString(R.string.str_connecting));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusConnected:
                    statusNotificationViewModel.removeStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusUnconnected:
                    connectionStatusNotification.setValue(UIUtils.getString(R.string.str_connect_fail));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                default:
                    break;
            }
        });
    }

    private void reloadConversations() {
        conversationListViewModel.getConversationListAsync(types, lines)
                .observe(this, conversationInfos -> {
                    adapter.setConversationInfos(conversationInfos);

                    new Handler().postDelayed(() -> {
                        adapter.notifyDataSetChanged();
                    },2000);


                    Logger.e("-----reloadConversations----->");

                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        conversationListViewModel.conversationInfoLiveData().removeObserver(conversationInfoObserver);
        conversationListViewModel.conversationRemovedLiveData().removeObserver(conversationRemovedObserver);
        conversationListViewModel.settingUpdateLiveData().removeObserver(settingUpdateObserver);
        imServiceStatusViewModel.imServiceStatusLiveData().removeObserver(imStatusLiveDataObserver);
        userViewModel.userInfoLiveData().removeObserver(userInfoLiveDataObserver);
    }

}

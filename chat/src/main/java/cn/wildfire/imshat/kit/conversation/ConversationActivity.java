package cn.wildfire.imshat.kit.conversation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnTouch;
import cn.wildfire.imshat.kit.ChatManagerHolder;
import cn.wildfire.imshat.kit.ConfigEventViewModel;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.WfcUIKit;
import cn.wildfire.imshat.kit.audio.AudioPlayManager;
import cn.wildfire.imshat.kit.channel.ChannelViewModel;
import cn.wildfire.imshat.kit.chatroom.ChatRoomViewModel;
import cn.wildfire.imshat.kit.common.OperateResult;
import cn.wildfire.imshat.kit.contact.ContactViewModel;
import cn.wildfire.imshat.kit.contact.model.UIUserInfo;
import cn.wildfire.imshat.kit.conversation.ext.core.ConversationExtension;
import cn.wildfire.imshat.kit.conversation.mention.MentionSpan;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.group.GroupViewModel;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.user.UserInfoActivity;
import cn.wildfire.imshat.kit.user.UserViewModel;
import cn.wildfire.imshat.kit.widget.InputAwareLayout;
import cn.wildfire.imshat.kit.widget.KeyboardAwareLinearLayout;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.ErrorCode;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.UserSettingScope;

public class ConversationActivity extends WfcBaseActivity implements
        KeyboardAwareLinearLayout.OnKeyboardShownListener,
        KeyboardAwareLinearLayout.OnKeyboardHiddenListener,
        ConversationMessageAdapter.OnPortraitClickListener,
        ConversationMessageAdapter.OnPortraitLongClickListener, ConversationInputPanel.OnConversationInputPanelStateChangeListener {

    public static final int REQUEST_PICK_MENTION_CONTACT = 100;

    private Conversation conversation;
    private boolean loadingNewMessage;
    private boolean shouldContinueLoadNewMessage = false;

    private static final int MESSAGE_LOAD_COUNT_PER_TIME = 20;
    private static final int MESSAGE_LOAD_AROUND = 10;

    @Bind(R.id.rootLinearLayout)
    InputAwareLayout rootLinearLayout;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.msgRecyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.inputPanelFrameLayout)
    ConversationInputPanel inputPanel;

    private ConversationMessageAdapter adapter;
    private boolean moveToBottom = true;
    private ConversationViewModel conversationViewModel;
    
    private ContactViewModel contactViewModel;
    private UserViewModel userViewModel;
    private ChatRoomViewModel chatRoomViewModel;

    private Handler handler;
    private long initialFocusedMessageId;
    
    private String channelPrivateChatUser;
    private String conversationTitle = "";
    private SharedPreferences sharedPreferences;
    private LinearLayoutManager layoutManager;
    private GroupInfo groupInfo;
    private boolean showGroupMemberName = false;


    private Observer<UiMessage> messageLiveDataObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            MessageContent content = uiMessage.message.content;
            if (isDisplayableMessage(uiMessage)) {
                
                if (shouldContinueLoadNewMessage) {
                    shouldContinueLoadNewMessage = false;
                    reloadMessage();
                    return;
                }
                Logger.e("---------messageLiveDataObserver------->"+JsonUtil.toJson(uiMessage));
                adapter.addNewMessage(uiMessage);
                if (moveToBottom || uiMessage.message.sender.equals(ChatManager.Instance().getUserId())) {
                    UIUtils.postTaskDelay(() -> {

                                int position = adapter.getItemCount() - 1;
                                if (position < 0) {
                                    return;
                                }
                                recyclerView.scrollToPosition(position);
                            },
                            100);
                }
            }
            if (content instanceof TypingMessageContent && uiMessage.message.direction == MessageDirection.Receive) {
                updateTypingStatusTitle((TypingMessageContent) content);
            } else {
                resetConversationTitle();
            }

            if (uiMessage.message.direction == MessageDirection.Receive) {
                conversationViewModel.clearUnreadStatus(conversation);
            }
        }
    };
    /**
     * send message change
     */
    private Observer<UiMessage> messageUpdateLiveDatObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            Logger.e("---------messageUpdateLiveDatObserver------->"+JsonUtil.toJson(uiMessage));
            if (isDisplayableMessage(uiMessage)) {
                adapter.updateMessage(uiMessage);
				
                if(uiMessage.getErrorCode()==240){
                    UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversation.target, false);
                    contactViewModel.contactListUpdatedLiveData().postValue(new Object());
                    showNotFriendDialog(userInfo);
                }else if(uiMessage.getErrorCode()== ErrorCode.FILE_TOO_LARGE){
                    Logger.e("------File too big to send----->"+uiMessage.getErrorCode());
                    Toast.makeText(ConversationActivity.this,UIUtils.getString(R.string.str_send_limit_40),Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    private Observer<UiMessage> messageRemovedLiveDataObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            if (isDisplayableMessage(uiMessage)) {
                adapter.removeMessage(uiMessage);
            }
        }
    };

    private boolean isDisplayableMessage(UiMessage uiMessage) {
        MessageContent content = uiMessage.message.content;
        if (content.getPersistFlag() == PersistFlag.Persist
                || content.getPersistFlag() == PersistFlag.Persist_And_Count) {
            return true;
        }
        return false;
    }

    private Observer<Map<String, String>> mediaUploadedLiveDataObserver = new Observer<Map<String, String>>() {
        @Override
        public void onChanged(@Nullable Map<String, String> stringStringMap) {
            for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                sharedPreferences.edit()
                        .putString(entry.getKey(), entry.getValue())
                        .apply();
            }

        }
    };

    private Observer<List<UserInfo>> userInfoUpdateLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (conversation.type == Conversation.ConversationType.Single) {
                conversationTitle = null;
                setTitle();
            }
            int start = layoutManager.findFirstVisibleItemPosition();
            int end = layoutManager.findLastVisibleItemPosition();
            adapter.notifyItemRangeChanged(start, end - start, userInfos);
        }
    };

    private Observer<Object> clearMessageLiveDataObserver = (obj) -> {
        adapter.setMessages(new ArrayList<>());
        adapter.notifyDataSetChanged();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // you can setup background here
//        getWindow().setBackgroundDrawableResource(R.mipmap.ic_splash);

    }

    @Override
    protected void afterViews() {
        initView();
        sharedPreferences = getSharedPreferences("sticker", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        conversation = intent.getParcelableExtra("conversation");
        conversationTitle = intent.getStringExtra("conversationTitle");
        initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        if (conversation == null) {
            finish();
        }
        setupConversation(conversation);
        conversationViewModel.clearUnreadStatus(conversation);
        contactViewModel=WfcUIKit.getAppScopeViewModel(ContactViewModel.class);
    }

    @Override
    protected int contentLayout() {
        return R.layout.conversation_activity;
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line) {
        return buildConversationIntent(context, type, target, line, -1);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, long toFocusMessageId) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, toFocusMessageId);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, String channelPrivateChatUser) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, -1);
    }

    public static Intent buildConversationIntent(Context context, Conversation conversation, String channelPrivateChatUser, long toFocusMessageId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("toFocusMessageId", toFocusMessageId);
        intent.putExtra("channelPrivateChatUser", channelPrivateChatUser);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        conversation = intent.getParcelableExtra("conversation");
        initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        channelPrivateChatUser = intent.getStringExtra("channelPrivateChatUser");
        setupConversation(conversation);
    }

    private void initView() {
        handler = new Handler();
        rootLinearLayout.addOnKeyboardShownListener(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (adapter.getMessages() == null || adapter.getMessages().isEmpty()) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            loadMoreOldMessages();
        });

        // message list
        adapter = new ConversationMessageAdapter(this);
        adapter.setOnPortraitClickListener(this);
        adapter.setOnPortraitLongClickListener(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                if (!recyclerView.canScrollVertically(1)) {
                    moveToBottom = true;
                    if (initialFocusedMessageId != -1 && !loadingNewMessage && shouldContinueLoadNewMessage) {
                        int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisibleItem > adapter.getItemCount() - 3) {
                            loadMoreNewMessages();
                        }
                    }
                } else {
                    moveToBottom = false;
                }
            }
        });

        inputPanel.init(this, rootLinearLayout);
        inputPanel.setOnConversationInputPanelStateChangeListener(this);
    }

    private void setupConversation(Conversation conversation) {
        if (conversationViewModel == null) {
            conversationViewModel = ViewModelProviders.of(this, new ConversationViewModelFactory(conversation, channelPrivateChatUser)).get(ConversationViewModel.class);

            conversationViewModel.messageLiveData().observeForever(messageLiveDataObserver);
            conversationViewModel.messageUpdateLiveData().observeForever(messageUpdateLiveDatObserver);
            conversationViewModel.messageRemovedLiveData().observeForever(messageRemovedLiveDataObserver);
            conversationViewModel.mediaUpdateLiveData().observeForever(mediaUploadedLiveDataObserver);
            conversationViewModel.clearMessageLiveData().observeForever(clearMessageLiveDataObserver);

            userViewModel = WfcUIKit.getAppScopeViewModel(UserViewModel.class);
            userViewModel.userInfoLiveData().observeForever(userInfoUpdateLiveDataObserver);
        } else {
            conversationViewModel.setConversation(conversation, channelPrivateChatUser);
        }

        if (conversation.type == Conversation.ConversationType.Group) {
            GroupViewModel groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
            groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            groupViewModel.groupInfoUpdateLiveData().observe(this, groupInfos -> {
                for (GroupInfo info : groupInfos) {
                    if (info.target.equals(groupInfo.target)) {
                        groupInfo = info;
                        setTitle();
                        adapter.notifyDataSetChanged();
                    }
                }



            });

            showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));
            userViewModel.settingUpdatedLiveData().observe(this, o -> {
                boolean showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));
                if (this.showGroupMemberName != showGroupMemberName) {
                    this.showGroupMemberName = showGroupMemberName;
                    adapter.notifyDataSetChanged();
                }
            });


            
            if(groupInfo!=null){
                List<GroupMember> members = groupViewModel.getGroupMembers(groupInfo.target, false);
                Logger.e("-------members---->"+members.size());
                List<String> memberIds = new ArrayList<>(members.size());
                for (GroupMember member : members) {
                    memberIds.add(member.memberId);
                }
                ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
                List<UserInfo> userInfos = contactViewModel.getContacts(memberIds, groupInfo.target);
                for (GroupMember member : members) {
                    for (UserInfo userInfo : userInfos) {
                        if (!TextUtils.isEmpty(member.alias) && member.memberId.equals(userInfo.uid)) {
                            userInfo.displayName = member.alias;
                            break;
                        }
                    }
                }
            }

        }

        inputPanel.setupConversation(conversationViewModel, conversation);

        MutableLiveData<List<UiMessage>> messages;
        if (initialFocusedMessageId != -1) {
            shouldContinueLoadNewMessage = true;
            Logger.e("----------loadAroundMessages-------->");
            messages = conversationViewModel.loadAroundMessages(initialFocusedMessageId, MESSAGE_LOAD_AROUND);
        } else {
            Logger.e("--------else--loadAroundMessages-------->");
            messages = conversationViewModel.getMessages();
        }

        // load message
        swipeRefreshLayout.setRefreshing(true);
        messages.observe(this, uiMessages -> {
            swipeRefreshLayout.setRefreshing(false);

            for (UiMessage item:uiMessages) {
                if(item.message.content instanceof SoundMessageContent){
                    Logger.e("------SoundMessageContent--------->"+JsonUtil.toJson(item));
                }else if(item.message.content instanceof TextMessageContent){
                    Logger.e("------TextMessageContent--------->"+JsonUtil.toJson(item));
                }
            }
            Logger.e("-------uiMessages------->"+JsonUtil.toJson(uiMessages));
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 1) {
                int initialMessagePosition;
                if (initialFocusedMessageId != -1) {
                    initialMessagePosition = adapter.getMessagePosition(initialFocusedMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerView.scrollToPosition(initialMessagePosition);
                        adapter.highlightFocusMessage(initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });
        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            joinChatRoom();
        }

        setTitle();
    }

    private void joinChatRoom() {
        chatRoomViewModel = ViewModelProviders.of(this).get(ChatRoomViewModel.class);
        chatRoomViewModel.joinChatRoom(conversation.target)
                .observe(this, new Observer<OperateResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable OperateResult<Boolean> booleanOperateResult) {
                        if (booleanOperateResult.isSuccess()) {
                            String welcome = "Welcome %s attend chat room";
                            TipNotificationContent content = new TipNotificationContent();
                            String userId = userViewModel.getUserId();
                            UserInfo userInfo = userViewModel.getUserInfo(userId, false);
                            if (userInfo != null) {
                                content.tip = String.format(welcome, userInfo.displayName);
                            } else {
                                content.tip = String.format(welcome, "<" + userId + ">");
                            }
                            conversationViewModel.sendMessage(content);
                            loadMoreOldMessages();
                            setChatRoomConversationTitle();

                        } else {
                            Toast.makeText(ConversationActivity.this, "attend failed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void quitChatRoom() {
        String welcome = "%s leaved chat room";
        TipNotificationContent content = new TipNotificationContent();
        String userId = userViewModel.getUserId();
        UserInfo userInfo = userViewModel.getUserInfo(userId, false);
        if (userInfo != null) {
            content.tip = String.format(welcome, userInfo.displayName);
        } else {
            content.tip = String.format(welcome, "<" + userId + ">");
        }
        conversationViewModel.sendMessage(content);
        chatRoomViewModel.quitChatRoom(conversation.target);
    }

    private void setChatRoomConversationTitle() {
        chatRoomViewModel.getChatRoomInfo(conversation.target, System.currentTimeMillis())
                .observe(this, chatRoomInfoOperateResult -> {
                    if (chatRoomInfoOperateResult.isSuccess()) {
                        ChatRoomInfo chatRoomInfo = chatRoomInfoOperateResult.getResult();
                        conversationTitle = chatRoomInfo.title;
                        setTitle(conversationTitle);
                    }
                });
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(conversationTitle)) {
            setTitle(conversationTitle);
        }

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversation.target, false);
            conversationTitle = userViewModel.getUserDisplayName(userInfo);

        } else if (conversation.type == Conversation.ConversationType.Group) {
            if (groupInfo != null) {
                conversationTitle = groupInfo.name;
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            ChannelViewModel channelViewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);
            ChannelInfo channelInfo = channelViewModel.getChannelInfo(conversation.target, false);
            if (channelInfo != null) {
                conversationTitle = channelInfo.name;
            }

            if (!TextUtils.isEmpty(channelPrivateChatUser)) {
                UserInfo channelPrivateChatUserInfo = userViewModel.getUserInfo(channelPrivateChatUser, false);
                if (channelPrivateChatUserInfo != null) {
                    conversationTitle += "@" + userViewModel.getUserDisplayName(channelPrivateChatUserInfo);
                } else {
                    conversationTitle += "@<" + channelPrivateChatUser + ">";
                }
            }
        }
        setTitle(conversationTitle);
    }

    @Override
    protected int menu() {
        return R.menu.conversation;
    }



    @Override
    protected void onResume() {
        super.onResume();


        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversation.target, false);
            conversationTitle = userViewModel.getUserDisplayName(userInfo);

            if(!ChatManager.Instance().isMyFriend(conversation.target)){
                showNotFriendDialog(userInfo);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_conversation_info) {
            showConversationInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showConversationInfo() {
        Intent intent = new Intent(ConversationActivity.this, ConversationInfoActivity.class);
        intent.putExtra("conversationInfo", ChatManager.Instance().getConversation(conversation));
        startActivity(intent);
    }

    @OnTouch({R.id.contentLayout, R.id.msgRecyclerView})
    boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && inputPanel.extension.canHideOnScroll()) {
            inputPanel.collapse();
        }
        return false;
    }

    @Override
    public void onPortraitClick(UserInfo userInfo) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    @Override
    public void onPortraitLongClick(UserInfo userInfo) {
        // TODO panel insert
        int position = inputPanel.editText.getSelectionEnd();
        position = position >= 0 ? position : 0;
        if (conversation.type == Conversation.ConversationType.Group) {
            SpannableString spannableString = mentionSpannable(userInfo);
            inputPanel.editText.getEditableText().insert(position, spannableString);
        } else {
            inputPanel.editText.getEditableText().insert(position, userViewModel.getUserDisplayName(userInfo));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= ConversationExtension.REQUEST_CODE_MIN) {
            inputPanel.extension.onActivityResult(requestCode, resultCode, data);
            return;
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_MENTION_CONTACT) {
            boolean isMentionAll = data.getBooleanExtra("mentionAll", false);
            SpannableString spannableString;
            if (isMentionAll) {
                spannableString = mentionAllSpannable();
            } else {
                String userId = data.getStringExtra("userId");
                UserInfo userInfo = userViewModel.getUserInfo(userId, false);
                spannableString = mentionSpannable(userInfo);
            }
            int position = inputPanel.editText.getSelectionEnd();
            position = position > 0 ? position - 1 : 0;
            inputPanel.editText.getEditableText().replace(position, position + 1, spannableString);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SpannableString mentionAllSpannable() {
        String text = UIUtils.getString(R.string.str_notify_all);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionSpan(true), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private SpannableString mentionSpannable(UserInfo userInfo) {
        String text = "@" + userInfo.displayName + " ";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionSpan(userInfo.uid), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inputPanel.onActivityPause();
       
        AudioPlayManager.getInstance().stopPlay();
    }

    @Override
    protected void onDestroy() {
        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            quitChatRoom();
        }


        super.onDestroy();
        conversationViewModel.messageLiveData().removeObserver(messageLiveDataObserver);
        conversationViewModel.messageUpdateLiveData().removeObserver(messageUpdateLiveDatObserver);
        conversationViewModel.messageRemovedLiveData().removeObserver(messageRemovedLiveDataObserver);
        conversationViewModel.mediaUpdateLiveData().removeObserver(mediaUploadedLiveDataObserver);
        conversationViewModel.clearMessageLiveData().removeObserver(clearMessageLiveDataObserver);
        userViewModel.userInfoLiveData().removeObserver(userInfoUpdateLiveDataObserver);
        inputPanel.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (rootLinearLayout.getCurrentInput() != null) {
            rootLinearLayout.hideAttachedInput(true);
            inputPanel.collapse();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onKeyboardShown() {
        inputPanel.onKeyboardShown();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onKeyboardHidden() {
        inputPanel.onKeyboardHidden();
    }

    private void reloadMessage() {
        conversationViewModel.getMessages().observe(this, uiMessages -> {
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();
        });
    }

    private void loadMoreOldMessages() {
        long fromMessageId = Long.MAX_VALUE;
        long fromMessageUid = Long.MAX_VALUE;
        if (adapter.getMessages() != null && !adapter.getMessages().isEmpty()) {
            fromMessageId = adapter.getItem(0).message.messageId;
            fromMessageUid = adapter.getItem(0).message.messageUid;
        }
        conversationViewModel.loadOldMessages(fromMessageId, fromMessageUid, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, uiMessages -> {
                    Logger.e("-------loadOldMessages------->"+JsonUtil.toJson(uiMessages));
                    for (UiMessage item:uiMessages) {
                        if(item.message.content instanceof SoundMessageContent){
                            Logger.e("------SoundMessageContent--------->"+JsonUtil.toJson(item));
                        }else if(item.message.content instanceof TextMessageContent){
                            Logger.e("------TextMessageContent--------->"+JsonUtil.toJson(item));
                        }
                    }
                    adapter.addMessagesAtHead(uiMessages);

                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void loadMoreNewMessages() {
        loadingNewMessage = true;
        adapter.showLoadingNewMessageProgressBar();
        conversationViewModel.loadNewMessages(adapter.getItem(adapter.getItemCount() - 2).message.messageId, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, messages -> {
                    loadingNewMessage = false;
                    adapter.dismissLoadingNewMessageProgressBar();

                    if (messages == null || messages.isEmpty()) {
                        shouldContinueLoadNewMessage = false;
                    }
                    if (messages != null && !messages.isEmpty()) {
                        adapter.addMessagesAtTail(messages);
                    }
                });
    }

    private void updateTypingStatusTitle(TypingMessageContent typingMessageContent) {
        String typingDesc = "";
        switch (typingMessageContent.getType()) {
            case TypingMessageContent.TYPING_TEXT:
                typingDesc = UIUtils.getString(R.string.str_inputting);
                break;
            case TypingMessageContent.TYPING_VOICE:
                typingDesc = UIUtils.getString(R.string.str_recording);
                break;
            case TypingMessageContent.TYPING_CAMERA:
                typingDesc = UIUtils.getString(R.string.str_target_picing);
                break;
            case TypingMessageContent.TYPING_FILE:
                typingDesc = UIUtils.getString(R.string.str_target_sending);
                break;
            case TypingMessageContent.TYPING_LOCATION:
                typingDesc = UIUtils.getString(R.string.str_target_sending_location);
                break;
            default:
                typingDesc = "unknown";
                break;
        }
        setTitle(typingDesc);
        handler.postDelayed(resetConversationTitleRunnable, 5000);
    }

    private Runnable resetConversationTitleRunnable = this::resetConversationTitle;

    private void resetConversationTitle() {
        if (!TextUtils.equals(conversationTitle, getTitle())) {
            setTitle(conversationTitle);
            handler.removeCallbacks(resetConversationTitleRunnable);
        }
    }

    @Override
    public void onInputPanelExpanded() {
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onInputPanelCollapsed() {
        // do nothing
    }


    private void showNotFriendDialog(UserInfo userInfo){

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.str_not_friend)
                .positiveText(R.string.str_delete)
                .negativeText(R.string.str_cancel)
                .cancelable(false)
                .onPositive((dialog1, which) -> {

                    ChatManager.Instance().removeConversation(new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0), true);
                    finish();

                })
                .onNegative((dialog1,which)->{
                    finish();
                })

                .build();
        dialog.show();

    }

}

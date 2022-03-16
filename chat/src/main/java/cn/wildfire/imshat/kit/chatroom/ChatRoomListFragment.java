package cn.wildfire.imshat.kit.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;

public class ChatRoomListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatroom_list_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.chatRoomTextView_0, R.id.chatRoomTextView_1, R.id.chatRoomTextView_2})
    void joinChatRoom(View view) {
        String roomId = "chatroom1";
        String title = "chat room";
        if (view.getId() == R.id.chatRoomTextView_1) {
            roomId = "chatroom2";
            title = "chat room 2";
        } else if (view.getId() == R.id.chatRoomTextView_2) {
            roomId = "chatroom3";
            title = "chat room 3";
        }

        
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.ChatRoom, roomId);
        intent.putExtra("conversation", conversation);
        intent.putExtra("conversationTitle", title);
        startActivity(intent);
    }
}

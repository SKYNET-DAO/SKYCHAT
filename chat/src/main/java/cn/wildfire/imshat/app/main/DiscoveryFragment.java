package cn.wildfire.imshat.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.imshat.kit.chatroom.ChatRoomListActivity;
import cn.wildfire.imshat.kit.conversation.ConversationActivity;
import cn.wildfire.imshat.plugs.PlugManagerActivity;
import cn.wildfire.imshat.plugs.listener.OnChannelListener;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.model.Conversation;
import cn.wildfire.imshat.discovery.download.PlugActivity;

public class DiscoveryFragment extends Fragment implements OnChannelListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_discovery, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.chatRoomOptionItemView)
    void chatRoom() {
        Intent intent = new Intent(getActivity(), ChatRoomListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.robotOptionItemView)
    void robot() {
        Intent intent = ConversationActivity.buildConversationIntent(getActivity(), Conversation.ConversationType.Single, "FireRobot", 0);
        startActivity(intent);
    }

    @OnClick(R.id.channelOptionItemView)
    void channel() {

    }

    @OnClick({R.id.sky_cicrle,R.id.sky_game})
    void onCirle(){
        Toast.makeText(getActivity(),getString(R.string.str_developing),Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.sky_install})
    void showPlug(){
        Intent i=new Intent(getActivity(), PlugActivity.class);
        startActivity(i);
    }

    @Override
    public void onItemMove(int starPos, int endPos) {

    }

    @Override
    public void onMoveToMyChannel(int starPos, int endPos) {

    }

    @Override
    public void onMoveToOtherChannel(int starPos, int endPos) {

    }
}

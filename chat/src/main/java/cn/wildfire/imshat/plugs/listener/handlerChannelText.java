package cn.wildfire.imshat.plugs.listener;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.imshat.discovery.download.IPFSModel;
import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.plugs.PlugManagerActivity;
import cn.wildfire.imshat.plugs.beans.Channel;
import cn.wildfire.imshat.plugs.constants.Constant;
import cn.wildfire.imshat.plugs.utils.PreUtils;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfire.imshat.wallet.viewmodel.IPFSViewModel;
import cn.wildfirechat.imshat.R;

public class handlerChannelText implements OnChannelListener {
    private List<Channel> mSelectedChannels = new ArrayList<>();
    private List<Channel> mUnSelectedChannels = new ArrayList<>();
    private Context mContext;
    private PlugManagerActivity plugManagerActivity;

    public handlerChannelText(Context context) {
        this.mContext=context;
        plugManagerActivity=((PlugManagerActivity)mContext);
        initChannelData();

    }


    @Override
    public void onItemMove(int starPos, int endPos) {
        listMove(mSelectedChannels, starPos, endPos);
    }

    @Override
    public void onMoveToMyChannel(int starPos, int endPos) {
        Channel channel = mUnSelectedChannels.remove(starPos);
        mSelectedChannels.add(endPos, channel);
    }

    @Override
    public void onMoveToOtherChannel(int starPos, int endPos) {
        mUnSelectedChannels.add(endPos, mSelectedChannels.remove(starPos));
    }

    private void listMove(List datas, int starPos, int endPos) {
        Object o = datas.get(starPos);
        
        datas.remove(starPos);
        
        datas.add(endPos, o);
    }



    private void initChannelData() {
        String selectedChannelJson = PreUtils.getString(Constant.SELECTED_CHANNEL_JSON, "");
        String unselectChannel = PreUtils.getString(Constant.UNSELECTED_CHANNEL_JSON, "");

        if (TextUtils.isEmpty(selectedChannelJson) || TextUtils.isEmpty(unselectChannel)) {
            
            String[] channels = mContext.getResources().getStringArray(R.array.channel);
            String[] channelCodes = mContext.getResources().getStringArray(R.array.channel_code);
            
            for (int i = 0; i < channelCodes.length; i++) {
                String title = channels[i];
                String code = channelCodes[i];
                mSelectedChannels.add(new Channel(title, code));
            }

            selectedChannelJson = new Gson().toJson(mSelectedChannels);
            Logger.i("selectedChannelJson:" + selectedChannelJson);
            PreUtils.putString(Constant.SELECTED_CHANNEL_JSON, selectedChannelJson);
        } else {
            
            List<Channel> selectedChannel = new Gson().fromJson(selectedChannelJson, new TypeToken<List<Channel>>() {
            }.getType());
            List<Channel> unselectedChannel = new Gson().fromJson(unselectChannel, new TypeToken<List<Channel>>() {
            }.getType());
            mSelectedChannels.addAll(selectedChannel);
            mUnSelectedChannels.addAll(unselectedChannel);
        }

        plugManagerActivity.setOnChannelListener(this,mSelectedChannels,mUnSelectedChannels);

    }

}

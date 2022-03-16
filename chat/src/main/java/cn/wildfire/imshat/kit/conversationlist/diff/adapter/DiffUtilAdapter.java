package cn.wildfire.imshat.kit.conversationlist.diff.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;

import java.util.List;

public abstract class DiffUtilAdapter<T> extends LQRAdapterForRecyclerView<T> {

    public DiffUtilAdapter(Context context, List<T> data) {
        super(context, data);
    }

    public DiffUtilAdapter(Context context, List<T> data, int defaultLayoutId) {
        super(context, data, defaultLayoutId);
    }


    @Override
    public void onBindViewHolder(LQRViewHolderForRecyclerView holder, int position) {
        super.onBindViewHolder(holder, position);
    }



    @Override
    public void onBindViewHolder(@NonNull LQRViewHolderForRecyclerView holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty()){
            onBindViewHolder(holder,position);
        }else{
            onUpdateViewholder(holder,position,payloads);
        }
    }


   public abstract void onUpdateViewholder(@NonNull LQRViewHolderForRecyclerView holder, int position, @NonNull List<Object> payloads);


}

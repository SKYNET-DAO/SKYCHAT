package cn.wildfire.imshat.wallet.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.android.wallet.widgets.CurrencyTextView;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.vondear.rxtool.RxImageTool;

import java.util.List;

import cn.wildfire.imshat.wallet.activity.CMCExchangeDetailActivity;
import cn.wildfire.imshat.wallet.bean.TransactionItem;
import cn.wildfirechat.imshat.R;

public class WalletMainAdapter<T> extends LQRAdapterForRecyclerView<T> {

    int EMPTY_LAYOUT=0;
    Context mContext;

    public WalletMainAdapter(Context context, List data) {
        super(context, data);
    }



    public WalletMainAdapter(Context context, List data, int defaultLayoutId) {
        super(context, data, defaultLayoutId);
        mContext=context;


    }

    @Override
    public LQRViewHolderForRecyclerView onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==EMPTY_LAYOUT){
            View emptyView= LayoutInflater.from(mContext).inflate(R.layout.empt_layout,parent,false);
            return  new LQRViewHolderForRecyclerView(parent.getContext(),emptyView);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void convert(LQRViewHolderForRecyclerView helper, T item, int position) {
        if(item instanceof TransactionItem){
            TextView time= helper.getView(R.id.time);
            TextView state= helper.getView(R.id.state);
            TextView address=helper.getView(R.id.address);
            ImageView icon=helper.getView(R.id.icon);

            CurrencyTextView amount=helper.getView(R.id.amount);

            TransactionItem transactionItem=(TransactionItem)item;
            time.setText(transactionItem.time);
            state.setText(transactionItem.state);
            address.setText(transactionItem.address);
            amount.setAlwaysSigned(true);
            amount.setAmount(transactionItem.value);
            amount.setFormat(transactionItem.valueFormat);

            if(transactionItem.value.signum()<0){

                icon.setImageBitmap(RxImageTool.getBitmap(R.mipmap.item_trans_send));
            }else{

                icon.setImageBitmap(RxImageTool.getBitmap(R.mipmap.item_trans_receiver));
            }
            helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailIntent= new Intent(mContext,CMCExchangeDetailActivity.class);
                           detailIntent.putExtra("txid",transactionItem.transactionHash);
                    mContext.startActivity(detailIntent);
                }
            });

        }


    }


    @Override
    public int getItemViewType(int position) {

        if(getData().get(position) instanceof String)return EMPTY_LAYOUT;

        return super.getItemViewType(position);
    }


}

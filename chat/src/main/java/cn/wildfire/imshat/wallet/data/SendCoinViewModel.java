package cn.wildfire.imshat.wallet.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.android.wallet.send.FeeCategory;
import com.android.wallet.send.PaymentIntent;

import org.bitcoinj.core.Transaction;

import cn.wildfire.imshat.wallet.bean.AddressAndLabel;

public class SendCoinViewModel extends AndroidViewModel {


    public enum State {
        REQUEST_PAYMENT_REQUEST, //
        INPUT, // asks for confirmation
        DECRYPTING, SIGNING, SENDING, SENT, FAILED // sending states
    }

    @Nullable
    public State state = null;
    @Nullable
    public PaymentIntent paymentIntent = null;
    public FeeCategory feeCategory = FeeCategory.NORMAL;

    @Nullable
    public Boolean directPaymentAck = null;

    @Nullable
    public Exception dryrunException = null;

    @Nullable
    private DynamicFeeLiveData dynamicFeeLiveData;
    private WalletLiveData walletLiveData;
    private WalletBalanceLiveData walletBalanceLiveData;
    @Nullable
    public AddressAndLabel validatedAddress = null;

    @Nullable
    public Transaction dryrunTransaction = null;

    @Nullable
    public Transaction sentTransaction = null;

    public SendCoinViewModel(@NonNull Application application) {
        super(application);
        dynamicFeeLiveData=new DynamicFeeLiveData(application);
        walletLiveData=new WalletLiveData(application);
        walletBalanceLiveData=new WalletBalanceLiveData(application);

    }


    public DynamicFeeLiveData dynamicFeeLiveData(){

        return dynamicFeeLiveData;
    }


}

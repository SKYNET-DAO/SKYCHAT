package cn.wildfire.imshat.wallet.data;

import android.app.Application;
import android.os.AsyncTask;

import com.orhanobut.logger.Logger;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

import java.util.Set;

public class TransactionsLiveData extends AbstractWalletLiveData<Set<Transaction>> {

    private static final long THROTTLE_MS = 1000;
    private Wallet wallet;

    public TransactionsLiveData(final Application application) {
        super(application, THROTTLE_MS);
    }



    @Override
    protected void onWalletActive(final Wallet wallet) {
        this.wallet=wallet;
        addWalletListener(wallet);
        load();
    }

    
    public void setWallet(final Wallet wallet){
        onWalletActive(wallet);
    }

    @Override
    protected void onWalletAsynProcess(int process) {

    }

    @Override
    protected void onWalletInactive(final Wallet wallet) {
        removeWalletListener(wallet);
    }

    private void addWalletListener(final Wallet wallet) {
        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addReorganizeEventListener(Threading.SAME_THREAD, walletListener);
        wallet.addChangeEventListener(Threading.SAME_THREAD, walletListener);
    }

    private void removeWalletListener(final Wallet wallet) {
        wallet.removeChangeEventListener(walletListener);
        wallet.removeReorganizeEventListener(walletListener);
        wallet.removeCoinsSentEventListener(walletListener);
        wallet.removeCoinsReceivedEventListener(walletListener);
    }

    @Override
    protected void load() {
        AsyncTask.execute(() -> postValue(wallet.getTransactions(true)));
    }

    private final WalletListener walletListener = new WalletListener();

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
            WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                    final Coin newBalance) {
            Logger.e("--------TransactionsLiveData-onCoinsReceived--->");
            triggerLoad();
        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance, final Coin newBalance) {
            Logger.e("--------TransactionsLiveData-onCoinsSent--->");
            triggerLoad();
        }

        @Override
        public void onReorganize(final Wallet wallet) {
            Logger.e("--------TransactionsLiveData-onReorganize--->");
            triggerLoad();
        }

        @Override
        public void onWalletChanged(final Wallet wallet) {
            Logger.e("--------TransactionsLiveData-onWalletChanged--->");
            triggerLoad();
        }
    }

}

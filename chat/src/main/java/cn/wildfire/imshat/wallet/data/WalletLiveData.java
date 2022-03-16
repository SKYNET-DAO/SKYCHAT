package cn.wildfire.imshat.wallet.data;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;

import com.orhanobut.logger.Logger;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

import java.util.Set;

public class WalletLiveData extends AbstractWalletLiveData<Wallet> {

    private Wallet wallet;
    private DownloadProcessListener processListener;

    public WalletLiveData(Application application) {
        super(application);
    }

    public WalletLiveData(Application application, long throttleMs) {
        super(application, throttleMs);
    }


    public WalletLiveData(Application application, DownloadProcessListener processListener) {
        super(application);
        this.processListener=processListener;
    }

    @Override
    protected void onWalletActive(Wallet wallet) {
        this.wallet=wallet;
        addWalletListener(wallet);
        load();
    }

    @Override
    protected void onWalletInactive(Wallet wallet) {
        super.onWalletInactive(wallet);
        removeWalletListener(wallet);

    }

    @Override
    protected void onWalletAsynProcess(int process) {
        processListener.onProcess(process);
    }

    @Override
    protected void onWalletRestoreResult(Wallet wallet) {
        load();
    }

    @Override
    protected void load() {
        AsyncTask.execute(() -> postValue(wallet));
    }



    public interface DownloadProcessListener{
        void onProcess(int present);
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



    private final WalletListener walletListener = new WalletListener();

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
            WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                    final Coin newBalance) {
            Logger.e("--------WalletLiveData-onCoinsReceived--->");

            triggerLoad();

        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                final Coin newBalance) {
            Logger.e("--------WalletLiveData-onCoinsSent--->");
            triggerLoad();
        }

        @Override
        public void onReorganize(final Wallet wallet) {
            Logger.e("--------WalletLiveData-onReorganize--->");
            triggerLoad();
        }

        @Override
        public void onWalletChanged(final Wallet wallet) {
            Logger.e("-----onWalletChanged---->"+wallet.getUnspents());

            triggerLoad();
        }
    }



}

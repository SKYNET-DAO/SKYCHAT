/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.wildfire.imshat.wallet.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

import com.android.wallet.constants.Configuration;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;


/**
 * @author Andreas Schildbach
 */
public final class WalletBalanceLiveData extends AbstractWalletLiveData<Coin> implements OnSharedPreferenceChangeListener {

    private final BalanceType balanceType;
    private Wallet wallet;

    public WalletBalanceLiveData(final Application application, final BalanceType balanceType) {
        super(application);
        this.balanceType = balanceType;
    }

    public WalletBalanceLiveData(final Application application) {
        this(application, BalanceType.ESTIMATED);
    }

    @Override
    protected void onWalletActive(final Wallet wallet) {
        this.wallet=wallet;
        addWalletListener(wallet);
        load();
    }

    
    public void setWallet(Wallet wallet){
        onWalletActive(wallet);
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
        AsyncTask.execute(() -> postValue(wallet.getBalance(balanceType)));
    }

    private final WalletListener walletListener = new WalletListener();

    private class WalletListener implements WalletCoinsReceivedEventListener, WalletCoinsSentEventListener,
            WalletReorganizeEventListener, WalletChangeEventListener {
        @Override
        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                    final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onCoinsSent(final Wallet wallet, final Transaction tx, final Coin prevBalance,
                                final Coin newBalance) {
            triggerLoad();
        }

        @Override
        public void onReorganize(final Wallet wallet) {
            triggerLoad();
        }

        @Override
        public void onWalletChanged(final Wallet wallet) {
            triggerLoad();
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Configuration.PREFS_KEY_BTC_PRECISION.equals(key))
            load();
    }
}

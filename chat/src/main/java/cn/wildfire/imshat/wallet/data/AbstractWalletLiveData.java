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

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.orhanobut.logger.Logger;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import cn.wildfire.imshat.wallet.manager.ListenerManager;
import cn.wildfire.imshat.wallet.services.WalletService;


/**
 * @author Andreas Schildbach
 */
public abstract class AbstractWalletLiveData<T> extends ThrottelingLiveData<T> implements WalletService.WalletListener {
    private final Application application;
    private final LocalBroadcastManager broadcastManager;


    public AbstractWalletLiveData(final Application application) {
        super();
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    public AbstractWalletLiveData(final Application application, final long throttleMs) {
        super(throttleMs);
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    @Override
    protected final void onActive() {
//        broadcastManager.registerReceiver(walletReferenceChangeReceiver,
//                new IntentFilter("ACTION_WALLET_CHANGE"));
        ListenerManager.getInstance().registerWalletListner(this);
        Logger.e("-----abs-onActive()---->");
    }

    @Override
    protected final void onInactive() {
        // TODO cancel async loading
//        if (wallet != null) onWalletInactive(wallet);
//        broadcastManager.unregisterReceiver(walletReferenceChangeReceiver);
        ListenerManager.getInstance().unregisterWalletLister(this);


    }

    

    public void loadWallet() {
        WalletService.startSelf(application);

    }





    protected  void onWalletActive(Wallet wallet){

    }

    protected  void onWalletAsynProcess(int process){}

    protected void onWalletRestoreResult(Wallet wallet){}


    @Override
    public void onWalletActiveCompelet(Wallet wallet) {
        onWalletActive(wallet);
    }

    @Override
    public void onWalletAsynProcessStart(int present) {
        onWalletAsynProcess(present);
    }


    @Override
    public void onWalletRestore(Wallet wallet) {
        onWalletRestoreResult(wallet);
    }

    protected void onWalletInactive(final Wallet wallet) {
        // do nothing by default
    }

    public static interface OnWalletLoadedListener {
        void onWalletLoaded(Wallet wallet);
    }



    public interface DownloadAsynLister{

        void onPrecess(int present);
    }


}

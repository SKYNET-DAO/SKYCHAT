package cn.wildfire.imshat.wallet;

import android.util.SparseArray;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

public class WalletManager {

    private static WalletManager instance;
    private static WalletAppKit walletAppKit;
    private SparseArray<Wallet> walletList=new SparseArray<>();
    public static WalletManager getInstance(){

        synchronized (WalletManager.class) {

            if (instance == null) {
                instance = new WalletManager();
                return instance;
            }

            return instance;

        }

    }



    public Wallet getWallet(){
       return walletList.get(0);
    }



    public void addWallet(Wallet wallet){
        walletList.clear();
        walletList.append(0,wallet);

    }


    public  WalletAppKit getWalletAppKit() {
        return walletAppKit;
    }

    public void clear(){

        walletList.clear();

    }





}

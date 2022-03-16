package cn.wildfire.imshat.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.android.base.BaseApp;
import com.android.wallet.constants.Configuration;
import com.android.wallet.constants.Constants;
import com.android.wallet.manager.IPManager;
import com.android.wallet.utils.IpV4Util;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Service;
import com.orhanobut.logger.Logger;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.MultiplexingDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.net.discovery.PeerDiscoveryException;
import org.bitcoinj.net.discovery.SeedPeers;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.web3j.crypto.Bip39Wallet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.wildfire.imshat.wallet.data.AbstractWalletLiveData;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class Kit {


    
    public static WalletAppKit getWalletKit(String seedcode, AbstractWalletLiveData.DownloadAsynLister downloadAsynLister) {

        Logger.e("------path---->"+BaseApp.getContext().getFilesDir().getPath()+"/"+"cmcwallet");
        WalletAppKit walletAppKit = new WalletAppKit(Constants.NETWORK_PARAMETERS, new File(BaseApp.getContext().getFilesDir().getPath()+"/"+"cmcwallet"), "CMC_WALLET") {
            @SuppressLint("CheckResult")
            @Override
            protected void onSetupCompleted() {

                Logger.e("-----wallet().getImportedKeys()---->"+wallet().getImportedKeys().size());
                if (wallet().getImportedKeys().size() < 1) {
                    wallet().importKey(new ECKey());
                }
//                wallet().allowSpendingUnconfirmedTransactions();

                String pubkey=BitUtil.INSTANCE.getPubKeyFrom(wallet());
                Logger.e("------ecKey--->"+" "+pubkey);


                List<String> seedWordsFromWallet = BitUtil.INSTANCE.getSeedWordsFromWallet(wallet());
                Logger.e("-------seedWordsFromWallet---->" + JsonUtil.toJson(seedWordsFromWallet));


            }
        };

        walletAppKit.setAutoSave(true);
        walletAppKit.setBlockingStartup(false);

//        try {
//            if(Constants.NETWORK_PARAMETERS instanceof TestNet3Params){
//
//                walletAppKit.setCheckpoints(BaseApp.getContext().getAssets().open("checkpoints-testnet.txt"));
//
//            }else{
//                walletAppKit.setCheckpoints(BaseApp.getContext().getAssets().open("checkpoints.txt"));
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        if (downloadAsynLister != null)
            BitUtil.INSTANCE.setDownListener(walletAppKit, downloadAsynLister);

        if (!TextUtils.isEmpty(seedcode)) {
            try {
                Logger.e("-------seedcode111--->" + seedcode);
                // Logger.e("-------getKeyChainSeed-hex--seedcode->"+walletAppKit.wallet().getKeyChainSeed().toHexString());
               

                DeterministicSeed seed = new DeterministicSeed(seedcode, null, "",1559320399);
                Logger.e("-------seed--->" + seed.toString());
                walletAppKit.restoreWalletFromSeed(seed);
            } catch (UnreadableWalletException e) {
                e.printStackTrace();
            }
        }

        List<PeerAddress>  peerAddressList=IPManager.initIp();
        walletAppKit.setPeerNodes(peerAddressList.toArray(new PeerAddress[peerAddressList.size()]));

        walletAppKit.startAsync().awaitRunning();
        Logger.e("-------walletAppKit.state()--->" + walletAppKit.state());

        return walletAppKit;
    }


    public static int maxConnectedPeers() {
        ActivityManager activityManager = (ActivityManager) BaseApp.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getMemoryClass() <= 128 ? 4 : 6;
    }

    private static void initPeerGroup(WalletAppKit walletAppKit) {

        
//      Configuration configuration= new Configuration(PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext()),BaseApp.getContext().getResources());
//        PeerGroup peerGroup=walletAppKit.peerGroup();
//        final int maxConnectedPeers = maxConnectedPeers();
//        final String trustedPeerHost = configuration.getTrustedPeerHost();
//        final boolean hasTrustedPeer = trustedPeerHost != null;
//
//        final boolean connectTrustedPeerOnly =true; //hasTrustedPeer && configuration.getTrustedPeerOnly();
//        peerGroup.setMaxConnections(connectTrustedPeerOnly ? 1 : maxConnectedPeers);
//        peerGroup.setConnectTimeoutMillis(Constants.PEER_TIMEOUT_MS);
//        //scan seed peersbitcoinj-core-0.16-SNAPSHOT.jar
////        peerGroup.addPeerDiscovery(new SeedPeers(Constants.NETWORK_PARAMETERS));
//        //scan discovery
//        peerGroup.setPeerDiscoveryTimeoutMillis(Constants.PEER_DISCOVERY_TIMEOUT_MS);
//        peerGroup.addPeerDiscovery(new PeerDiscovery() {
//            private final PeerDiscovery normalPeerDiscovery = MultiplexingDiscovery
//                    .forServices(Constants.NETWORK_PARAMETERS, 0);
//
//
//            @Override
//            public InetSocketAddress[] getPeers(final long services, final long timeoutValue,
//                                                final TimeUnit timeoutUnit) throws PeerDiscoveryException {
//                final List<InetSocketAddress> peers = new LinkedList<InetSocketAddress>();
//
//                boolean needsTrimPeersWorkaround = false;
//
//                if (true) {
//                    Logger.i(
//                            "trusted peer '" + trustedPeerHost + "'" + (connectTrustedPeerOnly ? " only" : ""));
//
//                    final InetSocketAddress addr = new InetSocketAddress(Constants.NETWORK_PARAMETERS.getId(),
//                            Constants.NETWORK_PARAMETERS.getPort());
//                    if (addr.getAddress() != null) {
//                        peers.add(addr);
//                        needsTrimPeersWorkaround = true;
//                    }
//                }
//
//                if (!connectTrustedPeerOnly)
//                    peers.addAll(
//                            Arrays.asList(normalPeerDiscovery.getPeers(services, timeoutValue, timeoutUnit)));
//
//                // workaround because PeerGroup will shuffle peers
//                if (needsTrimPeersWorkaround)
//                    while (peers.size() >= maxConnectedPeers)
//                        peers.remove(peers.size() - 1);
//
//                return peers.toArray(new InetSocketAddress[0]);
//            }
//
//            @Override
//            public void shutdown() {
//                normalPeerDiscovery.shutdown();
//            }
//        });


        

        //  byte[] addr = {43,(byte)249,31,38};
        walletAppKit.setPeerNodes(new PeerAddress(Constants.NETWORK_PARAMETERS, Constants.NETWORK_PARAMETERS.getId(), Constants.NETWORK_PARAMETERS.getPort()));


    }


//    public  WalletAppKit getWalletKitRestore(Context context, String seedcode){
//
//        
//        WalletAppKit walletAppKit = new WalletAppKit(BitUtil.INSTANCE.getParams(), new File(context.getFilesDir().getPath()), "CMC_WALLET") {
//            @SuppressLint("CheckResult")
//            @Override
//            protected void onSetupCompleted() {
//
//                if (wallet().getImportedKeys().size() < 1){
//                    ECKey key=new ECKey();
//                    wallet().importKey(key);
//                }
//                wallet().allowSpendingUnconfirmedTransactions();
//                BitUtil.INSTANCE.setupWalletListeners(wallet());
//                ECKey ecKey = wallet().getImportedKeys().get(0);
//
////                test2(ecKey);
////                
//                List<String> seedWordsFromWallet = BitUtil.INSTANCE.getSeedWordsFromWallet(wallet());
//                Logger.e("-------seedWordsFromWallet---->"+JsonUtil.toJson(seedWordsFromWallet));
//
//
//
//            }
//        };
//
//
//        walletAppKit.setAutoSave(true);
//        walletAppKit.setBlockingStartup(false);
//
//        BitUtil.INSTANCE.setDownListener(walletAppKit,new DownloadProgressTracker(){
//
//
//            @Override
//            protected void progress(double pct, int blocksSoFar, Date date) {
//                int percentage = Integer.parseInt(pct+"");
//                
//                walletViewModel.walletProgressLiveData().postValue(percentage);
//            }
//
//            @Override
//            protected void doneDownload() {
//                walletViewModel.walletProgressLiveData().postValue(100);
//            }
//
//        });
//
//        if(!TextUtils.isEmpty(seedcode)){
//            try {
//                Logger.e("-------seedcode111--->"+seedcode);
//                // Logger.e("-------getKeyChainSeed-hex--seedcode->"+walletAppKit.wallet().getKeyChainSeed().toHexString());
//                
//                DeterministicSeed seed = new DeterministicSeed(seedcode,null, "", Utils.currentTimeSeconds());
//                walletAppKit.restoreWalletFromSeed(seed);
//
//            } catch (UnreadableWalletException e) {
//                e.printStackTrace();
//            }
//        }
//        walletAppKit.startAsync();
//        walletAppKit.awaitRunning();
//        Logger.d("------save walletAppKit---->"+walletAppKit.wallet());
//        return  walletAppKit;
//    }


//    public static Flowable<WalletAppKit> createOrLoadBTCWallet(String seedcode) {

//        return Flowable.just(seedcode)
//                .observeOn(Schedulers.io())
//                .map(s -> getWalletKit(s));
//    }


//    public static Flowable<WalletAppKit> createOrLoadBTCWalletRestore(Context context,String seedcode) {
//
//        return Flowable.just(seedcode)
//                .observeOn(Schedulers.io())
//                .map(s -> getWalletKitRestore(context, s));
//    }


}

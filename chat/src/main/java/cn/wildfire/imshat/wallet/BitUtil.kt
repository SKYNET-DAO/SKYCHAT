package cn.wildfire.imshat.wallet

import android.annotation.SuppressLint

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import cn.wildfire.imshat.app.MyApp
import cn.wildfire.imshat.kit.WfcUIKit
import cn.wildfire.imshat.wallet.data.AbstractWalletLiveData
import com.google.common.base.Joiner
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.store.BlockStoreException
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChainGroup
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.UnreadableWalletException
import org.bitcoinj.wallet.Wallet
//import org.bouncycastle.util.encoders.Hex
import org.spongycastle.pqc.math.linearalgebra.ByteUtils
import org.web3j.utils.Numeric

import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.util.Date

import cn.wildfirechat.imshat.R
import com.android.base.BaseApp
import com.android.wallet.constants.Configuration
import com.android.wallet.constants.Constants
import com.android.wallet.utils.BitcoinUtils
import com.android.wallet.widgets.CurrencyTextView
import com.cjt2325.cameralibrary.util.LogUtil
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.bitcoinj.core.*
import org.bitcoinj.crypto.MnemonicCode
import org.spongycastle.util.encoders.Hex

import java.sql.DriverManager.println

//import com.subgraph.orchid.encoders.Hex;

object BitUtil {

    val passphrase = ""




    val bLockFile: File
        get() {
            val file = File("/tmp/bitcoin-blocks")
            if (!file.exists()) {
                try {
                    val newFile = file.createNewFile()
                    if (newFile) {
                        return file
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return file
        }

    val params: NetworkParameters
        get() = if (true) MainNetParams.get() else TestNet3Params.get()

    //get root address
    fun getMasterAddress1(wallet: Wallet,networkParameters: NetworkParameters): String {

          var  deterministickey=HDKeyDerivation.createMasterPrivateKey(MnemonicCode.toSeed(wallet.keyChainSeed.mnemonicCode,""))

        Logger.e("---------deterministickey------>${deterministickey}")

         var pubkey= BitcoinUtils.generatePublicKey(deterministickey.privateKeyAsHex,true)
        Logger.e("---------pubkey------>$pubkey")
        Logger.e("------------private------>${ deterministickey.privateKeyAsHex}")
       var address= BitcoinUtils.generateAddressWithBase58Check(pubkey)
        Logger.e("------address--->$address")
        return address
    }



   
    fun getSeedWordsFromWallet(wallet: Wallet): List<String>? {
        val seed = wallet.keyChainSeed
        return seed.mnemonicCode

    }

   
    fun getECKeyFromPriKey(priKey: String): ECKey {
        return ECKey.fromPrivate(Numeric.toBigInt(priKey))
    }

    fun getPubKeyFrom(wallet: Wallet): String {
        val ecKey = wallet.importedKeys.get(0)
        val pubkey = ecKey.publicKeyAsHex
        return pubkey
    }


    
    fun getFromSpeed(seedCode: String): Wallet? {
        val params = Constants.NETWORK_PARAMETERS
        val seed: DeterministicSeed
        try {
            seed = DeterministicSeed(seedCode, null, passphrase, Utils.currentTimeSeconds())

            return Wallet.fromSeed(params, seed)
        } catch (e: UnreadableWalletException) {
            e.printStackTrace()
        }

        return null
    }



    fun watchAddress() {
        val toWatch: Wallet? = null
        val watchingKey = toWatch!!.watchingKey
        Logger.e("--------watchaddress------>" + toWatch.watchedScripts.size)
        //
        //        String s = watchingKey.serializePubB58(getParams());
        //        long creationTimeSeconds = watchingKey.getCreationTimeSeconds();
        //
        //
        //        DeterministicKey key = DeterministicKey.deserializeB58(null, "key data goes here",getParams());
        //
        //        Wallet wallet = Wallet.fromWatchingKey(getParams(), key);


        //        NetworkParameters params = TestNet3Params.get();
        //
        //        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(),128,"password", Utils.currentTimeSeconds());
        //        wallet = Wallet.fromSeed(params,seed);
        //
        //        //tobytes
        //        byte[] bytes = MnemonicCode.toSeed(new ArrayList<>(), passphrase);

    }




    fun setDownListener(walletAppKit: WalletAppKit,downloadAsynListener:AbstractWalletLiveData.DownloadAsynLister) {

        walletAppKit.setDownloadListener(object : DownloadProgressTracker() {
            override fun progress(pct: Double, blocksSoFar: Int, date: Date) {
                val percentage = pct.toInt()
                
                downloadAsynListener.onPrecess(percentage)

            }

            override fun doneDownload() {
                val myAddress = walletAppKit.wallet().currentReceiveAddress()
                val legacyAddress = LegacyAddress.fromPubKeyHash(Constants.NETWORK_PARAMETERS, myAddress.hash)
                val s = walletAppKit.wallet()
                LogUtil.e("--------sync seccess------>address+balance==" + legacyAddress.toBase58() + "  " + s.toString())
                downloadAsynListener.onPrecess(100)

            }
        })


    }

    fun setupWalletListeners(wallet: Wallet) {
        wallet.addCoinsReceivedEventListener { wallet1, tx, prevBalance, newBalance ->
            val s = wallet.balance.toFriendlyString()
            var s1 = ""
            if (tx.purpose == Transaction.Purpose.UNKNOWN) {
                s1 = newBalance.minus(prevBalance).toFriendlyString()
            }
            Logger.e("-----receiverd coin----->$s===$s1")
            Logger.d("--------receiverd--tx----->$tx")
            Logger.d("--------wallet-json---->" + JsonUtil.toJson(wallet)!!)

        }
        wallet.addCoinsSentEventListener { wallet12, tx, prevBalance, newBalance ->
            val s = wallet.balance.toFriendlyString()
            val s1 = "Sent " + prevBalance.minus(newBalance).minus(tx.fee).toFriendlyString()
            Logger.e("-----send coin----->$s===$s1")

        }

    }

    //    public void Test() {
    //
    //        ECKey k1 = new ECKey(); // some random key
    //
    //        // encrypting a key
    //        KeyCrypter crypter1 = new KeyCrypterScrypt();
    //
    //        KeyParameter aesKey1 = crypter1.deriveKey("some arbitrary passphrase");
    //        ECKey k2 = k1.encrypt(crypter1, aesKey1);
    //        //System.out.println(k2.isEncrypted()); // true
    //
    //        // decrypting a key
    //        KeyCrypter crypter2 = k2.getKeyCrypter();
    //        KeyParameter aesKey2 = crypter2.deriveKey("some arbitrary passphrase");
    //        ECKey k3 = k2.decrypt(aesKey2);
    //
    //        //System.out.println(k1.equals(k3));  // true
    //    }

//
//    fun closedWallet() {
//        val kit = WalletManager.getInstance().walletAppKit
//        kit?.stopAsync()?.awaitTerminated()
//
//    }

    fun test2() {
        //        DeterministicKey deterministicKey = AppContext.getInstance().walletAppKit.wallet().getWatchingKey().dropPrivateBytes();
        //        deterministicKey = HDKeyDerivation.createMasterPubKeyFromBytes(deterministicKey.getPubKey(), deterministicKey.getChainCode());
        //        String xPublicKey = deterministicKey.serializePubB58(getParams());
        //        String  privateKey= AppContext.getInstance().walletAppKit.wallet().getKeyByPath(DeterministicKeyChain.ACCOUNT_ZERO_PATH).getPrivateKeyAsWiF(getParams());
        //
        //        Log.e("key", xPublicKey.toString());
        //        Log.e("privatekey", privateKey.toString());
        //        if (getParams() == RegTestParams.get()) {
        //            AppContext.getInstance().walletAppKit.connectToLocalHost();
        //        }
    }

    fun si(privateKey: String, recipientAddress: String, amount: String) {
        val request = SendRequest.to(Address.fromString(Constants.NETWORK_PARAMETERS,
                recipientAddress), Coin.parseCoin(amount))
        Signingtrasaction(privateKey, request.tx.hashAsString)
    }

    fun Signingtrasaction(wif: String, msg: String) {
        try {
            // creating a key object from WiF
            val dpk = DumpedPrivateKey.fromBase58(Constants.NETWORK_PARAMETERS, wif)
            val key = dpk.key
            // checking our key object
            // NetworkParameters main = MainNetParams.get();
            val check = key.getPrivateKeyAsWiF(Constants.NETWORK_PARAMETERS)
            println(wif == check)  // true
            Log.e("wif check", (wif == check).toString())
            // creating Sha object from string
            val hash = Sha256Hash.wrap(msg)
            // creating signature
            val sig = key.sign(hash)
            // encoding
            val res = sig.encodeToDER()
            // converting to hex
            //String hex = DatatypeConverter.printHexBinary(res);
            // String hex = new String(res);
            val hex = android.util.Base64.encodeToString(res, 16)
            Log.e("sigendTransiction", hex.toString())
            Log.e("decrypttx", "" + Hex.decode(sig.encodeToDER()))
        } catch (e: Exception) {   //signingkey = ecdsa.from_string(privateKey.decode('hex'), curve=ecdsa.SECP256k1)
            Log.e("signing exception", e.message.toString())
        }

    }


    fun getTransationHistory(wallet: Wallet) {

        val blockStore = MemoryBlockStore(Constants.NETWORK_PARAMETERS)
        //        Wallet wallet= walletAppKit.wallet();
        var chain: BlockChain? = null
        try {
            chain = BlockChain(MainNetParams.get(), wallet, blockStore)
        } catch (e: BlockStoreException) {
            e.printStackTrace()
        }

        val peerGroup = PeerGroup(MainNetParams.get(), chain)
        peerGroup.startAsync()

        //        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
        //            @Override
        //            public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
        //                System.out.println("\nReceived tx " + tx.getHashAsString());
        //                System.out.println(tx.toString());
        //            }
        //        });

        // Now download and process the block chain.
        peerGroup.downloadBlockChain()


    }


    fun addressToHash160() {


        val address = Address.fromString(Constants.NETWORK_PARAMETERS, "1PaMbW8eQzYvemskqc52SUxznae1faPjjp")
        val script = ScriptBuilder.createOutputScript(address)
        //  System.out.println("----------->hash 160:"+script.toString().split("\\[")[1].split("\\]")[0]);
    }



    //pubkey to address
    fun getAddressFromPubkey(pubkey: String): String {
        val hex = ByteUtils.fromHexString(pubkey)
        val legacyAddress1 = LegacyAddress.fromPubKeyHash(Constants.NETWORK_PARAMETERS, Utils.sha256hash160(hex))
        return legacyAddress1.toBase58()
    }


    fun isBTCValidAddress(address:String? ):Boolean {
        return try {
            var legacyAddress=LegacyAddress.fromBase58(Constants.NETWORK_PARAMETERS,address)
            return legacyAddress.toBase58() != null
        } catch (e:Exception){
            false

        }
    }



    fun formatFWalletAmount(coin: Coin):String{
        var configuration= Configuration(PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext()),BaseApp.getContext().resources)
        var balancetxtview= CurrencyTextView(BaseApp.getContext())
        balancetxtview.setFormat(configuration.format)
        balancetxtview.setAmount(coin)
        Logger.e("----balancetxtview---->${ balancetxtview.text}")
        return balancetxtview.text.toString()
    }










}

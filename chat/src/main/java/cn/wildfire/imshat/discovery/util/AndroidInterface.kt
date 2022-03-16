package cn.wildfire.imshat.discovery.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.preference.PreferenceManager
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import cn.wildfire.imshat.discovery.download.Constants
import cn.wildfire.imshat.kit.third.utils.ImageUtils
import cn.wildfire.imshat.kit.third.utils.UIUtils
import cn.wildfire.imshat.wallet.BitUtil
import cn.wildfire.imshat.wallet.BitUtil.formatFWalletAmount
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.MaterialDialog
import com.android.base.BaseApp
import com.android.base.LanguageUtil
import com.android.wallet.constants.Configuration
import com.android.wallet.utils.MonetarySpannable
import com.android.wallet.widgets.CurrencyTextView


import com.just.agentweb.AgentWeb
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxClipboardTool
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jnr.constants.Constant
import kotlinx.android.synthetic.main.activity_trust_wallet.*
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Monetary
import org.bitcoinj.utils.MonetaryFormat
import org.bitcoinj.wallet.Wallet


class AndroidInterface(private val agent: AgentWeb, private val context: Context,var wallet: Wallet) {

    private val deliver = Handler(Looper.getMainLooper())
    var sharedPreferences=context.getSharedPreferences("config",Context.MODE_PRIVATE)
    var webagent=agent
    var mWallet=wallet


    @JavascriptInterface
    fun init() {
        Logger.e("------init---->")
        providerWalletAddress()
        providerLang()
        providerWalletBalance()
        providerWalletMaster()
        providerWalletPubkey()
    }


    @JavascriptInterface
    fun getQrcode(qrcode:String){
        
        Logger.e("------qrcode---->$qrcode")
        saveImg2Gallery(qrcode)
    }


   // @JavascriptInterface
  //  fun Auth(msg:String){

  //      loadJsMethod()


//        if(isAuth()==1){
//            loadJsMethod()
//        }else{
//            val dialog = MaterialDialog.Builder(context)
//                    .title(R.string.str_agree_app)
//                    .content(R.string.str_need_auth)
//                    .positiveText(R.string.str_agree_app)
//                    .negativeText(R.string.str_noagree_app)
//                    .cancelable(false)
//                    .onPositive { d, w ->
//
//                    }
//                    .onNegative { d, w ->}
//                    .build()
//            dialog.show()
//        }

  //  }





    fun providerWalletAddress(){
        Logger.e("------providerWalletAddress--->")

        loadJsMethod()
    }


    fun providerLang(){

        Logger.e("------lang--->${LanguageUtil.getCurrentLang()}")
        webagent?.jsAccessEntrace?.quickCallJs("providerLang('${LanguageUtil.getCurrentLang()}')")
    }


    // app auth to plugin
    fun isAuth():Int{
        return  sharedPreferences?.getInt("auth",0)!!
    }


    //send wallet address to js
    fun  loadJsMethod(){
//        var data = JsonUtil.toJson(wallet?.currentReceiveAddress())
        Logger.e("-------wallet---->${wallet.currentReceiveAddress()}")
//        webagent?.jsAccessEntrace?.quickCallJs("callByAndroid")

        webagent?.jsAccessEntrace?.quickCallJs("getReciverCoinAddress('${wallet.currentReceiveAddress()}')")


    }




    fun  providerWalletBalance(){

        var balancecoin=wallet.getBalance( Wallet.BalanceType.AVAILABLE)
        var balance=formatFWalletAmount(balancecoin)
        webagent?.jsAccessEntrace?.quickCallJs("providerWalletBalance('${balance}')")
    }


    fun  providerWalletMaster(){

        val root = BitUtil.getMasterAddress1(wallet, com.android.wallet.constants.Constants.NETWORK_PARAMETERS)
        Logger.e("-------root--->$root")
        webagent?.jsAccessEntrace?.quickCallJs("providerWalletMaster('${root}')")
    }

    fun  providerWalletPubkey(){
        val pubkey = BitUtil.getPubKeyFrom(wallet)
        Logger.e("-------pubkey--->$pubkey")
        webagent?.jsAccessEntrace?.quickCallJs("providerWalletPubkey('${pubkey}')")
    }





    @JavascriptInterface
    fun copyHost(host:String){
        if(TextUtils.isEmpty(host))return
        RxClipboardTool.copyText(context,host)
        Toast.makeText(context,R.string.str_copy_address,Toast.LENGTH_SHORT).show()
    }



    @SuppressLint("CheckResult")
    private fun saveImg2Gallery(address: String?) {
        Flowable.just(address)
                .observeOn(Schedulers.io())
                .map {
                    QRCodeEncoder.syncEncodeQRCode(it, UIUtils.dp2px(BaseApp.getContext(),100f))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    ImageUtils.saveImageToGallery(BaseApp.getContext(),it)
                    Toast.makeText(BaseApp.getContext(),R.string.str_save_success,Toast.LENGTH_SHORT).show()
                }


    }




}

package cn.wildfire.imshat.wallet.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.kit.third.utils.FileUtils
import cn.wildfire.imshat.kit.third.utils.ImageUtils
import cn.wildfire.imshat.kit.third.utils.UIUtils
import cn.wildfire.imshat.wallet.WalletManager
import cn.wildfire.imshat.wallet.utils.KeyboardUtil
import cn.wildfire.imshat.wallet.utils.RxTimerUtil
import cn.wildfire.imshat.wallet.utils.SaveImgUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxClipboardTool
import com.vondear.rxtool.RxFileTool
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_receiver_cmc.*
import java.util.concurrent.TimeUnit

class ReceiverCmcActivity: WfcBaseActivity() {

    var clipboardManager:ClipboardManager?=null
    var walletViewModel:WalletViewModel?=null
    var bitmap:Bitmap?=null
    override fun contentLayout(): Int=R.layout.activity_receiver_cmc


    override fun afterViews() {
        clipboardManager= getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        title=getString(R.string.str_receiver_cmc)
        onInitView()
    }

     fun onInitView() {

         walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)
         walletViewModel?.walletLoadLiveData?.observe(this, Observer {
             address_tv.text=it.currentReceiveAddress().toString()
             makeQRCode(address_tv.text.toString())
         })
         walletViewModel?.walletLoadLiveData?.loadWallet()

         copy.setOnClickListener {
             RxClipboardTool.copyText(this,address_tv.text)
             Toast.makeText(this,R.string.str_copy_address,Toast.LENGTH_SHORT).show()
         }

         save.setOnClickListener {
             ImageUtils.saveImageToGallery(this, bitmap)
             Toast.makeText(this,R.string.str_save_success,Toast.LENGTH_SHORT).show()
         }


         RxTimerUtil.interval(3000) {
             var newaddress= walletViewModel?.walletLoadLiveData?.value?.freshReceiveAddress().toString()
             makeQRCode(newaddress)
             address_tv.text=newaddress
         }

     }


    @SuppressLint("CheckResult")
    fun makeQRCode(content:String){
        Flowable.just(content)
                .observeOn(Schedulers.io())
                .map { QRCodeEncoder.syncEncodeQRCode(it, UIUtils.dip2Px(200)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    bitmap=it
                    qrcode.setImageBitmap(it)
                }


    }

    override fun onDestroy() {
        super.onDestroy()
        RxTimerUtil.cancel()
    }


}
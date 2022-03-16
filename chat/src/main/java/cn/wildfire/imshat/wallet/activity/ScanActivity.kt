//package cn.wildfire.chat.wallet.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Vibrator
//import android.text.TextUtils
//import android.util.Log
//import android.view.MenuItem
//import android.view.View
//import android.widget.RelativeLayout
//import android.widget.TextView
//import android.widget.Toast
//import cn.bingoogolapple.qrcode.core.QRCodeView
//import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder
//import cn.bingoogolapple.qrcode.zxing.ZXingView
//import cn.wildfire.imshat.kit.WfcBaseActivity
//import cn.wildfire.imshat.wallet.BitUtil
//import cn.wildfirechat.imshat.R
//import com.lqr.imagepicker.ui.ImageGridActivity
//import com.lqr.imagepicker.ui.ImagePreviewActivity
//import com.orhanobut.logger.Logger
//import io.reactivex.Flowable
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.schedulers.Schedulers
//import java.io.File
//import java.util.ArrayList
//
//class ScanActivity: WfcBaseActivity(), QRCodeView.Delegate {
//    override fun contentLayout(): Int= R.layout.activity_scan
//
//
//    var zxingview: ZXingView?=null
//
//    override fun afterViews() {
//
//        onInitView()
//
//    }
//
//     fun onInitView() {
//
//        zxingview= findViewById(R.id.zxingview)
//        zxingview?.setDelegate(this)
//
//    }
//
//
//
//
//    override fun onStart() {
//        super.onStart()
//        
//        zxingview?.startCamera()
//        
//        zxingview?.showScanRect()
//        zxingview?.startSpot()
//    }
//
//    override fun onStop() {
//        zxingview?.stopCamera()
//        super.onStop()
//
//    }
//
//    override fun onDestroy() {
//        zxingview?.onDestroy()
//        super.onDestroy()
//
//    }
//
//
//    override fun onScanQRCodeSuccess(result: String?) {
//
//       // Logger.e("-------sqcode result------>$result")
//        Logger.e("-------sqcode result------>$result")
//
//
//        
//        (getSystemService(VIBRATOR_SERVICE) as Vibrator).apply {
//            vibrate(200); }
//        
//       
//        toJumpTarget(result)
//        
//        zxingview?.startSpot()
//
//
//    }
//
//
//
//    fun toJumpTarget(result: String?){
//
//        if(result?.contains("Receiver:")!!){//to add friend
//            var address= result.split(':')[1]
//
//            if(TextUtils.isEmpty(result)||!BitUtil.isBTCValidAddress(address)){
//
//                Toast.makeText(this,getString(R.string.str_select_unvalid_address),Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            //receiver coin
//            setResult(0x11,intent.putExtra("address",address))
//            finish()
//
//        }else if(result?.contains("Add:")!!){ //to add friend
//            //search friend
//            val friendstr = result.substring(result.indexOf("{"), result.length)
//            //get friend avator
//            if(!TextUtils.isEmpty(friendstr)){
////                ARouter.getInstance().build("/app/activitys/AddFriendActivity")
////                        .withString("friend",friendstr)
////                      //  .withString("deviceid",result[2])
////                        .navigation()
////                finish()
//            }
//
//        }
//
//
//
//    }
//
//
//
//    override fun onScanQRCodeOpenCameraError() {
//
//        Toast.makeText(this,"Failed to open camera",Toast.LENGTH_SHORT).show()
//
//    }
//
//
//    fun jump2GridImage(){
//        
//        val imagePicker = ImagePicker.getInstance()
//        imagePicker.isMultiMode = false
//        imagePicker.isShowCamera = false  
//        imagePicker.isCrop = false
//        val intent = Intent(this, ImageGridActivity::class.java)
//        intent.putExtra(ImagePreviewActivity.ISORIGIN, true)
//        startActivityForResult(intent, ImagePicker.RESULT_CODE_ITEMS)
//
//    }
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        when (resultCode) {
//
//            ImagePicker.RESULT_CODE_ITEMS -> if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
//                if (data != null) {
//                    
//                    val isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false)
//                    val images = data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) as ArrayList<ImageItem>
//                    
//                    for (imageItem in images) {
//                        val imageFileThumb: File?
//                        val imageFileSource: File?
//                        if (isOrig) {
//                            imageFileSource = File(imageItem.path)
//                            imageFileThumb = ImageUtils.genThumbImgFile(imageItem.path)
//
//                        } else {
//                            
//                            imageFileSource = ImageUtils.genThumbImgFile(imageItem.path)
//                            imageFileThumb = ImageUtils.genThumbImgFile(imageFileSource!!.absolutePath)
//                        }
//                        //                           if (imageFileSource != null && imageFileSource != null)
//                        //                               mPresenter.sendImgMsg(imageFileThumb, imageFileSource);
//
//                        if (imageFileSource != null) Flowable.just("")
//                                //   .compose(this.bindToLifecycle())
//                                .observeOn(Schedulers.io())
//                                .map { QRCodeDecoder.syncDecodeQRCode(imageFileSource.absolutePath) }
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(object : RxProgressSubscriber<String>(this) {
//                                    override fun onSuccess(data: String) {
//                                        Logger.d("-----scan success--->")
//                                        toJumpTarget(data)
//                                    }
//
//                                    override fun onError(ex: ResponseThrowable) {
//
//                                        if (ex.errorCode == "1002") return
//                                        Logger.d("-----scan fail--->" + ex.errorMsg)
//                                        Toast.makeText(this@ScanActivity,getString(R.string.str_scan_fail),Toast.LENGTH_SHORT).show()
//
//                                    }
//                                })
//
//                    }
//
//
//                }
//            }
//        }
//    }
//
//    override fun menu(): Int {
//        return R.menu.user_set_alias
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.gallery ->{
//
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//
//
//}
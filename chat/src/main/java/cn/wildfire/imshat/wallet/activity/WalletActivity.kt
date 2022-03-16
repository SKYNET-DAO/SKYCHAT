//package cn.wildfire.imshat.wallet.activity
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Build
//import android.text.InputType
//import android.view.MenuItem
//import android.view.View
//import android.widget.*
//import androidx.annotation.RequiresApi
//import androidx.lifecycle.Observer
//
//import cn.wildfire.imshat.kit.WfcBaseActivity
//import cn.wildfire.imshat.kit.WfcUIKit
//import cn.wildfire.imshat.wallet.*
//import cn.wildfire.imshat.wallet.utils.AESUtil
//import kotlinx.android.synthetic.main.activity_wallet.*
//import cn.wildfire.imshat.wallet.utils.ThemeUtils
//import cn.wildfire.imshat.wallet.utils.Utils
//import cn.wildfirechat.imshat.R
//import com.afollestad.materialdialogs.MaterialDialog
//import com.android.base.utils.ACacheUtil
//import com.orhanobut.logger.Logger
//import io.reactivex.android.schedulers.AndroidSchedulers
//import java.lang.Exception
//
//
//class WalletActivity : WfcBaseActivity() {
//
//    var walletViewModel: WalletViewModel? = null
//    var isbackup=false
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    override fun beforeViews() {
//        ThemeUtils.setStatusBarTheme(this, R.color.color_ffffff)
//        ThemeUtils.setStatusBarColor(this, R.color.color_ffffff)
//
//    }
//
//    @SuppressLint("CheckResult")
//    fun loadWallet() {
////        val dialog = MaterialDialog.Builder(this)
////                .content("Loading wallet...")
////                .progress(true, 2000)
////                .cancelable(false)
////                .build()
////        dialog.show()
//        Kit.createOrLoadBTCWallet(this, "")
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { walletAppKit ->
//                    run {
//                        WalletManager.getInstance().addWallet(walletAppKit)
////                        dialog.dismiss()
//                        updateWallet()
//                        val masteraddress = BitUtil.getMasterAddress1(walletAppKit.wallet(), BitUtil.params)
//                        Logger.e("--------masteraddress----->$masteraddress")
//
//                    }
//                }
//
//
//    }
//
//
//    override fun contentLayout(): Int = R.layout.activity_wallet
//
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun afterViews() {
//        Logger.e("-----ACacheUtil.get()------>$isbackup")
//        setTitle(getString(R.string.str_wallet))
//        findViewById<TextView>(R.id.ic_right_tv).apply {
//            this.visibility = View.VISIBLE
//            this.setTextColor(getColor(R.color.color_444444))
//            this.text = getString(R.string.str_account)
//            setOnClickListener { startActivity(Intent(this@WalletActivity, ExchangeHistoryActivity::class.java)) }
//        }
//
//        findViewById<TextView>(R.id.title).apply {
//            this.visibility = View.VISIBLE
//            this.setTextColor(getColor(R.color.color_444444))
//            this.text = getString(R.string.str_wallet)
//
//        }
//
//        findViewById<RelativeLayout>(R.id.toolbarroot).setBackgroundColor(getColor(R.color.color_ffffff))
//
//        receiver_in.setOnClickListener {
//            startActivityForResult(Intent(this, ReceiverCmcActivity::class.java), 0x12)
//        }
//
//        transform_out.setOnClickListener {
//            startActivityForResult(Intent(this, TransactionCmcActivity::class.java), 0x12)
//
//        }
//
//        backUptwallet.setOnClickListener {
//
//            if(isbackup)showInputPwdDialog()
//
//
//        }
//
//        if (WalletManager.getInstance().walletList.size == 0) {
//            loadWallet()
//        } else {
//            updateWallet()
//        }
//
//        importwallet.setOnClickListener { startActivity(Intent(this@WalletActivity, ImportMnemonicActivity::class.java)) }
//
//        walletViewModel = WfcUIKit.getAppScopeViewModel(WalletViewModel::class.java)
//        walletViewModel?.walletProgressLiveData()?.observe(this, Observer<Int> {
//            findViewById<ProgressBar>(R.id.progressBar).apply {
//                if (it == 100) {
//                    visibility = View.GONE
//                    updateWallet()
//
//                } else visibility = View.VISIBLE
//                progress = it
//            }
//        })
//
//
//
//
//
//    }
//
//
//    override fun menu(): Int {
//        return R.menu.menu_history
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.account1 -> {
//                startActivity(Intent(this@WalletActivity, BackupMnemonicActivity::class.java))
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    fun updateWallet() {
//        WalletManager.getInstance().wallet?.apply {
//            var balance = this?.balance?.toPlainString()?.toDouble()
//            if (balance == 0.0) {
//                cmc_count.text = Utils.toSubStringDegistForChart(balance!!, 2, false) + " cmc"
//            } else {
//                cmc_count.text = Utils.toSubStringDegistForChart(balance!!, 4, false) + " cmc"
//            }
//        }
//
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//
//        when (requestCode) {
//            0x12 -> {
//                if (WalletManager.getInstance().walletList.size > 0) {
//                    updateWallet()
//                }
//            }
//
//
//        }
//    }
//
//
//    fun showInitPwdDialog(){
//
//        val dialog = MaterialDialog.Builder(this)
//                .title(R.string.str_pwd)
//                .input(getString(R.string.str_setting_pwd),"",false) { dialog, input ->kotlin.run {
//
//                   
//                } }
//                .inputRange(6,15,resources.getColor(R.color.red6))
//                .inputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
//                .positiveText(R.string.str_ok)
//                .negativeText(R.string.str_cancel)
//                .cancelable(false)
//                .onPositive { dialog1, which -> kotlin.run {
//
//                    //wallet set password ,to skip the page of backup with setting true
//                    try {
//                       var psw0=  dialog1.inputEditText?.text.toString()
//                        startActivity(Intent(this@WalletActivity, BackupMnemonicActivity::class.java).apply {
//                            putExtra("wallet_psw",AESUtil().encrypt(psw0))
//                        })
//                    }catch (e:Exception){
//                        
//                        Toast.makeText(this,getString(R.string.str_pwd_auth_fail),Toast.LENGTH_SHORT).show()
//                    }
//
//
//                } }
//                .onNegative { dialog, which ->finish()}
//                .build()
//        dialog.show()
//    }
//
//
//
//
//    fun showInputPwdDialog(){
//
//        val dialog = MaterialDialog.Builder(this)
//                .title(R.string.str_pwd_input)
//                .input(getString(R.string.str_setting_pwd),"",false) { dialog, input ->kotlin.run {
//
//                    
//                } }
//                .inputRange(6,15,resources.getColor(R.color.red6))
//                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
//                .positiveText(R.string.str_ok)
//                .negativeText(R.string.str_cancel)
//                .cancelable(false)
//                .autoDismiss(false)
//                .onPositive { d, w -> kotlin.run {
//
//                    //wallet set password ,to skip the page of backup with setting true
//                    try {
//                        var psw0=  d.inputEditText?.text.toString()
//                        var cachepsw= ACacheUtil.get().getAsString("wallet_psw")
//                        if(AESUtil().decrypt(cachepsw)==psw0){
//                            d.dismiss()
//                            startActivity(Intent(this@WalletActivity, BackupMnemonicActivity::class.java))
//                        }else{
//                            d.inputEditText?.setText("")
//                            d.inputEditText?.hint=getString(R.string.str_pwd_error)
//                        }
//
//                    }catch (e:Exception){
//                        
//                        d.inputEditText?.hint=getString(R.string.str_pwd_error)
//                    }
//
//
//                } }
//                .onNegative{d,w -> d.dismiss()}
//                .build()
//        dialog.show()
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        isbackup= ACacheUtil.get().getAsBoolean("isbackup",false)
//        Logger.e("--------onResume-isbackup-------->$isbackup")
//
//        
//        if(!isbackup){
//            val dialog = MaterialDialog.Builder(this)
//                    .title(R.string.str_backup_words)
//                    .content(R.string.str_del_backup_content)
//                    .positiveText(R.string.str_backup)
//                    .negativeText(R.string.str_cancel)
//                    .neutralText(R.string.str_import)
//                    .cancelable(false)
//                    .onPositive { dialog1, which ->
//                        run {
//                            dialog1.dismiss()
//                            showInitPwdDialog()
//                        }
//                    }
//                    .onNeutral { dialog1, which ->kotlin.run {
//                        dialog1.dismiss()
//                        startActivity(Intent(this,ImportMnemonicActivity::class.java))
//                    }  }
//                    .onNegative{d,w->finish()}
//                    .build()
//            dialog.show()
//        }
//
//    }
//
//
//
//    }
//
//
//

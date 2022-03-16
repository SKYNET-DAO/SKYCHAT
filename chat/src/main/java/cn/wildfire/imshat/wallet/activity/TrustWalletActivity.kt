package cn.wildfire.imshat.wallet.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import cn.wildfire.imshat.app.Config
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.kit.WfcUIKit
import cn.wildfire.imshat.kit.third.utils.UIUtils
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfire.imshat.wallet.adapter.WalletMainAdapter
import cn.wildfire.imshat.wallet.bean.TransactionItem
import cn.wildfire.imshat.wallet.services.WalletService
import cn.wildfirechat.imshat.BuildConfig
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.android.wallet.constants.Configuration
import com.android.wallet.constants.Constants
import com.android.wallet.utils.Toast
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxClipboardTool
import kotlinx.android.synthetic.main.activity_mnemonic_import.*
import kotlinx.android.synthetic.main.activity_trust_wallet.*
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.wallet.CoinSelection
import org.bitcoinj.wallet.CoinSelector
import org.bitcoinj.wallet.DefaultCoinSelector
import org.bitcoinj.wallet.Wallet

class TrustWalletActivity : WfcBaseActivity() {

   lateinit var configuration:Configuration
    var walletViewModel: WalletViewModel?=null
    var materialDialog:MaterialDialog?=null
    lateinit  var sharedPreferences:SharedPreferences

   lateinit var lqrAdapterForRecyclerView:WalletMainAdapter<Any>

    override fun contentLayout(): Int {
        return R.layout.activity_trust_wallet
    }

    @SuppressLint("CheckResult")
    override fun afterViews() {

        initToolbar()
        sharedPreferences=getSharedPreferences("config",Context.MODE_PRIVATE)
        configuration=Configuration(PreferenceManager.getDefaultSharedPreferences(this),resources)
        walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)

     //   var wallet= WalletManager.getInstance().wallet
//        if(wallet==null){
//            walletViewModel?.loadWallet()
//        }


        walletViewModel?.walletLoadLiveData?.observe(this, Observer {
            Logger.e("-----walletLoadLiveData-TrustWalletActivity---->")
            walletViewModel?.getTransforms()
            walletViewModel?.getBalance()
           //kkkkkkkkkkkk walletViewModel?.walletLoadLiveData?.value?.reset()



        })

        walletViewModel?.walletProgressLiveData()?.observe(this, Observer {
            Logger.e("------walletProgressLiveData----->$it%")
            id_asyn_process.text= "$it%"
            materialDialog?.setProgress(it)
            if(it==100){

                materialDialog?.dismiss()
                
                if(!sharedPreferences.getBoolean("isbackupmnemonic",false)&&!sharedPreferences.getBoolean("isbackup",false)){
                    WarningBackupMnemonic()
                }
            }



        })

        walletViewModel?.balanceLiveData()?.observe(this, Observer {
            Logger.e("------balanceLiveData----->$it")

            balance.setFormat(configuration.format)
            balance.setAmount(it)

            for (output in walletViewModel?.walletLoadLiveData?.value!!.getUnspents()) {
                Logger.e("-------balanceLiveData-getUnspents----->$output")
            }



        })


        walletViewModel?.transactionsLiveData()?.observe(this, Observer { it ->

            Logger.e("-----transactionsLiveData----->${it.size}")
            lqrAdapterForRecyclerView.clearData()
            if(it.size==0){
//                lqrAdapterForRecyclerView.addLastItem("empty")
            }else{
                var items= buildListItems(this,ArrayList(it),walletViewModel?.walletLoadLiveData?.value)

                var results=items.sortedByDescending { it.comPtime }
                lqrAdapterForRecyclerView.addNewData(results)
            }

        })

        lqrAdapterForRecyclerView=WalletMainAdapter(this, ArrayList<Any>() as MutableList<Any?>?,R.layout.item_transform)
        list_transform.adapter=lqrAdapterForRecyclerView


        copy.setOnClickListener {
            var address=walletViewModel?.walletLoadLiveData?.value?.currentReceiveAddress()
            RxClipboardTool.copyText(this,address.toString())
            Toast(this).postToast(R.string.str_copy_address)
        }

        receiver.setOnClickListener {
            startActivity(Intent(this,ReceiverCmcActivity::class.java))
        }

        send.setOnClickListener {

            startActivity(Intent(this,SendCmcActivity::class.java))
        }

        initData()


    }

    fun initData(){
        materialDialog=showLoadingDialog()
        walletViewModel?.walletLoadLiveData?.loadWallet()

    }

    fun buildListItems(context: Context, transactions: List<Transaction>,
                       wallet: Wallet?
    ): List<TransactionItem> {
        val items = java.util.ArrayList<TransactionItem>(transactions.size + 1)
        for (tx in transactions)
            items.add(TransactionItem(context, tx, wallet,configuration.format))
        return items
    }


    override fun menu(): Int {
        return R.menu.menu_trust
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.net)?.isVisible = BuildConfig.DEBUG
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.backupwallet ->{startActivity(Intent(this,BackupMnemonicActivity::class.java))}
                R.id.importmnemonic -> {startActivity(Intent(this,ImportMnemonicActivity::class.java))}
                R.id.net -> startActivity(Intent(this,TestNetActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }



    fun showLoadingDialog():MaterialDialog{
      return  MaterialDialog.Builder(this)
                .title(R.string.str_asyning1)
                .progress(false,100,true)
                .cancelable(false)
                .positiveText(R.string.str_ok)
                .onPositive { dialog, which ->finish() }
                .showListener {
                    Logger.e("-----showListener---->")

                }.show()

    }


    fun WarningBackupMnemonic(){
       MaterialDialog.Builder(this)
                        .title(R.string.str_backup)
                        .content(Html.fromHtml(resources.getString(R.string.help_safety)))
                        .positiveText(R.string.str_iknow)
                        .positiveColor(resources.getColor(R.color.colorPrimary))
                        
                        .checkBoxPromptRes(R.string.str_isShowAgain, false, null)
                        .onAny { dialog, which ->
                            run {
                                if(dialog.isPromptCheckBoxChecked){
                                    sharedPreferences.edit().putBoolean("isbackupmnemonic",true).apply()
                                }
                            }
                        }
               .show()
    }


    @SuppressLint("ResourceAsColor")
    fun initToolbar(){

        toolbar.titleMarginStart=UIUtils.dip2Px(105)
//        toolbar.setTitleTextColor(R.color.colorPrimary)
//       var title= findViewById<TextView>(R.id.title)
//        title.visibility=View.VISIBLE
//        title.text=getString(R.string.str_wallet)
        toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))


    }


}

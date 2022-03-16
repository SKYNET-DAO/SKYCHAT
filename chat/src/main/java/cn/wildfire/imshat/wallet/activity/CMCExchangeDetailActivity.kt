package cn.wildfire.imshat.wallet.activity

import android.text.Spanned
import android.text.SpannedString
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager

import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfire.imshat.wallet.services.WalletService
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import com.android.base.BaseApp
import com.android.wallet.constants.Configuration
import com.android.wallet.constants.Constants
import com.android.wallet.utils.Formats
import com.android.wallet.utils.StringUtil
import com.android.wallet.utils.Toast
import com.android.wallet.utils.WalletUtils
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxClipboardTool
import kotlinx.android.synthetic.main.activity_detail_cmc.*
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionConfidence
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.store.SPVBlockStore
import org.bitcoinj.utils.Fiat
import org.bitcoinj.utils.MonetaryFormat
import org.bitcoinj.wallet.Wallet
import java.io.File


class CMCExchangeDetailActivity : WfcBaseActivity() {
    var txhash:Sha256Hash?=null
    var addressSpanned: Spanned?=null
    lateinit var walletViewModel:WalletViewModel
    var coin:Coin?=null

    lateinit var configuration: Configuration
    override fun contentLayout()= R.layout.activity_detail_cmc

    override fun afterViews() {
        configuration=Configuration(PreferenceManager.getDefaultSharedPreferences(this),resources)
        txhash= intent.extras.get("txid") as Sha256Hash?
        Logger.e("-------txhash-tx--->$txhash")
        walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)
        walletViewModel.walletLoadLiveData.observe(this, Observer {
            Logger.e("-----walletLoadLiveData-CMCExchangeDetailActivity---->")
            var tx= it.getTransaction(txhash)
            val sent = tx?.getValue(it)!!.signum() < 0
            val self = WalletUtils.isEntirelySelf(tx, it)
            val confidence = tx.confidence
            val purpose = tx.purpose
            val memo = Formats.sanitizeMemo(tx.memo)


            //amount
            initAmount(sent,tx,it,purpose)
            //address
            initAddress(sent,tx,it,self)
            //time
            initTime(tx)
            //confirm number
            initConfirm(it,tx)
            initBlock(tx)
            initCopy(self)



        })

        walletViewModel.walletLoadLiveData.loadWallet()

        address_copy.setOnClickListener {
            RxClipboardTool.copyText(this,sender_address.text)
            Toast(this).postToast(R.string.str_copy_address)
        }

        exid_copy.setOnClickListener {
            RxClipboardTool.copyText(this,exchange_id.text)
            Toast(this).postToast(R.string.str_copy_address_coin)
        }

    }

    fun initCopy(self:Boolean){
        if(self){
            address_copy.visibility=View.GONE
        }else{
            address_copy.visibility=View.VISIBLE
        }
    }


    fun initBlock(tx:Transaction?){

        confirm_number.text=tx?.confidence?.depthInBlocks.toString()

    }



    fun initConfirm(wallet: Wallet,tx:Transaction){

        Logger.e("-----initConfirm--->"+tx.toString())


    }

    fun initTime(tx:Transaction){
        // time
        val time = tx.updateTime
        var timeStr = if (true)
            DateUtils.formatDateTime(this, time.time,
                    DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
        else
            DateUtils.getRelativeTimeSpanString(this, time.time)

        exchange_time.text=timeStr;

    }


    var fee: Coin? = null
    var feeFormat: MonetaryFormat? = null
    var valueFormat: MonetaryFormat? = null
    var value: Coin?=null

    fun initAmount(sent:Boolean,tx:Transaction,wallet: Wallet,purpose: Transaction.Purpose){
        var value=tx.getValue(wallet)
        // fee
        val fee = tx.fee
        val showFee = fee != null && !fee.isZero
        this.feeFormat =configuration.format
        this.fee = if (showFee) fee!!.negate() else null

        // value
        this.valueFormat = configuration.format
        if (purpose == Transaction.Purpose.RAISE_FEE) {
            this.value = fee!!.negate()
        } else if (value.isZero) {
            this.value = null
        } else {
            this.value = if (showFee) value.add(fee!!) else value
        }




        //fee
        if(fee==null){
            net_fee.setAmount(Coin.ZERO)
        }else{
            net_fee.setAmount(fee)
        }

        net_fee.setFormat(feeFormat)

        //amount
        amount.setAlwaysSigned(true)
        amount.setAmount(value)
        amount.setFormat(valueFormat)





        //exchange_id
        exchange_id.text=tx.txId.toString()




    }


    fun initAddress(sent: Boolean,tx:Transaction,it:Wallet,self:Boolean){
        val purpose = tx.purpose
        val memo = Formats.sanitizeMemo(tx.memo)
        val address = if (sent)
            WalletUtils.getToAddressOfSent(tx, it)
        else
            WalletUtils.getWalletAddressOfReceived(tx, it)
        if (tx.isCoinBase) {
            this.addressSpanned = SpannedString
                    .valueOf("Minned")
        } else if (purpose == Transaction.Purpose.RAISE_FEE) {
            this.addressSpanned = null
        } else if (purpose == Transaction.Purpose.KEY_ROTATION || self) {
            this.addressSpanned = SpannedString.valueOf(getString(R.string.symbol_internal) + " "
                    + getString(R.string.wallet_transactions_fragment_internal))
        } else if (memo != null && memo.size >= 2) {
            this.addressSpanned = SpannedString.valueOf(memo[1])
        } else if (address != null) {
            val address1 = WalletUtils.formatAddress(address, address.toString().length,
                    Constants.ADDRESS_FORMAT_LINE_SIZE)
            this.addressSpanned = address1

        } else {
            this.addressSpanned = SpannedString.valueOf("?")
        }

        sender_address.text=this.addressSpanned.toString();


    }


}

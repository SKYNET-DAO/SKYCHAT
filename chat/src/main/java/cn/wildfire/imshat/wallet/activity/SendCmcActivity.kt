package cn.wildfire.imshat.wallet.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.kit.qrcode.QRCodeActivity
import cn.wildfire.imshat.kit.qrcode.ScanQRCodeActivity
import cn.wildfire.imshat.kit.third.utils.UIUtils
import com.android.base.utils.ACacheUtil
import cn.wildfire.imshat.wallet.BitUtil
import cn.wildfire.imshat.wallet.bean.AddressAndLabel
import cn.wildfire.imshat.wallet.data.SendCoinViewModel
import cn.wildfire.imshat.wallet.services.WalletService

import cn.wildfire.imshat.wallet.utils.AESUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.BuildConfig
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.MaterialDialog
import com.android.wallet.bean.TransactionEx
import com.android.wallet.constants.Configuration
import com.android.wallet.constants.Constants
import com.android.wallet.dialog.DialogBuilder
import com.android.wallet.offline.DirectPaymentTask
import com.android.wallet.send.*
import com.android.wallet.utils.Bluetooth
import com.android.wallet.utils.WalletUtils
import com.king.zxing.Intents
import com.orhanobut.logger.Logger
import de.schildbach.wallet.integration.android.BitcoinIntegration
import kotlinx.android.synthetic.main.activity_transaction.*
import org.bitcoin.protocols.payments.Protos
import org.bitcoinj.core.*
import org.bitcoinj.protocols.payments.PaymentProtocol
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import java.lang.Exception
import org.bouncycastle.crypto.params.KeyParameter
import java.util.*

class SendCmcActivity : WfcBaseActivity() {
    lateinit var walletViewModel: WalletViewModel
    lateinit var sendCoinViewModel: SendCoinViewModel
    var receivingAddressListener:ReceivingAddressListener?=null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    lateinit var configuration:Configuration
    private var amountCalculatorLink: CurrencyCalculatorLink? = null
    lateinit var broadcastManager:LocalBroadcastManager
    var  REQUEST_CODE_SCAN_QR_CODE=0x01


    private val handler = Handler()
    override fun contentLayout(): Int = R.layout.activity_transaction


    override fun afterViews() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        configuration=Configuration(PreferenceManager.getDefaultSharedPreferences(this),resources)
        backgroundThread = HandlerThread("backgroundThread", Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread!!.start()
        backgroundHandler= Handler(backgroundThread!!.looper)

        receivingAddressListener=ReceivingAddressListener()
        title = getString(R.string.str_out)
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel::class.java)
        sendCoinViewModel = ViewModelProviders.of(this).get(SendCoinViewModel::class.java)
        initView()
        onEventListener()
        initData()


    }


    fun initData(){
        walletViewModel.walletLoadLiveData.loadWallet()
    }

    fun initView(){
        var currencyAmountView=findViewById<CurrencyAmountView>(R.id.send_coins_amount_cmc)
        var localAmountView=findViewById<CurrencyAmountView>(R.id.send_coins_amount_local)

        //left cmc amount
        currencyAmountView.setCurrencySymbol(configuration.format.code())
        currencyAmountView.setInputFormat(configuration.maxPrecisionFormat)
        currencyAmountView.setHintFormat(configuration.format)
        //right local
        localAmountView.setInputFormat(Constants.LOCAL_FORMAT)
        localAmountView.setHintFormat(Constants.LOCAL_FORMAT)

        //init amountCalculatorLink
        amountCalculatorLink = CurrencyCalculatorLink(currencyAmountView, localAmountView)
        amountCalculatorLink?.exchangeDirection = configuration.lastExchangeDirection
    }

    fun onEventListener() {

        findViewById<ImageView>(R.id.qrcode).setOnClickListener {
            //  startActivityForResult(Intent(this,ScanActivity::class.java),0x11)
            startActivityForResult(Intent(this, ScanQRCodeActivity::class.java), 0x11)

        }

        send_coin.setOnClickListener {

            validateReceivingAddress()
            MaterialDialog.Builder(this)
                    .content(getString(R.string.str_transform_coforim))
                    .negativeText(getString(R.string.str_cancel))
                    .positiveText(getString(R.string.str_ok))
                    .cancelable(false)
                    .onPositive { dialog, which ->
                        run {

                            if (everythingPlausible())
                                handleGo()

                        }
                    }
                    .show()
        }

        adress_coin.onFocusChangeListener = receivingAddressListener
        adress_coin.addTextChangedListener(receivingAddressListener)

        sendCoinViewModel.dynamicFeeLiveData().observe(this, Observer<Map<FeeCategory, Coin>> {
//            updateView()
            handler.post(dryrunRunnable)
        })

        walletViewModel.walletLoadLiveData.observe(this, Observer {
            Logger.e("--------walletLoadLiveData-observe-->${it.toString()}")
        })

    }


    @SuppressLint("CheckResult")
    fun SendCoin2Address() {


    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            0x11 -> {
                if (data != null) {
                    val result = data.getStringExtra(Intents.Scan.RESULT)
                    
                    if (!result.isNullOrEmpty() && BitUtil.isBTCValidAddress(result)) {
                        adress_coin.setText(result)
                        adress_coin.isEnabled = false
                    } else {
                        Toast.makeText(this, getString(R.string.str_invalid_address), Toast.LENGTH_SHORT).show()
                    }

                }

            }
            REQUEST_CODE_SCAN_QR_CODE ->{
                if(data!=null){
                    val result = data.getStringExtra(Intents.Scan.RESULT).trim()
                      adress_coin.setText(result)
                }
            }


        }

    }





    private fun validateReceivingAddress() {
        try {
            val addressStr = adress_coin.text.toString().trim()
            if (!addressStr.isEmpty()) {
                val address = Address.fromString(Constants.NETWORK_PARAMETERS, addressStr)
                sendCoinViewModel.validatedAddress = AddressAndLabel(Constants.NETWORK_PARAMETERS, address.toString(),
                        null)
//                adress_coin.text = null
                Logger.i("Locked to valid address:${sendCoinViewModel.validatedAddress}")
            }
        } catch (x: AddressFormatException) {
            // swallow
            sendCoinViewModel.validatedAddress=null
            Toast.makeText(this,R.string.str_select_unvalid_address,Toast.LENGTH_SHORT).show()

        }

    }


     inner class ReceivingAddressListener : View.OnFocusChangeListener, TextWatcher{
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (!hasFocus) {

                updateView()
                validateReceivingAddress()

            }
        }

        override fun afterTextChanged(s: Editable) {
            val constraint = s.toString().trim()
            if (!constraint.isEmpty()){
                validateReceivingAddress()

            }

            updateView()

        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }


    private fun handleGo() {

        var wallet=walletViewModel.walletLoadLiveData.value
        if (wallet?.isEncrypted!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                object : DeriveKeyTask(backgroundHandler, WalletUtils.scryptIterationsTarget(this)) {

                     @RequiresApi(Build.VERSION_CODES.KITKAT)
                     override fun onSuccess(encryptionKey: KeyParameter, wasChanged: Boolean) {
                        if (wasChanged)
                            WalletUtils.autoBackupWallet(this@SendCmcActivity, wallet)
                        signAndSendPayment(encryptionKey)
                    }
                }.deriveKey(wallet, "")
            }
            setState(SendCoinViewModel.State.DECRYPTING)
        } else {
            signAndSendPayment(null)
        }
    }


    @SuppressLint("StringFormatInvalid")
    private fun signAndSendPayment(encryptionKey: KeyParameter?) {
        setState(SendCoinViewModel.State.SIGNING)
        // final payment intent
        val finalPaymentIntent = sendCoinViewModel.paymentIntent?.mergeWithEditedValues(
                amountCalculatorLink?.amount,
                if (sendCoinViewModel.validatedAddress != null) sendCoinViewModel.validatedAddress?.address else null)
        val finalAmount = finalPaymentIntent?.amount

        // prepare send request
        val fees = sendCoinViewModel.dynamicFeeLiveData().value
        val wallet = walletViewModel.walletLoadLiveData.value
        val sendRequest = finalPaymentIntent?.toSendRequest()
        sendRequest?.emptyWallet = sendCoinViewModel.paymentIntent!!.mayEditAmount() && finalAmount == wallet?.getBalance(Wallet.BalanceType.AVAILABLE)
        sendRequest?.feePerKb = fees?.get(sendCoinViewModel.feeCategory)
        sendRequest?.memo = sendCoinViewModel.paymentIntent?.memo
        sendRequest?.exchangeRate = amountCalculatorLink?.exchangeRate
        sendRequest?.aesKey = encryptionKey

        val fee = sendCoinViewModel.dryrunTransaction!!.fee
        if (fee.isGreaterThan(finalAmount)) {
            setState(SendCoinViewModel.State.INPUT)

            val btcFormat = configuration.format
            val dialog = DialogBuilder.warn(this,
                    R.string.send_coins_fragment_significant_fee_title)
            dialog.setMessage(getString(R.string.send_coins_fragment_significant_fee_message, btcFormat.format(fee),
                    btcFormat.format(finalAmount)))
            dialog.setPositiveButton(R.string.send_coins_fragment_button_send, DialogInterface.OnClickListener { dialog, which -> sendPayment(sendRequest!!, finalAmount!!) })
            dialog.setNegativeButton(R.string.button_cancel, null)
            dialog.show()
        } else {
            sendPayment(sendRequest!!, finalAmount!!)
        }
    }



     private fun sendPayment(sendRequest: SendRequest, finalAmount: Coin) {
        val wallet =walletViewModel.walletLoadLiveData.value

        object: SendCoinsOfflineTask(wallet, backgroundHandler) {
         override fun onSuccess(transaction: Transaction) {
                sendCoinViewModel.sentTransaction = transaction
                setState(SendCoinViewModel.State.SENDING)
               
                sendCoinViewModel!!.sentTransaction!!.confidence.addEventListener(sentTransactionConfidenceListener)
               
                wallet!!.freshAddress(KeyChain.KeyPurpose.REFUND)
                
             //   walletViewModel.walletLoadLiveData.walletAppKit.peerGroup().broadcastTransaction(transaction)
             WalletService.broadcastTransaction(this@SendCmcActivity, transaction)
             finish()
        }

        private fun directPay(payment: Protos.Payment) {
            val callback = object: DirectPaymentTask.ResultCallback {
             override fun onResult(ack:Boolean) {
            sendCoinViewModel.directPaymentAck = ack

            if (sendCoinViewModel.state === SendCoinViewModel.State.SENDING)
               setState(SendCoinViewModel.State.SENT)
    //            updateView()
                }

                override fun onFail(messageResId:Int, vararg messageArgs:Any) {
                val dialog = DialogBuilder.warn(this@SendCmcActivity,
                R.string.send_coins_fragment_direct_payment_failed_title)
                dialog.setMessage(
                        sendCoinViewModel.paymentIntent!!.paymentUrl + "\n" + getString(messageResId, *messageArgs)
                        + "\n\n" + getString(R.string.send_coins_fragment_direct_payment_failed_msg))
                dialog.setPositiveButton(R.string.button_retry, DialogInterface.OnClickListener { dialog, which -> directPay(payment) })
                dialog.setNegativeButton(R.string.button_dismiss, null)
                dialog.show()
                }
                }

                if (sendCoinViewModel.paymentIntent!!.isHttpPaymentUrl)
                {
                DirectPaymentTask.HttpPaymentTask(backgroundHandler, callback,
                        sendCoinViewModel.paymentIntent!!.paymentUrl, WalletUtils.httpUserAgent(this@SendCmcActivity)).send(payment)
                }

        }

            
         @SuppressLint("StringFormatInvalid")
         override fun onInsufficientMoney(missing: Coin) {
            setState(SendCoinViewModel.State.INPUT)
            val estimated = wallet?.getBalance(Wallet.BalanceType.ESTIMATED)
            val available = wallet?.getBalance(Wallet.BalanceType.AVAILABLE)
            val pending = estimated?.subtract(available)
            val btcFormat = configuration.format
            val dialog = DialogBuilder.warn(this@SendCmcActivity,
            R.string.send_coins_fragment_insufficient_money_title)
            val msg = StringBuilder()
            msg.append(getString(R.string.send_coins_fragment_insufficient_money_msg1, btcFormat.format(missing)))
            if (pending?.signum()!! > 0)
            msg.append("\n\n")
            .append(getString(R.string.send_coins_fragment_pending, btcFormat.format(pending)))
            if (sendCoinViewModel.paymentIntent!!.mayEditAmount())
            msg.append("\n\n").append(getString(R.string.send_coins_fragment_insufficient_money_msg2))
            dialog.setMessage(msg)
            if (sendCoinViewModel.paymentIntent!!.mayEditAmount())
            {
            dialog.setPositiveButton(R.string.send_coins_options_empty, DialogInterface.OnClickListener { dialog, which -> handleEmpty() })
            dialog.setNegativeButton(R.string.button_cancel, null)
            }
            else
            {
            dialog.setNeutralButton(R.string.button_dismiss, null)
            }
            dialog.show()
        }

            
         override fun onInvalidEncryptionKey() {
            setState(SendCoinViewModel.State.INPUT)

            }

            
         override fun onEmptyWalletFailed() {
            setState(SendCoinViewModel.State.INPUT)
            val dialog = DialogBuilder.warn(this@SendCmcActivity,
            R.string.send_coins_fragment_empty_wallet_failed_title)
            dialog.setMessage(R.string.send_coins_fragment_hint_empty_wallet_failed)
            dialog.setNeutralButton(R.string.button_dismiss, null)
            dialog.show()
        }
            
      override fun onFailure(exception:Exception) {
            setState(SendCoinViewModel.State.FAILED)
            val dialog = DialogBuilder.warn(this@SendCmcActivity, R.string.send_coins_error_msg)
            dialog.setMessage(exception.toString())
            dialog.setNeutralButton(R.string.button_dismiss, null)
            dialog.show() }

    }.sendCoinsOffline(sendRequest) // send asynchronously

  }



    private fun setState(state: SendCoinViewModel.State?) {
        sendCoinViewModel.state = state

    }


    private fun handleEmpty() {
        val available = walletViewModel.balanceLiveData().value
        amountCalculatorLink?.setBtcAmount(available)
        handler.post(dryrunRunnable)
    }


    private val dryrunRunnable = object : Runnable {
        override fun run() {
            if (sendCoinViewModel.state == SendCoinViewModel.State.INPUT)
                executeDryrun()
                updateView()

        }

        private fun executeDryrun() {
            var wallet=walletViewModel.walletLoadLiveData.value
            sendCoinViewModel.dryrunTransaction = null
            sendCoinViewModel.dryrunException = null

            val fees = sendCoinViewModel.dynamicFeeLiveData()?.value
            val amount = amountCalculatorLink!!.amount
            if (amount != null && fees != null) {
                try {
                    val dummy = wallet?.currentReceiveAddress() // won't be used, tx is never
                    // committed
                    val sendRequest = sendCoinViewModel.paymentIntent!!.mergeWithEditedValues(amount, dummy)
                            .toSendRequest()
                    sendRequest.signInputs = false
                    var editamount= sendCoinViewModel?.paymentIntent!!.mayEditAmount()
                    var mAmount= amount == wallet?.getBalance(Wallet.BalanceType.AVAILABLE)
                    Logger.e("-----editamount  mAmount----->$editamount  $mAmount")
                    sendRequest.emptyWallet = editamount&& mAmount
                    sendRequest.feePerKb = fees!!.get(sendCoinViewModel.feeCategory)
                    wallet?.completeTx(sendRequest)
                    sendCoinViewModel.dryrunTransaction = sendRequest.tx
                } catch (x: Exception) {
                    sendCoinViewModel.dryrunException = x


                }

            }
        }
    }


    private val sentTransactionConfidenceListener = TransactionConfidence.Listener { confidence, reason ->
        this.runOnUiThread {

            val confidence = sendCoinViewModel.sentTransaction!!.confidence
            val confidenceType = confidence.confidenceType
            val numBroadcastPeers = confidence.numBroadcastPeers()
            if (sendCoinViewModel.state === SendCoinViewModel.State.SENDING) {
                if (confidenceType == TransactionConfidence.ConfidenceType.DEAD) {
                    setState(SendCoinViewModel.State.FAILED)
                } else if (numBroadcastPeers > 1 || confidenceType == TransactionConfidence.ConfidenceType.BUILDING) {
                    setState(SendCoinViewModel.State.SENT)
                    // Auto-close the dialog after a short delay
                    if (configuration.sendCoinsAutoclose) {
                        handler.postDelayed({ this.finish() }, 500)
                    }
                }
            }

            if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS && confidenceType == TransactionConfidence.ConfidenceType.PENDING) {
                // play sound effect
                val soundResId = resources.getIdentifier("send_coins_broadcast_$numBroadcastPeers",
                        "raw", this.getPackageName())
                if (soundResId > 0)
                    RingtoneManager
                            .getRingtone(this, Uri.parse(
                                    "android.resource://" + this.getPackageName() + "/" + soundResId))
                            .play()
            }

//            updateView()
        }
    }



    fun updateView(){
        if( sendCoinViewModel.dryrunException!=null){

            send_coin.isEnabled=false
            send_coin.background=resources.getDrawable(R.drawable.shap_round_solid_textview_gray)

            when(sendCoinViewModel.dryrunException){

                is Wallet.DustySendRequested ->{
                    tip_balance.text=getString(R.string.send_coins_fragment_hint_dusty_send)
                }

                is InsufficientMoneyException -> {
                    tip_balance.text=getString(R.string.send_coins_fragment_hint_insufficient_money,
                            configuration.format.format((sendCoinViewModel.dryrunException as InsufficientMoneyException).missing!!))
                }

                is Wallet.CouldNotAdjustDownwards ->  tip_balance.text=getString(R.string.send_coins_fragment_hint_empty_wallet_failed)

                else ->    tip_balance.text=sendCoinViewModel.dryrunException.toString()

            }

        }else{

            if(sendCoinViewModel.validatedAddress!=null&&sendCoinViewModel.dryrunTransaction!=null){
                send_coin.isEnabled=true
                send_coin.background=resources.getDrawable(R.drawable.shap_round_solid_textview1)
            }else{
                send_coin.isEnabled=false
                send_coin.background=resources.getDrawable(R.drawable.shap_round_solid_textview_gray)
            }


        }

    }


    override fun onResume() {
        super.onResume()
        updateStateFrom(PaymentIntent.blank())
        amountCalculatorLink?.setListener(amountsListener)

        handler.post(dryrunRunnable)

    }


    private val amountsListener = object : CurrencyAmountView.Listener {
       override fun changed() {
           handler.post(dryrunRunnable)

        }

        override fun focusChanged(hasFocus: Boolean) {}
    }


    private fun isPayeePlausible(): Boolean {
        if (sendCoinViewModel.paymentIntent!!.hasOutputs())
            return true

        return sendCoinViewModel.validatedAddress != null
    }

    private fun isAmountPlausible(): Boolean {
        return if (sendCoinViewModel.dryrunTransaction != null)
            sendCoinViewModel.dryrunException == null
        else if (sendCoinViewModel.paymentIntent!!.mayEditAmount())
            amountCalculatorLink!!.hasAmount()
        else
            sendCoinViewModel.paymentIntent!!.hasAmount()
        return true
    }


    private fun everythingPlausible(): Boolean {
        return (sendCoinViewModel.state == SendCoinViewModel.State.INPUT&&isPayeePlausible() && isAmountPlausible())
    }

    private fun updateStateFrom(paymentIntent: PaymentIntent) {
        Logger.i("got {}", paymentIntent)

        sendCoinViewModel.paymentIntent = paymentIntent

//        sendCoinViewModel.validatedAddress = null
//        sendCoinViewModel.directPaymentAck = null

        // delay these actions until fragment is resumed
        handler.post {
            setState(SendCoinViewModel.State.INPUT)
            amountCalculatorLink?.setBtcAmount(paymentIntent.amount)
//                requestFocusFirst()
//                updateView()
            handler.post(dryrunRunnable)
        }
    }



    override fun menu(): Int {
        return R.menu.menu_send_coin
    }







    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.scan -> startActivityForResult(Intent(this,ScanQRCodeActivity::class.java),REQUEST_CODE_SCAN_QR_CODE)
        }


        return super.onOptionsItemSelected(item)
    }






}
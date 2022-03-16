package cn.wildfire.imshat.wallet.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.wildfire.imshat.kit.ChatManagerHolder
import cn.wildfire.imshat.kit.GlideApp
import cn.wildfire.imshat.kit.WfcBaseActivity
import com.android.base.utils.ACacheUtil
import cn.wildfire.imshat.wallet.BitUtil
import cn.wildfire.imshat.wallet.Kit
import cn.wildfire.imshat.wallet.WalletManager
import cn.wildfire.imshat.wallet.services.WalletService
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.MaterialDialog
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_mnemonic.*
import kotlinx.android.synthetic.main.activity_net_test.*
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.wallet.UnreadableWalletException

class TestNetActivity : WfcBaseActivity() {
    var sharedPreferences: SharedPreferences? = null
    lateinit var walletViewModel:WalletViewModel

    override fun contentLayout(): Int = R.layout.activity_net_test


    override fun afterViews() {
        walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)
        walletViewModel.walletLoadLiveData.observe(this, Observer {
            netpeer.text=it.networkParameters.id
            lastblockheight.text=it.lastBlockSeenHeight.toString()
            lasthash.text=it.lastBlockSeenHash.toString()
        })

        walletViewModel.walletLoadLiveData.loadWallet()


    }




}




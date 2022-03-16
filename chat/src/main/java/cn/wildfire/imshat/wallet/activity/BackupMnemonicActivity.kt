package cn.wildfire.imshat.wallet.activity

import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.wallet.BitUtil
import cn.wildfire.imshat.wallet.WalletManager
import cn.wildfire.imshat.wallet.utils.SaveImgUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import kotlinx.android.synthetic.main.activity_mnemonic.*
import java.lang.StringBuilder
import java.util.logging.Logger

class BackupMnemonicActivity : WfcBaseActivity() {

    var walletViewModel:WalletViewModel?=null

    override fun contentLayout(): Int=R.layout.activity_mnemonic

    override fun menu(): Int {
        return R.menu.mnemonic_next
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.next ->{

             startActivity(Intent(this@BackupMnemonicActivity, BackupMnemonicVertifyActivity::class.java).apply {
                 putExtra("words",words.text)
             })
                finish()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun afterViews() {
        walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)

        walletViewModel?.walletLoadLiveData?.observe(this, Observer {
            BitUtil.getSeedWordsFromWallet(it).toString()?.apply {
                com.orhanobut.logger.Logger.e("----getSeedWordsFromWallet----->$this")
                words.text=  substring(1,this.length-1)
            }
        })
        walletViewModel?.walletLoadLiveData?.loadWallet()
        title=getString(R.string.str_backup_words)

    }



}

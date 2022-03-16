package cn.wildfire.imshat.kit.conversation

import android.content.Intent
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.lifecycle.Observer

import com.afollestad.materialdialogs.MaterialDialog
import com.orhanobut.logger.Logger

import java.util.ArrayList

import androidx.lifecycle.ViewModelProviders

import org.bitcoinj.core.Coin
import org.bitcoinj.wallet.Wallet

import cn.wildfire.imshat.kit.contact.model.UIUserInfo
import cn.wildfire.imshat.kit.contact.pick.PickConversationTargetActivity
import cn.wildfire.imshat.kit.group.GroupViewModel
import cn.wildfire.imshat.kit.third.utils.UIUtils
import cn.wildfire.imshat.wallet.BitUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.model.GroupInfo

class CreateConversationActivity : PickConversationTargetActivity() {
    private var groupViewModel: GroupViewModel? = null
    private var walletViewModel: WalletViewModel? = null
    private var mWallet: Wallet? = null
    override fun afterViews() {
        super.afterViews()
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel::class.java)
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel::class.java)
        walletViewModel!!.walletLoadLiveData.observe(this, Observer {

            this.mWallet = it
        })
        walletViewModel!!.walletLoadLiveData.loadWallet()

        //modify by yzr
        title = getString(R.string.str_create_conversation)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onContactPicked(initialCheckedUserInfos: List<UIUserInfo>, newlyCheckedUserInfos: List<UIUserInfo>) {
        if (initialCheckedUserInfos.isEmpty() && newlyCheckedUserInfos.size == 1) {//<=1 nothing

            //            Intent intent = new Intent(this, ConversationActivity.class);
            //            Conversation conversation = new Conversation(Conversation.ConversationType.Single, newlyCheckedUserInfos.get(0).getUserInfo().uid);
            //            intent.putExtra("conversation", conversation);
            //            startActivity(intent);
            //            finish();
        } else {
            //pop pay dialog
            val payDialog = MaterialDialog.Builder(this)
                    .title(UIUtils.getString(R.string.str_create_group) + "(" + newlyCheckedUserInfos.size + ")")
                    .content(UIUtils.getString(R.string.str_cmc_100))
                    .cancelable(false)
                    .positiveText(getString(R.string.str_create))
                    .negativeText(getString(R.string.str_cancel))
                    .onPositive { d, w ->

                        //                        var balancecoin=wallet.getBalance( Wallet.BalanceType.AVAILABLE)
                        //                        var balance=formatFWalletAmount(balancecoin)

                        val balancecoin = mWallet!!.getBalance(Wallet.BalanceType.AVAILABLE)

                        val balance = BitUtil.formatFWalletAmount(balancecoin)

                        Logger.e("-------balance---->$balance")
                        if(balance.trim().toDouble()>=1.00) {

                            createDemoGroup(initialCheckedUserInfos, newlyCheckedUserInfos)
                        } else {
                            Toast.makeText(this, R.string.str_balance_not_enough, Toast.LENGTH_SHORT).show()
                        }

                    }
                    .build()
            payDialog.show()

            //
        }

    }


    fun createDemoGroup(initialCheckedUserInfos: List<UIUserInfo>, newlyCheckedUserInfos: List<UIUserInfo>) {

        val dialog = MaterialDialog.Builder(this)
                .content(this.getString(R.string.str_createing))
                .progress(true, 100)
                .cancelable(false)
                .build()
        dialog.show()

        val userInfos = ArrayList<UIUserInfo>()
        userInfos.addAll(initialCheckedUserInfos)
        userInfos.addAll(newlyCheckedUserInfos)
        groupViewModel!!.createGroup(this, userInfos).observe(this, Observer {

            dialog.dismiss()
            if (it.isSuccess) {
                UIUtils.showToast(UIUtils.getString(R.string.create_group_success))
                val intent = Intent(this@CreateConversationActivity, ConversationActivity::class.java)
                val conversation = Conversation(Conversation.ConversationType.Group, it.getResult(), 0)
                intent.putExtra("conversation", conversation)
                startActivity(intent)
            } else {
                UIUtils.showToast(UIUtils.getString(R.string.create_group_fail))
            }
            finish()
        })
    }

    override fun onGroupPicked(groupInfos: List<GroupInfo>) {
        val intent = Intent(this, ConversationActivity::class.java)
        val conversation = Conversation(Conversation.ConversationType.Group, groupInfos[0].target)
        intent.putExtra("conversation", conversation)
        startActivity(intent)
        finish()
    }
}

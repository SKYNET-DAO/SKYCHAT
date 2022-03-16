package cn.wildfire.imshat.wallet.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import cn.wildfire.imshat.kit.WfcBaseActivity
import com.android.base.utils.ACacheUtil
import cn.wildfire.imshat.wallet.bean.SelectStringBean
import cn.wildfirechat.imshat.R
import com.lqr.adapter.LQRAdapterForRecyclerView
import com.lqr.adapter.LQRViewHolderForRecyclerView
import kotlinx.android.synthetic.main.activity_mnemonic_verify.*

class BackupMnemonicVertifyActivity : WfcBaseActivity() {

    var words1Adapter: LQRAdapterForRecyclerView<SelectStringBean>? = null
    var wordsAdapter: LQRAdapterForRecyclerView<String>? = null
    var intentwords: MutableList<String>? = null
    lateinit var sharedPreferences:SharedPreferences


    override fun contentLayout(): Int {
        return R.layout.activity_mnemonic_verify
    }

    override fun menu(): Int {
        return R.menu.mnemonic_ok
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ok -> {

                if (wordsAdapter?.itemCount == intentwords?.size) {

                    if (intentwords.toString().trim() == wordsAdapter?.data.toString().trim()) {
                        Toast.makeText(this, getString(R.string.str_backup_success), Toast.LENGTH_SHORT).show()
                        sharedPreferences.edit().putBoolean("isbackup",true).apply()
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.str_backup_fail), Toast.LENGTH_SHORT).show()
                        clearData()
                    }

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun afterViews() {
        sharedPreferences=getSharedPreferences("config", Context.MODE_PRIVATE)
        title=getString(R.string.str_backup_words)
        intentwords = intent?.extras?.getString("words")?.split(",") as MutableList<String>?
        var words = mutableListOf<SelectStringBean>()
        intentwords?.forEach {
            words.add(SelectStringBean()?.apply { content = it;ispress = false })
        }


        words1Adapter = object : LQRAdapterForRecyclerView<SelectStringBean>(this, words?.shuffled(), R.layout.item_wods) {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun convert(helper: LQRViewHolderForRecyclerView?, item: SelectStringBean?, position: Int) {

                helper?.getView<TextView>(R.id.itemword)?.text = item?.content



                if (item?.ispress!!) {
                    helper?.getView<LinearLayout>(R.id.root)?.setBackgroundColor(getColor(R.color.color_b5b5b5))
                } else {
                    helper?.getView<LinearLayout>(R.id.root)?.setBackgroundColor(getColor(R.color.color_ffffff))

                }

                helper?.convertView?.setOnClickListener {
                    if (!item?.ispress!!) {
                        item.ispress = true
                        wordsAdapter?.addLastItem(item?.content)
                        helper.getView<LinearLayout>(R.id.root)?.setBackgroundColor(getColor(R.color.color_b5b5b5))
                    }
                }


            }

        }

        wordslist1.adapter = words1Adapter

        //第一个recycleview
        wordsAdapter = object : LQRAdapterForRecyclerView<String>(this, mutableListOf(), R.layout.item_wods_show) {
            override fun convert(helper: LQRViewHolderForRecyclerView?, item: String?, position: Int) {
                helper?.getView<TextView>(R.id.itemword)?.text = item

            }

        }
        wordslist.adapter = wordsAdapter

        clearAll.setOnClickListener { clearData() }


    }


    override fun onBackPressed() {

    }


    fun clearData(){
        wordsAdapter?.clearData()
        words1Adapter?.data?.forEach {
            it.ispress = false
        }
        words1Adapter?.notifyDataSetChanged()
    }


}

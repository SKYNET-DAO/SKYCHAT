package cn.wildfire.imshat.discovery.download

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Html
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView

import com.afollestad.materialdialogs.MaterialDialog
import com.android.ipfsclient.IPFSManager
import com.lqr.adapter.LQRAdapterForRecyclerView
import com.lqr.adapter.LQRViewHolderForRecyclerView
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxFileTool

import java.io.File
import java.util.ArrayList

import butterknife.Bind
import cn.wildfire.imshat.app.Config
import cn.wildfire.imshat.discovery.download.adapter.PlugListAdapter
import cn.wildfire.imshat.discovery.download.adapter.VDownloadListAdapter
import cn.wildfire.imshat.kit.GlideApp
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.net.AppConst
import cn.wildfire.imshat.net.api.ImRetrofitService
import cn.wildfire.imshat.net.helper.rxjavahelper.RxObserver
import cn.wildfire.imshat.net.model.base.bean.ResponseData
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfire.imshat.wallet.viewmodel.IPFSViewModel
import cn.wildfirechat.imshat.BuildConfig
import cn.wildfirechat.imshat.R


class PlugActivity : WfcBaseActivity() {

    internal var urls: List<String> = mutableListOf()
    internal var titles: List<String> = ArrayList()
    internal var ipfsModels: List<IPFSModel> = ArrayList()
    private val lqrAdapterForRecyclerView: LQRAdapterForRecyclerView<IPFSModel>? = null
    val REQUETCODE=0x15
//        var pluglist="/api/test/plugin/getPluginList";//test network
     val pluglist = "/api/plugin/getPluginList"//main network

     var ipfsViewModel: IPFSViewModel?=null
    internal var recyclerView: RecyclerView? = null
     var adapter: VDownloadListAdapter? = null
    var sharedPreferences: SharedPreferences?=null


    override fun contentLayout(): Int {
        return R.layout.activity_plug
    }

    override fun afterViews() {
        recyclerView=findViewById(R.id.exchangedatas)
        sharedPreferences=getSharedPreferences("config", Context.MODE_PRIVATE)
        ipfsViewModel = ViewModelProviders.of(this).get(IPFSViewModel::class.java)
        
         adapter = VDownloadListAdapter(this, ArrayList())

        ipfsViewModel?.onPlugList()?.observe(this, Observer {
            Logger.e("-----onPlugList------>" + JsonUtil.toJson(it)!!)
            adapter?.addMoreData(it)
        })


        ipfsViewModel?.progress()?.observe(this, Observer {
            showLoading(it!!)
        })
        recyclerView?.adapter=adapter

        var istip=sharedPreferences?.getBoolean("isjiaoshuitip",false)
        if(!istip!!){
            WarningJiaoshuiTip()
        }

        initData()

    }


    fun initData(){
        var url = ""
        //        if(BuildConfig.DEBUG){
        //            url=AppConst.BASE_URL +testpluglist;
        //        }else{
        url = AppConst.BASE_URL + pluglist
        Logger.e("-------procpluglist----->$url")
        //        }

        ipfsViewModel?.getPluglist(url)
    }


    fun WarningJiaoshuiTip(){
        MaterialDialog.Builder(this)
                .title(R.string.str_tip)
                .content(Html.fromHtml(resources.getString(R.string.help_jiaoshui_tip)))
                .positiveText(R.string.str_iknow)
                .positiveColor(resources.getColor(R.color.colorPrimary))
                
                .checkBoxPromptRes(R.string.str_isShowAgain, false, null)
                .onAny { dialog, which ->
                    run {
                        if(dialog.isPromptCheckBoxChecked){
                            sharedPreferences?.edit()?.putBoolean("isjiaoshuitip",true)?.apply()
                        }
                    }
                }
                .show()
    }


    override fun menu(): Int {
        return R.menu.menu_download_all_option
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {


        when(item?.itemId){
            R.id.sky_installed ->{startActivityForResult(Intent(this,PlugTaskListActivity::class.java),REQUETCODE)}
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==REQUETCODE){
            adapter?.notifyDataSetChanged()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }



}

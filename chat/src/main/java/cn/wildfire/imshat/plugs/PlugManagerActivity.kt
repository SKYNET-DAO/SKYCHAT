package cn.wildfire.imshat.plugs

import android.content.Context
import android.content.SharedPreferences
import android.text.Html
import android.text.TextUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.plugs.adapter.ChannelAdapter
import cn.wildfire.imshat.plugs.beans.Channel
import cn.wildfire.imshat.plugs.beans.Channel.TYPE_MY_CHANNEL
import cn.wildfire.imshat.plugs.constants.Constant
import cn.wildfire.imshat.plugs.listener.ItemDragHelperCallBack
import cn.wildfire.imshat.plugs.listener.OnChannelDragListener
import cn.wildfire.imshat.plugs.listener.OnChannelListener
import cn.wildfire.imshat.plugs.listener.handlerChannelText
import cn.wildfire.imshat.plugs.utils.PreUtils
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfire.imshat.wallet.viewmodel.IPFSViewModel
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_channel.*
import java.util.ArrayList

class PlugManagerActivity:WfcBaseActivity(), OnChannelDragListener {



    val pluglist = "/api/plugin/getPluginList"//main network

    var ipfsViewModel: IPFSViewModel?=null
    var sharedPreferences: SharedPreferences?=null
    private var mSelectedChannels = ArrayList<Channel>()
    private var mUnSelectedChannels = ArrayList<Channel>()

    private val mDatas = ArrayList<Channel>()
    private var mAdapter: ChannelAdapter? = null

    private var mHelper: ItemTouchHelper? = null

    private var mOnChannelListener: OnChannelListener? = null

    fun setOnChannelListener(onChannelListener: OnChannelListener, mSelectedChannels: MutableList<Channel>, mUnSelectedChannels: MutableList<Channel>) {
        mOnChannelListener = onChannelListener
        this.mSelectedChannels= mSelectedChannels as ArrayList<Channel>
        this.mUnSelectedChannels= mUnSelectedChannels as ArrayList<Channel>
    }


    override fun contentLayout()= R.layout.activity_channel


    override fun afterViews() {

        sharedPreferences=getSharedPreferences("config", Context.MODE_PRIVATE)
        ipfsViewModel = ViewModelProviders.of(this).get(IPFSViewModel::class.java)

        ipfsViewModel?.onPlugList()?.observe(this, Observer {
            Logger.e("-----onPlugList------>" + JsonUtil.toJson(it)!!)

        })


        ipfsViewModel?.progress()?.observe(this, Observer {
            showLoading(it!!)
        })

        handlerChannelText(this)

        var istip=sharedPreferences?.getBoolean("isjiaoshuitip",false)
        if(!istip!!){
            WarningJiaoshuiTip()
        }


        initData()

    }


    fun initData(){

//        var url = AppConst.BASE_URL + pluglist
//        Logger.e("-------procpluglist----->$url")
//        //        }
//
//        ipfsViewModel?.getPluglist(url)


        processLogic()
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


    fun processLogic(){
        mDatas.add(Channel(Channel.TYPE_MY, getString(R.string.str_my_channel), ""))
        val bundle = intent.getBundleExtra("CHANNEL")
        val selectedDatas = bundle.getSerializable(Constant.DATA_SELECTED) as List<Channel>
        val unselectedDatas = bundle.getSerializable(Constant.DATA_UNSELECTED) as List<Channel>
//       val selectedDatas=mSelectedChannels
//        val unselectedDatas=mUnSelectedChannels
        setDataType(selectedDatas, TYPE_MY_CHANNEL)
        setDataType(unselectedDatas, Channel.TYPE_OTHER_CHANNEL)

        mDatas.apply {
            addAll(selectedDatas)
           // add(Channel(Channel.TYPE_OTHER, getString(R.string.str_channel_tj), ""))
            addAll(unselectedDatas)
        }



        mAdapter = ChannelAdapter(mDatas)
        val manager = GridLayoutManager(this, 4)
        recyclerView.layoutManager = manager
        recyclerView.adapter = mAdapter
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemViewType = mAdapter?.getItemViewType(position)
                return if (itemViewType == TYPE_MY_CHANNEL || itemViewType == Channel.TYPE_OTHER_CHANNEL) 1 else 4
            }
        }
        val callBack = ItemDragHelperCallBack(this)
        mHelper = ItemTouchHelper(callBack)
        mAdapter?.setOnChannelDragListener(this)
        //attachRecyclerView
        mHelper?.attachToRecyclerView(recyclerView)


    }

     private fun setDataType(datas: List<Channel>, type: Int) {
        for (i in datas.indices) {
            datas[i].setItemType(type)
        }
    }



    override fun onStarDrag(baseViewHolder: BaseViewHolder?) {
        mHelper?.startDrag(baseViewHolder!!)
    }

    override fun onItemMove(starPos: Int, endPos: Int) {

        //        if (starPos < 0||endPos<0) return;
        
        if (mOnChannelListener != null)
            mOnChannelListener?.onItemMove(starPos - 1, endPos - 1)
             onMove(starPos, endPos)
    }

    override fun onMoveToMyChannel(starPos: Int, endPos: Int) {

        
        onMove(starPos, endPos)

        if (mOnChannelListener != null)
            mOnChannelListener!!.onMoveToMyChannel(starPos - 1 - mAdapter!!.getMyChannelSize(), endPos - 1)
    }

    override fun onMoveToOtherChannel(starPos: Int, endPos: Int) {
        
        onMove(starPos, endPos)
        if (mOnChannelListener != null)
            mOnChannelListener!!.onMoveToOtherChannel(starPos - 1, endPos - 2 - mAdapter!!.getMyChannelSize())
    }

    private fun onMove(starPos: Int, endPos: Int) {
        val startChannel = mDatas[starPos]
        
        mDatas.removeAt(starPos)
        
        mDatas.add(endPos, startChannel)
        mAdapter?.notifyItemMoved(starPos, endPos)




    }


    override fun onDestroy() {

        
        PreUtils.putString(Constant.SELECTED_CHANNEL_JSON, Gson().toJson(mSelectedChannels))
        PreUtils.putString(Constant.UNSELECTED_CHANNEL_JSON, Gson().toJson(mUnSelectedChannels))
        super.onDestroy()
    }


}
//package cn.wildfire.imshat.wallet.activity
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.widget.TextView
//
//
//import com.lqr.adapter.LQRAdapterForRecyclerView
//import com.lqr.adapter.LQRViewHolderForRecyclerView
//
//import org.bitcoinj.core.Transaction
//
//import java.util.ArrayList
//
//import cn.wildfire.imshat.kit.WfcBaseActivity
//import cn.wildfire.imshat.wallet.BitUtil
//import cn.wildfire.imshat.wallet.JsonUtil
//import cn.wildfire.imshat.wallet.WalletManager
//import cn.wildfire.imshat.wallet.bean.TransactionBean
//import cn.wildfire.imshat.wallet.itemdecoration.RecyclerViewItemDecoration1
//import cn.wildfire.imshat.wallet.utils.TimeUtils
//import cn.wildfirechat.imshat.R
//import com.orhanobut.logger.Logger
//import io.reactivex.Flowable
//import io.reactivex.android.schedulers.AndroidSchedulers
//import kotlinx.android.synthetic.main.activity_exchange_history.*
//import org.bitcoinj.params.MainNetParams
//
//class ExchangeHistoryActivity : WfcBaseActivity() {
//
//    private var adapter: LQRAdapterForRecyclerView<Transaction>? = null
//
//    override fun contentLayout(): Int {
//        return R.layout.activity_exchange_history
//    }
//
//    override fun afterViews() {
//        title = getString(R.string.str_account)
//        initRecycleView()
//    }
//
//    private fun initRecycleView() {
//        adapter = object : LQRAdapterForRecyclerView<Transaction>(this, ArrayList(), R.layout.item_exchange_history) {
//            override fun convert(helper: LQRViewHolderForRecyclerView, item: Transaction, position: Int) {
//
//
//
//
//               var typecount= item.getValue(WalletManager.getInstance().wallet).toPlainString()
//                Logger.e("---------typecount--------->$typecount}")
//
//                var timestamp= TimeUtils.getTimeForDetail2(item?.updateTime?.time.toString(),TimeUtils.FORMAT_TYPE_1,false)
//
//                if(!typecount.contains("-")){
//                    helper?.getView<TextView>(R.id.direction)?.apply {
//                        text=context.getString(R.string.str_in)
//                        setTextColor(resources.getColor(R.color.color_e88272))
//
//                    }
//
//                    helper?.getView<TextView>(R.id.left_icon)?.setBackgroundColor(resources.getColor(R.color.color_e88272))
//                    helper?.getView<TextView>(R.id.count)?.apply {
//
//                        setTextColor(resources.getColor(R.color.color_e88272))
//                        text=typecount+" "+"CMC"
//
//                    }
//
//                }else{
//
//                    helper?.getView<TextView>(R.id.direction)?.apply {
//                        text=context.getString(R.string.str_output)
//                        setTextColor(resources.getColor(R.color.color_50aa58))
//                    }
//
//                    helper?.getView<TextView>(R.id.left_icon)?.setBackgroundColor(resources.getColor(R.color.color_50aa58))
//
//                    helper?.getView<TextView>(R.id.count)?.apply {
//                        setTextColor(resources.getColor(R.color.color_50aa58))
//                        var outcoin= item.getValue(WalletManager.getInstance().wallet).toPlainString()+" "+"CMC"
//                        Logger.e("-----outcoin----->$outcoin")
//                        text=outcoin
//
//                    }
//                }
//
//                helper?.getView<TextView>(R.id.exchanexchange_timege_time)?.text=timestamp
//
//                helper?.convertView?.setOnClickListener {
//
//                    var transactionbean= makeTransationBean(item!!)
//                    var intent= Intent(this@ExchangeHistoryActivity,CMCExchangeDetailActivity::class.java)
//                        intent.putExtra("transation",transactionbean)
//                         startActivity(intent)
//                }
//            }
//        }.apply {
//
//           var itemdecoration= RecyclerViewItemDecoration1.Builder(this@ExchangeHistoryActivity)
////                    .mode(RecyclerViewItemDecoration1.MODE_HORIZONTAL)
//                    // .dashWidth(8)
//                    //  .dashGap(5)
//                    .thickness(1)
//                    //.drawableID(R.color.line3)
//                    .create()
//          //  exchangedatas.addItemDecoration(itemdecoration)
//            exchangedatas.adapter=this
//        }
//
//
//        initData()
//
//    }
//
//
//
//    @SuppressLint("CheckResult")
//    fun initData(){
//        Flowable.just("")
//                .observeOn(io.reactivex.schedulers.Schedulers.io())
//                .map {WalletManager.getInstance().wallet.getTransactions(false)}
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    adapter?.addNewData(it?.toList()?.sortedByDescending { it.updateTime})
//
//                }
//    }
//
//    fun makeTransationBean(item:Transaction):TransactionBean{
//
//
//        var ins= mutableListOf<String>().apply { item.inputs.forEach {
//            if(it.scriptSig.chunks.size>0){
//
//                var pubkey= it.scriptSig.chunks[1]?.toString()?.apply {
//                    subSequence(indexOf("[")+1,indexOf("]")) }
//                var outaddress=  BitUtil.getAddressFromPubkey(pubkey!!)
//                this.add(outaddress)
//                
//            }
//        } }.apply {
//            Logger.d("-------insize------->$size")
//            forEach {  Logger.d("---------arrays---------->$it") }
//
//
//        }
//
//        var outs=mutableListOf<String>().apply { item.outputs.forEach {
//            this.add(it.scriptPubKey.getToAddress(MainNetParams.get(),true).toString())
//        } }.apply {
//            Logger.d("-------outsize------->$size")
//            forEach {  Logger.d("---------arrays---------->$it") }
//        }
//
//
//        var toplainfree=if(item.fee!=null)item.fee.toPlainString() else "0.00"
//
//
//
//
//
//
//        
//
//        return TransactionBean()
//                .setIns(ins)
//                .setOuts(outs)
//                .setTxid(item.txId.toString())
//                .setUpdateTime(item?.updateTime.time)
//                .setAmount(item.getValue(WalletManager.getInstance().wallet).toPlainString())
//                 .setFree(toplainfree)
//
//
//
//
//
//
//    }
//
//}

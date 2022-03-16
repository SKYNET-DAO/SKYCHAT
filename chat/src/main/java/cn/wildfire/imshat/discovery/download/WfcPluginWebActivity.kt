package cn.wildfire.imshat.discovery.download

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Html
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.android.ipfsclient.IPFSManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxFileTool

import java.io.File

import butterknife.Bind
import cn.wildfire.imshat.app.MyApp
import cn.wildfire.imshat.discovery.util.PluginUtils
import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.net.AppConst
import cn.wildfirechat.imshat.R
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import cn.wildfire.imshat.discovery.download.Constants.PLUGIN_DIR
import cn.wildfire.imshat.kit.user.ChangeMyNameActivity
import cn.wildfire.imshat.kit.widget.ProgressWebView
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.lzy.okgo.utils.HttpUtils
import com.lzy.okgo.utils.HttpUtils.runOnUiThread
import kotlinx.android.synthetic.main.activity_webview.*
import org.bitcoinj.wallet.Wallet

class WfcPluginWebActivity : WfcBaseActivity() {


    private var ipfsModel: IPFSModel? = null
    var indexhtmlpath:String?=null
    var webview: ProgressWebView?=null
    var walletViewModel:WalletViewModel?=null
    var wallet:Wallet?=null
    var sharedPreferences:SharedPreferences?=null


    override fun contentLayout(): Int {
        return R.layout.activity_webview
    }

    override fun afterViews() {
        sharedPreferences=getSharedPreferences("config",Context.MODE_PRIVATE)
        webview=findViewById(R.id.webview)
        walletViewModel=ViewModelProviders.of(this).get(WalletViewModel::class.java)

        walletViewModel?.walletLoadLiveData?.observe(this, Observer {
            wallet=it
        })
        ipfsModel = intent.getSerializableExtra("model") as IPFSModel


             var plugContent=RxFileTool.listFilesInDir(Constants.PLUGIN_DIR+"/"+ipfsModel?.sign)

             plugContent.forEach {
                 if(it.name=="index.html")indexhtmlpath=it.path
             }




        if (TextUtils.isEmpty(ipfsModel!!.title)) {
            webview!!.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    val title = view.title
                    if (!TextUtils.isEmpty(title)) {
                        setTitle(title)
                    }
                }
            }
        } else {
            title = ipfsModel!!.title
        }

        walletViewModel?.walletLoadLiveData?.loadWallet()
        loadIndexPage(indexhtmlpath)



    }

    private fun loadIndexPage(path: String?) {
        webview!!.loadUrl(File(path).toURI().toString())
        webview!!.addJavascriptInterface(this, "Android")

    }

    companion object {
        fun loadUrl(context: Context, model: IPFSModel) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("model", model)
            context.startActivity(intent)
        }
    }



    @JavascriptInterface
    fun  loadJsMethod() {
        var data = JsonUtil.toJson(wallet?.currentReceiveAddress())
        Logger.e("-------wallet---->$data")
        runOnUiThread {
            webview?.loadUrl("javascript:getReciverCoinAddress('" + data + "')");

        }

        fun isAuth(): Int {
            return sharedPreferences?.getInt("auth", 0)!!
        }


        @JavascriptInterface
        fun Auth(msg: String) {

            if (isAuth() == 1) {
                loadJsMethod()
            } else {
                val dialog = MaterialDialog.Builder(this)
                        .title(R.string.str_agree_app)
                        .content("Request perssion from Skychat")
                        .positiveText(R.string.str_agree_app)
                        .negativeText(R.string.str_noagree_app)
                        .cancelable(false)
                        .onPositive { d, w ->
                            loadJsMethod()
                        }
                        .onNegative { d, w -> finish() }
                        .build()
                dialog.show()
            }

        }

    }






}

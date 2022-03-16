package cn.wildfire.imshat.discovery.download

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.webkit.*
import android.widget.LinearLayout
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import cn.wildfire.imshat.discovery.util.AndroidInterface

import com.just.agentweb.AgentWeb
import com.just.agentweb.NestedScrollAgentWebView
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxFileTool

import org.bitcoinj.wallet.Wallet

import java.io.File

import cn.wildfire.imshat.kit.WfcBaseActivity
import cn.wildfire.imshat.kit.utils.Path.FileUtils
import cn.wildfire.imshat.wallet.JsonUtil
import cn.wildfire.imshat.wallet.viewmodel.WalletViewModel
import cn.wildfirechat.imshat.R
import com.afollestad.materialdialogs.MaterialDialog
import com.android.base.LanguageUtil
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.WebViewClient
import io.ipfs.api.IPFS
import kotlinx.android.synthetic.main.activity_net_test.*
import java.net.URI

class WebActivity : WfcBaseActivity() {
    private var mAgentWeb: AgentWeb? = null
    private var webLayout: LinearLayout? = null
    private var ipfsModel: IPFSModel? = null
    private var indexhtmlpath: String? = null
    private var walletViewModel: WalletViewModel? = null
    private var wallet: Wallet? = null
    private var sharedPreferences: SharedPreferences? = null


    override fun contentLayout(): Int {
        return R.layout.activity_webview1
    }

    override fun afterViews() {
        ipfsModel = intent.getSerializableExtra("model") as IPFSModel
        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE)
        walletViewModel = ViewModelProviders.of(this).get(WalletViewModel::class.java)

        webLayout = findViewById(R.id.lin_web)
        val webView = NestedScrollAgentWebView(this)
//         setting(webView)
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE,  null)
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(webLayout!!, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)
                .setWebView(webView)
                .setWebViewClient(object : WebViewClient(){})
                .createAgentWeb()
                .go("")


        walletViewModel?.walletLoadLiveData?.observe(this, Observer {
            this.wallet=it
            initData()
        })

        walletViewModel?.walletLoadLiveData?.loadWallet()


    }

    private fun initData() {


        val files = RxFileTool.listFilesInDir(Constants.PLUGIN_DIR + "/" + ipfsModel!!.sign)

        for (item in files) {
            Logger.e("-------file----->" + item.name)
            if (item.name == "index.html") indexhtmlpath = item.path
        }

        Logger.e("-------indexhtmlpath--->$indexhtmlpath")
        loadIndexPage(indexhtmlpath)
    }


    private fun loadIndexPage(path: String?) {


        mAgentWeb?.urlLoader?.loadUrl( File(path).toURI().toString())
//        mAgentWeb?.jsInterfaceHolder?.addJavaObject("Android",this)
        mAgentWeb?.jsInterfaceHolder?.addJavaObject("Android", AndroidInterface(mAgentWeb!!, this,this.wallet!!))



//

    }


    fun setting(webView: WebView?) {
        webView?.settings?.apply {
            
            javaScriptEnabled = true



            
            useWideViewPort = true 

            
            setSupportZoom(true) 
            builtInZoomControls = true 
            displayZoomControls = false 

            
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK 
            allowFileAccess = true 
            loadsImagesAutomatically = true 
            defaultTextEncodingName = "utf-8" 
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(true)

            
            setGeolocationEnabled(true)
            saveFormData = true

            
            setNeedInitialFocus(true)
            
            loadWithOverviewMode = true
            
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

            
            setSupportMultipleWindows(false)
            
            javaScriptCanOpenWindowsAutomatically = false
            
            allowContentAccess = true 
            allowFileAccess = true 
            
            allowFileAccessFromFileURLs = true
            
            allowUniversalAccessFromFileURLs = true

            
            loadsImagesAutomatically = true 
//            blockNetworkImage = App.getSp(SettingConstants.IMG, false)

            blockNetworkLoads = false 


            
            setSupportZoom(true) 
            builtInZoomControls = false 
            displayZoomControls = true 

            
            defaultTextEncodingName = "UTF-8"
            defaultFontSize = 16 
            defaultFixedFontSize = 16 
            minimumFontSize = 8 
            minimumLogicalFontSize = 8 
            textZoom = 100 

            
            standardFontFamily = "sans-serif" 
            serifFontFamily = "serif" 
            sansSerifFontFamily = "sans-serif" 
            fixedFontFamily = "monospace" 
            cursiveFontFamily = "cursive" 
            fantasyFontFamily = "fantasy" 

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { 
                mediaPlaybackRequiresUserGesture = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { 
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { 
                offscreenPreRaster = false
            }
        }
    }

    companion object {
        fun loadUrl(context: Context, model: IPFSModel) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("model", model)
            context.startActivity(intent)
        }
    }


    override fun onPause() {
        mAgentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }


    override fun onResume() {
        mAgentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }


    override fun onDestroy() {
        mAgentWeb?.webLifeCycle?.onDestroy()
        super.onDestroy()
    }




}

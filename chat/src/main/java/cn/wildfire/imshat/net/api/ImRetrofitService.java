package cn.wildfire.imshat.net.api;

import android.content.Context;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okrx2.adapter.ObservableBody;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfire.imshat.app.MyApp;
import cn.wildfire.imshat.kit.utils.AppInfoUtils;
import cn.wildfire.imshat.kit.utils.ManifestUtil;
import cn.wildfire.imshat.net.AppConst;
import cn.wildfire.imshat.net.bean.AppInfoBean;
import cn.wildfire.imshat.net.bean.RandomIp;
import cn.wildfire.imshat.net.helper.JsonConvert;
import cn.wildfire.imshat.net.helper.LzyResponse;
import cn.wildfire.imshat.net.model.base.bean.ResponseData;
import cn.wildfire.imshat.net.model.base.bean.ResponseDataOther;
import cn.wildfire.imshat.net.model.pojo.BannerBean;
import cn.wildfire.imshat.net.model.pojo.HotKeyBean;
import cn.wildfire.imshat.net.model.pojo.UserBean;
import cn.wildfire.imshat.net.model.pojoVO.ArticleListVO;
import cn.wildfire.imshat.net.model.pojoVO.TypeTagVO;
import io.reactivex.Observable;
import cn.wildfire.imshat.discovery.download.IPFSModel;
import cn.wildfire.imshat.net.model.base.bean.ResponseData;






public class ImRetrofitService {


    public static String version = "/ipfs/appInfo";





   public static Observable<ResponseData<AppInfoBean>> requestAppUpdate(String url){
        //defined httpparams
       HttpParams httpParams=new HttpParams();
       String signal ="78:87:16:08:A2:F6:AC:08:3D:FA:BD:DE:F8:B7:F0:0A:DE:68:7A:F5";//fixed
       String version = ManifestUtil.getVersionName(MyApp.getContext());
       httpParams.put("sign",signal);
       httpParams.put("version",version);
       
      return   OkGo.<ResponseData<AppInfoBean>>
                post(url)
                .params(httpParams)
                .converter(new JsonConvert<ResponseData<AppInfoBean>>(){

                }).adapt(new ObservableBody<>());

   }


   public static Observable<ResponseData<RandomIp>> getIp(Context context,String baseurl){

//       Map<String,String> map1 = new HashMap<>();
//       map1.put("env","prod");
       return   OkGo.<ResponseData<RandomIp>>
               get(baseurl)
               .tag(context)
               .converter(new JsonConvert<ResponseData<RandomIp>>(){

               }).adapt(new ObservableBody<>());


   }


    public static Observable<ResponseData<List<IPFSModel>>> getInitPluginList(String url){
       return OkGo.<ResponseData<List<IPFSModel>>>post(url)
                .converter(new JsonConvert<ResponseData<List<IPFSModel>>>() {
                })
                .adapt(new ObservableBody<>());




    }





}

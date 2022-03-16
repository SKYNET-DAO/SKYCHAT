package cn.wildfire.imshat.wallet.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.wallet.viewmodel.BaseViewModel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.lzy.okrx2.adapter.ObservableBody;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.wildfire.imshat.discovery.download.IPFSModel;
import cn.wildfire.imshat.net.api.ImRetrofitService;
import cn.wildfire.imshat.net.helper.JsonConvert;
import cn.wildfire.imshat.net.helper.LzyResponse;
import cn.wildfire.imshat.net.model.base.bean.ResponseData;
import io.reactivex.Observable;

public class IPFSViewModel extends BaseViewModel {
    private Application application;
    MutableLiveData<List<IPFSModel>> pluginlist;
    public IPFSViewModel(@NonNull Application application) {
        super(application);
        this.application=application;
    }



    public MutableLiveData<List<IPFSModel>> onPlugList(){  //VerifyCodeBean
        if(pluginlist==null){
            pluginlist=new MutableLiveData<>();
        }
        return pluginlist;
    }

    @SuppressLint("CheckResult")
    public void getPluglist(String url){
        getProgress().postValue(true);
        setDisposable(ImRetrofitService.getInitPluginList(url).subscribe(this::onPlugListSuccess,this::onReqFail));
    }



    private void onPlugListSuccess(ResponseData<List<IPFSModel>> bean){
        if(bean!=null&&bean.getData()!=null){
            onPlugList().postValue(bean.getData());}
    }

    private void onReqFail(Throwable e){
        getProgress().postValue(false);
        onError(e);
    }


        public  void DownloadPlugin(String downloadurl){
            OkGo.<File>get(downloadurl)
                    .tag(this.application)
                    .execute(new FileCallback() {
                        @Override
                        public void onSuccess(Response<File> response) {
                            Logger.d("------download success------>" + response.body());
//                             downloadplugin.postValue(response.body());
                        }

                        @Override
                        public void onError(Response<File> response) {
                            
                            Toast.makeText(application, "Download exception", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void downloadProgress(Progress progress) {
                            
                            int value = (int) (100 * (progress.currentSize / progress.totalSize));

                        }
                        @Override
                        public void onStart(Request<File, ? extends Request> request) {
                           
                        }
                    });





    }





}

package cn.wildfire.imshat.app;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.mine_android.api.Api;
import com.android.wallet.viewmodel.BaseViewModel;

import java.util.Map;

import cn.wildfire.imshat.net.bean.RandomIp;
import cn.wildfire.imshat.net.helper.LzyResponse;

public class IpViewModel extends BaseViewModel {

    MutableLiveData<RandomIp> randomIp;
    public IpViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<RandomIp> onRandomIp(){
        if(randomIp==null){
            randomIp=new MutableLiveData<>();
        }
        return randomIp;
    }


    @SuppressLint("CheckResult")
    public void getRandom(String url){
        getProgress().postValue(true);
        setDisposable(Api.Companion.getRandomIp(url).subscribe(this::onRandomResult,this::onReqFail));

    }



    private void onRandomResult(LzyResponse<RandomIp> bean){
        getProgress().postValue(false);
        if(bean!=null&&bean.getData()!=null){
            onRandomIp().postValue(bean.getData());}
    }

    private void onReqFail(Throwable e){
        onError(e);
    }





}

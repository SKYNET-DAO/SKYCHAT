package cn.wildfire.imshat.discovery.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.android.ipfsclient.zip.ZipUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;
import com.vondear.rxtool.RxFileTool;

import java.io.File;
import java.io.IOException;

import cn.wildfire.imshat.discovery.download.Constants;
import cn.wildfire.imshat.discovery.download.WfcPluginWebActivity;
import cn.wildfire.imshat.discovery.listener.UnzipCallback;
import io.ipfs.api.IPFS;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PluginUtils {

    @SuppressLint("CheckResult")
    public static void saveAndUnzip(String pulginId, String srcPath, UnzipCallback unzipCallback){
        Flowable.just("")
                .observeOn(Schedulers.io())
                .map(s->{
                    ZipUtils.doUnTarGz(new File(srcPath),Constants.PLUGIN_DIR+"/"+pulginId);
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success->{
                    unzipCallback.zipSuccess();
                },error->{
                    unzipCallback.zipFail();
                });


    }









}

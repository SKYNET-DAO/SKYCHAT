package com.android.ipfsclient;

import android.annotation.SuppressLint;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class IPFSManager {
    private static IPFSManager instance;
    private IPFS ipfs;


    @SuppressLint("CheckResult")
    public void init(String address){
        Flowable.just(address)
                .observeOn(Schedulers.io())
                .map(s -> {
                    ipfs = new IPFS(s);
                    Logger.e("-----初始化成功map----->"+ipfs.toString());
                    return ipfs;
                })
                .subscribe(ipfs->{
                    ipfs.refs.local();
                },e->{
                    Logger.e("-----初始化失败---->"+e.getMessage());
                    ipfs=null;
                    init(address);

                });
    }

    public static IPFSManager getInstance(){
        synchronized (IPFSManager.class){
            if(instance==null)instance=new IPFSManager();
            return instance;
        }
    }



    public Flowable<byte[]> getIpfsByte(String mutihash){

        return  Flowable.just(mutihash)
                .observeOn(Schedulers.io())
                .map(s -> {
                        try{
                            Multihash filePointer = Multihash.fromBase58(s);
                            return ipfs.cat(filePointer);
                        }catch (IOException e){
                          return null;
                        }
                })
                .observeOn(AndroidSchedulers.mainThread());

    }

    public String catUrl(String hash){


        if(ipfs!=null)return ipfs.catUrl(hash);
        return "";


    }


    public boolean checkState(){
        if(this.ipfs==null)return false;
        return true;

    }


}

package cn.wildfire.imshat.net.model.base.bean;

import java.io.Serializable;


public class ResponseDataOther<T> extends ResponseData<T> implements Serializable {

    private String downloadurl;
    
    private String result;

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
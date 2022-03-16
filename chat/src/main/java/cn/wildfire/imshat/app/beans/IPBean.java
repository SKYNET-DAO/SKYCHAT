package cn.wildfire.imshat.app.beans;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class IPBean extends LitePalSupport implements Serializable {

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private String ip;

    public IPBean(String ip) {
        this.ip = ip;

    }


}

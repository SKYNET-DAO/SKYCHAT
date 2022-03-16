package cn.wildfire.imshat.net.model.base.bean;

import java.io.Serializable;

public class ResponseData<T> implements Serializable {

    private static final long serialVersionUID = 5213230387175987834L;

    private int code;
    private int status;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int errorCode) {
        this.code = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
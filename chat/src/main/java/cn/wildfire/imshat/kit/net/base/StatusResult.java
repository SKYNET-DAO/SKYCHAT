package cn.wildfire.imshat.kit.net.base;

/**
 * Created by imndx on 2017/12/16.
 */


public class StatusResult {
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}

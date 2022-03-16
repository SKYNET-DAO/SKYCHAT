package cn.wildfire.imshat.wallet.subscribe;



public class NullException extends NullPointerException {

    public String code;
    public String message;

    public NullException(String message, String code) {
        this.message=message;
        this.code = code;
    }
}

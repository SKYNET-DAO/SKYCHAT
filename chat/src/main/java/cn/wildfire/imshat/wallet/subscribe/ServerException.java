package cn.wildfire.imshat.wallet.subscribe;



public class ServerException extends RuntimeException {
    public String code;
    public String message;

    public ServerException(String message, String code) {
        this.message=message;
        this.code = code;
    }
}

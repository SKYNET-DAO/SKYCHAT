package cn.wildfire.imshat.wallet.subscribe;



public class ResponseThrowable extends Exception {
    public String ErrorCode;           
    public String ErrorMsg;
    public Throwable throwable;
    public int state;

    public String getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(String errorCode) {
        ErrorCode = errorCode;
    }

    public String getErrorMsg() {
        return ErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        ErrorMsg = errorMsg;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ResponseThrowable(String errorMsg) {
        ErrorMsg = errorMsg;
    }

    public ResponseThrowable(int errorcode) {
        ErrorCode = String.valueOf(errorcode);
    }

    public ResponseThrowable(Throwable throwable, String code) {
        super(throwable);
        this.throwable = throwable;
        this.ErrorCode = code;
    }

    @Override
    public String toString() {
        return "ResponseThrowable{" +
                "ErrorCode='" + ErrorCode + '\'' +
                ", ErrorMsg='" + ErrorMsg + '\'' +
                ", throwable=" + throwable +
                ", state=" + state +
                '}';
    }
}

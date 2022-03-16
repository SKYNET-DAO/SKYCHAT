package cn.wildfire.imshat.wallet.subscribe;

import com.google.gson.JsonParseException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;

import retrofit2.HttpException;


public class ExceptionHandle {
    
    public static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;
    private static String serviceState;

    public static ResponseThrowable handleException(Throwable e) {
        ResponseThrowable ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponseThrowable(e, ERROR.HTTP_ERROR);
            switch (httpException.code()) {
                case GATEWAY_TIMEOUT:
                    ex.ErrorMsg = "";
                    break;
                case UNAUTHORIZED:
                case FORBIDDEN:
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex.ErrorMsg = "Service exception";
                    break;
            }
            return ex;
        } else if (e instanceof ServerException) {

            ServerException resultException = (ServerException) e;
            ex = new ResponseThrowable(resultException, resultException.code);
            ex.ErrorMsg = resultException.message;
            return ex;
        }else if(e instanceof NullException){
            ex = new ResponseThrowable(e, ERROR.NULL_ERROR);
            ex.ErrorMsg = "NULL Pointer";
            return ex;
        }

        else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            ex = new ResponseThrowable(e, ERROR.PARSE_ERROR);
            ex.ErrorMsg = "Resolve exception";
            return ex;
        } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
            ex = new ResponseThrowable(e, ERROR.NETWORD_ERROR);
            
            ex.ErrorMsg = "";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponseThrowable(e, ERROR.SSL_ERROR);
            ex.ErrorMsg = "Authentication failed";
            return ex;
        } else if (e instanceof ConnectTimeoutException || e instanceof java.net.SocketTimeoutException) {
            ex = new ResponseThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.ErrorMsg ="Time out";
            return ex;
        } else {
            ex = new ResponseThrowable(e, ERROR.UNKNOWN);


            ex.ErrorMsg = "";
            return ex;
        }
    }

    
    public static class ERROR {
        
        public static final String UNKNOWN = "1000";
       
        public static final String PARSE_ERROR = "1001";
        
        public static final String NETWORD_ERROR = "1002";
       
        public static final String HTTP_ERROR = "1003";

        
        public static final String SSL_ERROR = "1004";

        
        public static final String TIMEOUT_ERROR = "1005";


       
        public static final String NULL_ERROR = "1006";
    }


}


package cn.wildfire.imshat.net.helper.interceptor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;



public class TokenInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // try the request
        Response originalResponse = chain.proceed(request);

        
        ResponseBody responseBody = originalResponse.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }
        String bodyString = buffer.clone().readString(charset);
        Log.d("body---------->", bodyString);

        /***************************************/

        JSONObject extrasJson = null;
        try {
            if (extrasJson == null){
                extrasJson = new JSONObject(bodyString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int code = Integer.parseInt(extrasJson.optString("errorCode")); 


        if (code == 401){  

            

            
            String refreshToken = "sssgr122222222";
           
            String newToken = "sssssssss  ne wtoken";

            // create a new request and modify it accordingly using the new token
            Request newRequest = request.newBuilder().header("token", newToken)
                    .build();

            // retry the request

            originalResponse.body().close();
            return chain.proceed(newRequest);
        }

        // otherwise just pass the original response on
        return originalResponse;
    }

}

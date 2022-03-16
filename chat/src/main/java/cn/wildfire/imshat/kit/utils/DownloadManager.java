package cn.wildfire.imshat.kit.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by heavyrainlee on 20/02/2018.
 */

public class DownloadManager {

    private static DownloadManager downloadManager;
    private final OkHttpClient okHttpClient;

    public static DownloadManager get() {
        if (downloadManager == null) {
            downloadManager = new DownloadManager();
        }
        return downloadManager;
    }

    private DownloadManager() {
        okHttpClient = new OkHttpClient();
    }

    public void download(final String url, final String saveDir, final OnDownloadListener listener) {
        download(url, saveDir, listener);
    }

    public void download(final String url, final String saveDir, String name, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                
                listener.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                
                String savePath = isExistDir(saveDir);
                String fileName = TextUtils.isEmpty(name) ? getNameFromUrl(url) : name;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, fileName);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        
                        listener.onProgress(progress);
                    }
                    fos.flush();
                    
                    listener.onSuccess(file);
                } catch (Exception e) {
                    File file = new File(savePath, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    listener.onFail();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

   
    private String isExistDir(String saveDir) throws IOException {
        
        File downloadFile = new File(saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        return downloadFile.getAbsolutePath();
    }

    
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        
        void onSuccess(File file);

       
        void onProgress(int progress);

       
        void onFail();
    }
}
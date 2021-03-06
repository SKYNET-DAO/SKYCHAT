package cn.wildfire.imshat.kit.preview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.JCameraListener;

import cn.wildfire.imshat.app.Config;
import cn.wildfirechat.imshat.R;

public class TakePhotoActivity extends AppCompatActivity {
    private JCameraView mJCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        initView();
    }

    private void initView() {
        mJCameraView = findViewById(R.id.cameraView);
        //(0.0.7+)
        mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath());
        //(0.0.8+)
        
        File file = new File(Config.Companion.getVIDEO_SAVE_DIR());
        if (!file.exists()) {
            file.mkdirs();
        }

        mJCameraView.setSaveVideoPath(Config.Companion.getVIDEO_SAVE_DIR());
        mJCameraView.setJCameraLisenter(new JCameraListener() {

            @Override
            public void captureSuccess(Bitmap bitmap) {
                
                String path = saveBitmap(bitmap, Config.Companion.getPHOTO_SAVE_DIR());
                Intent data = new Intent();
                data.putExtra("take_photo", true);
                data.putExtra("path", path);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                
                Intent data = new Intent();
                data.putExtra("take_photo", false);
                data.putExtra("path", url);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mJCameraView != null) {
            mJCameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mJCameraView != null) {
            mJCameraView.onPause();
        }
    }

    public String saveBitmap(Bitmap bm, String dir) {
        String path = "";
        File f = new File(dir, "wfc_" + SystemClock.currentThreadTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            path = f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
}

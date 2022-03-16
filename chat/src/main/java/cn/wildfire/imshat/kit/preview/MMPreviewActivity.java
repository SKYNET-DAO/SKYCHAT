package cn.wildfire.imshat.kit.preview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.lzy.okgo.OkGo;
import com.orhanobut.logger.Logger;
import com.vondear.rxtool.RxFileTool;
import com.vondear.rxtool.RxImageTool;


import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.wildfire.imshat.app.Config;
import cn.wildfire.imshat.kit.GlideApp;
import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.conversation.message.model.UiMessage;
import cn.wildfire.imshat.kit.third.utils.ImageUtils;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfire.imshat.kit.utils.DownloadManager;
import cn.wildfire.imshat.kit.utils.NavigatebarUtil;
import cn.wildfire.imshat.kit.widget.ViewPagerFixed;
import cn.wildfire.imshat.net.helper.rxjavahelper.RxObserver;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfire.imshat.wallet.utils.SaveImgUtil;
import cn.wildfirechat.imshat.R;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import rx.Observable;

/**
 * @author imndx
 */
public class MMPreviewActivity extends WfcBaseActivity {
    private SparseArray<View> views;
    private View currentVideoView;

    private static int currentPosition = -1;
    private static List<UiMessage> messages;
    private boolean pendingPreviewInitialMedia;
    private String localUrl="";

    private final PagerAdapter pagerAdapter = new PagerAdapter() {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {


            Logger.e("------instantiateItem--->");
            View view;
            UiMessage message = messages.get(position);
            if (message.message.content.getType() == MessageContentType.ContentType_Image) {
                view = LayoutInflater.from(MMPreviewActivity.this).inflate(R.layout.preview_photo, null);
            } else {
                view = LayoutInflater.from(MMPreviewActivity.this).inflate(R.layout.preview_video, null);
            }

            container.addView(view);
            views.put(position % 3, view);
            if (pendingPreviewInitialMedia) {
                preview(view, message);
            }
            return view;
        }



        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            // do nothing ?
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return messages == null ? 0 : messages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    };

    final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // TODO 
            Logger.e("-------onPageScrolled----->"+position+"  "+currentPosition);
        }

        @Override
        public void onPageSelected(int position) {
            View view = views.get(position % 3);
            if (view == null) {
                // pending layout
                return;
            }
            if (currentVideoView != null) {
                resetVideoView(currentVideoView);
                currentVideoView = null;
            }
            UiMessage message = messages.get(position);
            Logger.e("------onPageSelected------>"+position+"  "+ JsonUtil.toJson(message));
            currentPosition=position;
            preview(view, message);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void preview(View view, UiMessage message) {


        if (message.message.content.getType() == MessageContentType.ContentType_Image) {
            previewImage(view, message);

        } else {
            previewVideo(view, message);


        }
    }

    private void resetVideoView(View view) {
        PhotoView photoView = view.findViewById(R.id.photoView);
        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        ImageView playButton = view.findViewById(R.id.btnVideo);
        VideoView videoView = view.findViewById(R.id.videoView);
        photoView.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
        videoView.stopPlayback();
        videoView.setVisibility(View.INVISIBLE);
    }

    private void previewVideo(View view, UiMessage message) {



        VideoMessageContent content = (VideoMessageContent) message.message.content;
        PhotoView photoView = view.findViewById(R.id.photoView);
        photoView.setImageBitmap(content.getThumbnail());

        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);
        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.GONE);

        ImageView btn = view.findViewById(R.id.btnVideo);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setVisibility(View.GONE);
                if (TextUtils.isEmpty(content.localPath)) {
                    File videoFile = new File(Config.Companion.getVIDEO_SAVE_DIR(), message.message.messageUid + ".mp4");
                    if (!videoFile.exists()) {
                        view.setTag(message.message.messageUid + "");
                        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        final WeakReference<View> viewWeakReference = new WeakReference<>(view);
                        DownloadManager.get().download(content.remoteUrl, Config.Companion.getVIDEO_SAVE_DIR(), message.message.messageUid + ".mp4", new DownloadManager.OnDownloadListener() {
                            @Override
                            public void onSuccess(File file) {
                                UIUtils.postTaskSafely(() -> {
                                    View targetView = viewWeakReference.get();
                                    if (targetView != null && (message.message.messageUid + "").equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        playVideo(targetView, file.getAbsolutePath());
                                    }
                                });
                            }

                            @Override
                            public void onProgress(int progress) {
                                // TODO update progress
                                Log.e(MMPreviewActivity.class.getSimpleName(), "video downloading progress: " + progress);
                                loadingProgressBar.post(()->{loadingProgressBar.setProgress(progress);});

                            }

                            @Override
                            public void onFail() {
                                View targetView = viewWeakReference.get();
                                UIUtils.postTaskSafely(() -> {
                                    if (targetView != null && (message.message.messageUid + "").equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        targetView.findViewById(R.id.btnVideo).setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
                    } else {
                        playVideo(view, videoFile.getAbsolutePath());
                    }
                } else {
                    playVideo(view, content.localPath);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void playVideo(View view, String videoUrl) {

        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

        PhotoView photoView = view.findViewById(R.id.photoView);
        photoView.setVisibility(View.GONE);

        ImageView btn = view.findViewById(R.id.btnVideo);
        btn.setVisibility(View.GONE);

        MediaController controller=new MediaController(this);
        Logger.e("-------UIUtils.getNavBarHeight(this)---->"+UIUtils.getDisplayHeight());
        Logger.e("-------UIUtils.getNavBarHeight(this)---->"+UIUtils.getNavBarHeight(this));
        Logger.e("-------UIUtils.getNavBarHeight(this)---->"+controller.getMeasuredHeight());

        controller.setPadding(0,UIUtils.dip2Px(200),0,UIUtils.dip2Px(25));
        videoView.setMediaController(controller);

        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.GONE);
        view.findViewById(R.id.loading).setVisibility(View.GONE);
        currentVideoView = view;

        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoUrl);
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(MMPreviewActivity.this, "play error", Toast.LENGTH_SHORT).show();
            resetVideoView(view);
            return true;
        });
        videoView.setOnCompletionListener(mp -> resetVideoView(view));
        videoView.start();

    }

    private void previewImage(View view, UiMessage message) {
        Logger.e("-------previewImage---->");
        ImageMessageContent content = (ImageMessageContent) message.message.content;
        ProgressBar progressBar=view.findViewById(R.id.loading);
        String path = TextUtils.isEmpty(content.localPath) ? content.remoteUrl : content.localPath;
        PhotoView photoView = view.findViewById(R.id.photoView);
        photoView.setOnClickListener(v->{
        if(toolbar.getVisibility()==View.GONE){
            toolbar.setVisibility(View.VISIBLE);
            NavigatebarUtil.showNavKey(this,1);
        }else{
            toolbar.setVisibility(View.GONE);
            NavigatebarUtil.hideNavKey(this);
        }
        });
        GlideApp.with(MMPreviewActivity.this).load(path)
                .placeholder(new BitmapDrawable(getResources(), content.getThumbnail()))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Logger.e("--------onLoadFailed--->"+e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
               .into(photoView);


    }


    @Override
    protected int contentLayout() {
        return R.layout.activity_mm_preview;
    }


    @Override
    protected int menu() {
        return R.menu.file_preview;
    }

    @Override
    protected void afterMenus(Menu menu) {
       // saveMenu = menu.findItem(R.id.save);


    }

    @SuppressLint("CheckResult")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
          switch (item.getItemId()) {
              case R.id.save:
                    Logger.e("--------onOptionsItemSelected----->"+currentPosition);
                     UiMessage uiMessage=messages.get(currentPosition);
                   int type= uiMessage.message.content.getType();
                  if (type == MessageContentType.ContentType_Image) {

                      ImageMessageContent message = (ImageMessageContent) messages.get(currentPosition).message.content;
                      if (message != null) {

                          downloadUrl(message.remoteUrl)
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(bitmap -> {
                                      //save to gallery
                                      if(bitmap!=null){
                                          ImageUtils.saveImageToGallery(this,bitmap);
                                          Toast.makeText(this,R.string.str_save_success,Toast.LENGTH_SHORT).show();
                                      }

                                  });

                      }

                  }else if(type==MessageContentType.ContentType_Video){
                      downMp4(uiMessage);
                  }else{
                         Toast.makeText(MMPreviewActivity.this, getString(R.string.str_save_not_support), Toast.LENGTH_SHORT).show();
                  }

                  break;

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void afterViews() {

        views = new SparseArray<>(3);
        final ViewPagerFixed viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(pageChangeListener);
        if (currentPosition == 0) {
            viewPager.post(() -> pageChangeListener.onPageSelected(0));
        } else {
            viewPager.setCurrentItem(currentPosition);
            pendingPreviewInitialMedia = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messages = null;
    }

    public static void startActivity(Context context, List<UiMessage> messages, int current) {
        if (messages == null || messages.isEmpty()) {
            Log.w(MMPreviewActivity.class.getSimpleName(), "message is null or empty");
            return;
        }
        MMPreviewActivity.messages = messages;
        MMPreviewActivity.currentPosition = current;
        Intent intent = new Intent(context, MMPreviewActivity.class);
        context.startActivity(intent);
    }



    @SuppressLint("CheckResult")
    private Flowable<Bitmap> downloadUrl(String remoteurl){
       return Flowable.just(remoteurl)
                .observeOn(Schedulers.io())
                .map(s -> GlideApp.with(MMPreviewActivity.this)
                        .asBitmap()
                        .load(s)
                        .submit()
                        .get());
    }


    private void downMp4(UiMessage uiMessage) {
        final ProgressDialog pd; 
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //
        pd.setMessage(getString(R.string.str_loading));
        pd.setCanceledOnTouchOutside(false);
        VideoMessageContent content = (VideoMessageContent) uiMessage.message.content;
        File videoFile=new File(Config.Companion.getVIDEO_SAVE_DIR(),uiMessage.message.messageUid + ".mp4");
        if(!videoFile.exists()){
            pd.show();
            DownloadManager.get().download(content.remoteUrl, Config.Companion.getVIDEO_SAVE_DIR(), uiMessage.message.messageUid + ".mp4", new DownloadManager.OnDownloadListener() {
                @Override
                public void onSuccess(File file) {
                    Logger.e("-----onSuccess---->"+file.getPath());
                    pd.dismiss();
                    new com.android.wallet.utils.Toast(MMPreviewActivity.this).postToast(R.string.str_downloading_success);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));

                }

                @Override
                public void onProgress(int progress) {
                    // TODO update progress
                    Logger.e("-----onProgress---->"+progress);
                    pd.setProgress(progress);

                }

                @Override
                public void onFail() {
                    Logger.e("-----onFail---->");
                    pd.dismiss();
                    new com.android.wallet.utils.Toast(MMPreviewActivity.this).postToast(R.string.str_downloading_fail);

                }
            });
        }else{
            new com.android.wallet.utils.Toast(MMPreviewActivity.this).postToast(R.string.str_downloaded);

        }

    }



}

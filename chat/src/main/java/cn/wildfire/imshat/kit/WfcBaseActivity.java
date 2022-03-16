package cn.wildfire.imshat.kit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;

import org.simple.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.afollestad.materialdialogs.Theme;
import com.android.base.LanguageUtil;
import com.orhanobut.logger.Logger;

import cn.wildfire.imshat.wallet.activity.AppManager;
import cn.wildfirechat.imshat.R;
import kotlin.jvm.Volatile;

public abstract class WfcBaseActivity extends AppCompatActivity {

    private MaterialDialog dialog;
    @Bind(R.id.toolbar)
    public Toolbar toolbar;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        beforeViews();
        setContentView(contentLayout());
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (showHomeMenuItem()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        afterViews();
        AppManager.getInstance().addActivity(this);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        Context newContext = LanguageUtil.initAppLanguage(newBase);
        super.attachBaseContext(newContext);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu() != 0) {
            getMenuInflater().inflate(menu(), menu);
        }
        afterMenus(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            hideInputMethod();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

   
    protected abstract @LayoutRes
    int contentLayout();

    
    protected @MenuRes
    int menu() {
        return 0;
    }

    
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void beforeViews() {

    }




    
    protected void afterViews() {

    }

    
    protected void afterMenus(Menu menu) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideInputMethod();
    }

    protected boolean showHomeMenuItem() {
        return true;
    }





    public void showLoading(boolean ishow ){
        Logger.e("-----showLoading---->"+ishow);
        if(ishow){
            dialog= new MaterialDialog.Builder(this).theme(Theme.LIGHT)
                    .content(R.string.str_loading)
                    .progress(true, 100)
                    .cancelable(false)
                    .build();
            if (hasWindowFocus()) dialog.show();
        } else{
            if(dialog!=null){
                dialog.dismiss();
                dialog=null;
            }

        }

    }


    public void showLoading(boolean ishow,String message){
//        Logger.e("-----showLoading---->"+ishow);
        if(ishow){
            if(dialog!=null){
                dialog.show();
                return;
            }
            dialog= new MaterialDialog.Builder(this).theme(Theme.LIGHT)
                    .content(message)
                    .progress(true, 2000)
                    .build();
//            if (hasWindowFocus()){
//                Logger.e("-----hasWindowFocus---->");
            dialog.show();
//            }
        } else{
            if(dialog!=null){
                dialog.dismiss();
                dialog=null;
            }

        }

    }



    public void showLoading(boolean ishow,String message,boolean iscancel){
//        Logger.e("-----showLoading---->"+ishow);
        if(ishow){
            if(dialog!=null){
                dialog.getBuilder().content(message);
                dialog.show();
                return;
            }
            dialog= new MaterialDialog.Builder(this).theme(Theme.LIGHT)
                    .content(message)
                    .cancelable(iscancel)
                    .progress(true, 2000)
                    .build();
//            if (hasWindowFocus()){
//                Logger.e("-----hasWindowFocus---->");
            dialog.show();
//            }
        } else{
            if(dialog!=null){
                dialog.dismiss();
                dialog=null;
            }

        }

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().finishActivity(this);
    }


}

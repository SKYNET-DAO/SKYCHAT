package cn.wildfire.imshat.app.third.location.ui.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.jaeger.library.StatusBarUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;

public abstract class BaseActivity<V, T extends BasePresenter<V>> extends AppCompatActivity {

    protected T mPresenter;

    
    @Bind(R.id.appBarLayout)
    protected AppBarLayout mAppBar;
    //    @Bind(R.id.toolbar)
    //    protected Toolbar mToolbar;
    @Bind(R.id.toolbarContainerFrameLayout)
    public FrameLayout mToolbar;
    @Bind(R.id.backImageView)
    public ImageView mToolbarNavigation;
    @Bind(R.id.backDividerView)
    public View mToolbarDivision;
    @Bind(R.id.titleLinearLayout)
    public LinearLayout mLlToolbarTitle;
    @Bind(R.id.titleTextView)
    public TextView mToolbarTitle;
    @Bind(R.id.subTitleTextView)
    public TextView mToolbarSubTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }

        
        setContentView(provideContentViewId());
        ButterKnife.bind(this);

        setupAppBarAndToolbar();

        
        StatusBarUtil.setColor(this, UIUtils.getColor(R.color.colorPrimaryDark), 10);

        initView();
        initData();
        initListener();
    }


    private void setupAppBarAndToolbar() {
        
        if (mAppBar != null && Build.VERSION.SDK_INT > 21) {
            mAppBar.setElevation(10.6f);
        }

        
//        if (mToolbar != null) {
//            setSupportActionBar(mToolbar);
//            if (isToolbarCanBack()) {
//                ActionBar actionBar = getSupportActionBar();
//                if (actionBar != null) {
//                    actionBar.setDisplayHomeAsUpEnabled(true);
//                }
//            }
//        }

        mToolbarNavigation.setVisibility(isToolbarCanBack() ? View.VISIBLE : View.GONE);
        mToolbarDivision.setVisibility(isToolbarCanBack() ? View.VISIBLE : View.GONE);
        mToolbarNavigation.setOnClickListener(v -> onBackPressed());
        mLlToolbarTitle.setPadding(isToolbarCanBack() ? 0 : 40, 0, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    
    public void init() {
    }

    public void initView() {
    }

    public void initData() {
    }

    public void initListener() {
    }

    
    protected abstract T createPresenter();

    
    protected abstract int provideContentViewId();


    protected boolean isToolbarCanBack() {
        return true;
    }


    public void jumpToActivity(Intent intent) {
        startActivity(intent);
    }

    public void jumpToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    public void jumpToActivityAndClearTask(Class activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void jumpToActivityAndClearTop(Class activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    
    public void setToolbarTitle(String title) {
        mToolbarTitle.setText(title);
    }

    public void setToolbarSubTitle(String subTitle) {
        mToolbarSubTitle.setText(subTitle);
        mToolbarSubTitle.setVisibility(subTitle.length() > 0 ? View.VISIBLE : View.GONE);
    }

}

package cn.wildfire.imshat.language;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.base.LanguageSettingBean;
import com.android.base.LanguageUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.Locale;

import cn.wildfire.imshat.kit.WfcBaseActivity;
import cn.wildfire.imshat.kit.third.utils.UIUtils;
import com.android.base.utils.ACacheUtil;
import cn.wildfire.imshat.wallet.activity.AppManager;
import com.android.base.conts.AcacheKeys;
import cn.wildfirechat.imshat.R;

public class LanguageActivity extends WfcBaseActivity {


    private RecyclerView mLanguageSelet;
    SmartRefreshLayout smartRefreshLayout;
    private LanguageSeletorAdapter mLanguageSeletorAdapter;

    @Override
    protected int contentLayout() {
        return R.layout.activity_language;
    }

    @Override
    protected void afterViews() {
        setTitle(getString(R.string.str_language_switch));
        mLanguageSelet = (RecyclerView) findViewById(R.id.rv_language_list);
        smartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smartrefreshlayout);
        smartRefreshLayout.setEnableLoadmore(false);
        smartRefreshLayout.setEnableRefresh(false);


        mLanguageSeletorAdapter = new LanguageSeletorAdapter(LanguageUtil.mArrayList);
        mLanguageSelet.setAdapter(mLanguageSeletorAdapter);
        LanguageSettingBean languageSettingBean = (LanguageSettingBean) ACacheUtil.get(this).getAsObject(AcacheKeys.LANGUAGELOCALE);
        if (languageSettingBean != null) {
            Locale locale = languageSettingBean.getLocale();
            int position = LanguageUtil.getPositionForLocale(locale);
            if (getResources().getConfiguration().locale.toString().equals("zh_TW") ||
                    getResources().getConfiguration().locale.toString().equals("zh_MO_#Hant") ||
                    getResources().getConfiguration().locale.toString().equals("zh_HK_#Hant") ||
                    getResources().getConfiguration().locale.toString().equals("zh_TW_#Hant") ||
                    getResources().getConfiguration().locale.toString().equals("zh_HK") ||
                    getResources().getConfiguration().locale.toString().equals("zh_MO")) {//zh_TW_#Hant
                mLanguageSeletorAdapter.setCurrentPosition(2);
                return;
            }
            mLanguageSeletorAdapter.setCurrentPosition(position);
        }

        onEvent();

    }


    private void onEvent() {

        mLanguageSelet.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {

                MaterialDialog dialog = new MaterialDialog.Builder(LanguageActivity.this)
                        .content(UIUtils.getString(R.string.str_switch_language_content))
                        .positiveText(R.string.str_restart_app_now)
                        .negativeText(R.string.str_cancel)
                        .cancelable(false)
                        .onPositive((dialog1, which) -> {
                            mLanguageSeletorAdapter.setCurrentPosition(position);
                            LanguageSettingBean languageSettingBean = LanguageUtil.mArrayList.get(position);
                            if (languageSettingBean != null) {
                                ACacheUtil.get(LanguageActivity.this).put(AcacheKeys.LANGUAGELOCALE,
                                        new LanguageSettingBean(languageSettingBean.getName(), languageSettingBean.getLocale()));
                            }
                            
                            LanguageUtil.initAppLanguage(LanguageActivity.this);
                            AppManager.getInstance().restartApp(LanguageActivity.this,"");

                        }).build();
                dialog.show();

            }
        });

    }
}

package com.android.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;


import com.android.base.conts.AcacheKeys;
import com.android.base.utils.ACacheUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Locale;




public class LanguageUtil {


    public static final ArrayList<LanguageSettingBean> mArrayList = new ArrayList<>();

   
    public static Context initAppLanguage(Context context) {
        if (context == null) {
            return null;
        }
        
        if (mArrayList.size() == 0) {
            mArrayList.add(new LanguageSettingBean("English", Locale.ENGLISH));
            mArrayList.add(new LanguageSettingBean("日本語", Locale.JAPAN));
            mArrayList.add(new LanguageSettingBean("한국어", new Locale("ko")));
            mArrayList.add(new LanguageSettingBean("Français", Locale.FRANCE));
            mArrayList.add(new LanguageSettingBean("Español", new Locale("es")));
//            mArrayList.add(new LanguageSettingBean("Nederlands", new Locale("nl")));
            mArrayList.add(new LanguageSettingBean("Tiếng Việt", new Locale("vi")));
            mArrayList.add(new LanguageSettingBean("繁體中文", new Locale("fi")));
            mArrayList.add(new LanguageSettingBean("简体中文", Locale.SIMPLIFIED_CHINESE));
        }

        Configuration config = context.getResources().getConfiguration();
        
        LanguageSettingBean languageSettingBean =null; //(LanguageSettingBean) ACacheUtil.get(context).getAsObject(AcacheKeys.LANGUAGELOCALE);
        if (languageSettingBean == null) {
            if (!isSupportCurrentContry(mArrayList)) {
                Locale.setDefault(Locale.ENGLISH);
                config.locale = Locale.ENGLISH;
            } else {
                Locale.setDefault(Locale.getDefault());
                config.locale = Locale.getDefault();
            }
            ACacheUtil.get(context).put(AcacheKeys.LANGUAGELOCALE, new LanguageSettingBean("", config.locale));

        } else {
            Locale.setDefault(languageSettingBean.getLocale());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(languageSettingBean.getLocale());
            } else {
                config.locale = languageSettingBean.getLocale();
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            return context.createConfigurationContext(config);
        } else {

            context.getResources().updateConfiguration(config
                    , context.getResources().getDisplayMetrics());
        }

        return context;

    }


    
    private static boolean isSupportCurrentContry(ArrayList<LanguageSettingBean> mArrayList) {

        for (LanguageSettingBean item : mArrayList) {
            if (item.getLocale().getLanguage().equals(Locale.getDefault().getLanguage())) {
                return true;
            }
        }
        return false;
    }


    public static int getPositionForLocale(Locale locale) {
        for (LanguageSettingBean item : mArrayList) {
            if (item.getLocale().getLanguage().equals(locale.getLanguage())) {

                return mArrayList.indexOf(item);
            }
        }
        return 0;
    }

    public static boolean isCanConvertToPinYin(Context context){
        String localLanguage = context.getResources().getConfiguration().locale.getLanguage();
        switch (localLanguage) {
            case "en":
                return true;
            case "zh":
                return true;
            case "fi":
                return true;
            default:
                if (localLanguage.equals("zh")) {
                    return true;
                }
                break;
        }

        return false;
    }


    public static String getCurrentLang(){

        LanguageSettingBean languageSettingBean = (LanguageSettingBean) ACacheUtil.get(BaseApp.getContext()).getAsObject(AcacheKeys.LANGUAGELOCALE);
        if(languageSettingBean!=null){
            languageSettingBean.getLocale();
            Logger.d("------languageSettingBean.getLocale()---->"+languageSettingBean.getLocale()+"  "+languageSettingBean.getName());
        }

        String lang=languageSettingBean.getLocale().toString();
        if(lang.contains("_")){
            lang=lang.substring(0,lang.indexOf("_"));
        }

        return lang;

    }
}

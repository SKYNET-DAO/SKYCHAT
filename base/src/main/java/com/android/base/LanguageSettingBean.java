package com.android.base;

import java.io.Serializable;
import java.util.Locale;



public class LanguageSettingBean implements Serializable {
    private String name;
    private  Locale locale;

    public LanguageSettingBean(String name, Locale locale) {
        this.name = name;
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

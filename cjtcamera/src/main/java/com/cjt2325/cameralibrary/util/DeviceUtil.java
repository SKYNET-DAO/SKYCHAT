package com.cjt2325.cameralibrary.util;

import android.os.Build;


public class DeviceUtil {

    private static String[] huaweiRongyao = {
            "hwH60",    
            "hwPE",     
            "hwH30",    
            "hwHol",    
            "hwG750",   
            "hw7D",      
            "hwChe2",      
    };

    public static String getDeviceInfo() {
        String handSetInfo =
                "Phone model：" + Build.DEVICE +
                        "\nOS ver：" + Build.VERSION.RELEASE +
                        "\nSDK Ver：" + Build.VERSION.SDK_INT;
        return handSetInfo;
    }

    public static String getDeviceModel() {
        return Build.DEVICE;
    }

    public static boolean isHuaWeiRongyao() {
        int length = huaweiRongyao.length;
        for (int i = 0; i < length; i++) {
            if (huaweiRongyao[i].equals(getDeviceModel())) {
                return true;
            }
        }
        return false;
    }
}

package com.android.wallet.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.android.wallet.constants.Constants;

import org.bitcoinj.core.VersionMessage;

public class AppUtils {



    public static String httpUserAgent(final String versionName) {
        final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
        versionMessage.appendToSubVer(Constants.USER_AGENT, versionName, null);
        return versionMessage.subVer;
    }


    private static PackageInfo packageInfo;

    public static synchronized PackageInfo packageInfo( Application context) {
        // replace by BuildConfig.VERSION_* as soon as it's possible
        if (packageInfo == null) {
            try {
                packageInfo =context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return packageInfo;
    }


}

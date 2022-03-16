package cn.wildfire.imshat.wallet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;


public class NetworkUtil {


    public static final int CHINA_MOBILE = 1; 
    public static final int CHINA_UNICOM = 2; 
    public static final int CHINA_TELECOM = 3; 


    public static final int SIM_OK = 0;
    public static final int SIM_NO = -1;
    public static final int SIM_UNKNOW = -2;

    public static final String CONN_TYPE_WIFI = "wifi";
    public static final String CONN_TYPE_GPRS = "gprs";
    public static final String CONN_TYPE_NONE = "none";

    
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infos = manager.getAllNetworkInfo();
        for (NetworkInfo info : infos) {
            if (info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean getNetWorkState(Context context) {
        
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

                if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                    return true;
                } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                    return true;
                }
            }
            return false;

        } else {
            
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            Network[] networks = connMgr.getAllNetworks();
            
            for (int i = 0; i < networks.length; i++) {
                
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (networkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                        return true;
                    } else if (networkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                        return true;
                    }
                }

            }
            return false;
        }
    }

    
    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

   
    public static boolean is3G(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    
    public static String getWifiOr2gOr3G(Context context) {
        String networkType = "";
        if (context != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
                if (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting()) {
                    networkType = activeNetInfo.getTypeName().toLowerCase();
                    if (networkType.equals("wifi")) {
                        networkType = "WF";
                    } else { 
                        networkType = "2G"; 
                        int subType = activeNetInfo.getSubtype();
                        switch (subType) {
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_CDMA: // IS95
                                break;
                            case TelephonyManager.NETWORK_TYPE_EDGE: // 2.75
                                break;
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_GPRS: // 2.5
                                break;
                            case TelephonyManager.NETWORK_TYPE_HSDPA: // 3.5
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_HSPA: // 3.5
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                                networkType = "3G";
                                break;
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                                networkType = "3G";
                                break; // ~ 1-2 Mbps
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                networkType = "3G";
                                break; // ~ 5 Mbps
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                                networkType = "3G";
                                break; // ~ 10-20 Mbps
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                break; // ~25 kbps
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                networkType = "4G";
                                break; // ~ 10+ Mbps
                            default:
                                break;
                        }
                    } 
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
        }
        return networkType;
    }

   
    public static String getNetworkType(Context context) {
        String result = null;


        ConnectivityManager connectivity = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));

        if (connectivity == null) {
            result = null;
        } else {

            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i] != null) {
                        State tem = info[i].getState();
                        if ((tem == State.CONNECTED || tem == State.CONNECTING)) {
                            String temp = info[i].getExtraInfo();
                            result = info[i].getTypeName() + " "
                                    + info[i].getSubtypeName() + temp;
                            break;
                        }
                    }
                }
            }

        }

        return result;
    }

  
    public static String getNetConnectType(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null == connManager) {
            return CONN_TYPE_NONE;
        }

        NetworkInfo info = null;
        info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != info) {
            State wifiState = info.getState();
            if (State.CONNECTED == wifiState) {
                return CONN_TYPE_WIFI;
            }
        } else {
        }

        info = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != info) {
            State mobileState = info.getState();
            if (State.CONNECTED == mobileState) {
                return CONN_TYPE_GPRS;
            }
        } else {
        }
        return CONN_TYPE_NONE;
    }


    public static String getProxy(Context context) {
        String proxy = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isAvailable()) {
                String stringExtraInfo = networkinfo.getExtraInfo();
                if (stringExtraInfo != null && ("cmwap".equals(stringExtraInfo) || "uniwap".equals(stringExtraInfo))) {
                    proxy = "10.0.0.172:80";
                } else if (stringExtraInfo != null && "ctwap".equals(stringExtraInfo)) {
                    proxy = "10.0.0.200:80";
                }
            }
        }

        return proxy;
    }


    public static int getSimState(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        if (simState == TelephonyManager.SIM_STATE_READY) {
            return SIM_OK;
        } else if (simState == TelephonyManager.SIM_STATE_ABSENT) {
            return SIM_NO;
        } else {
            return SIM_UNKNOW;
        }
    }


    public static int getNSP(Context context, StringBuffer nsp) {

        if (getSimState(context) == SIM_OK) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = tm.getNetworkOperatorName();

            //String numeric = tm.getNetworkOperator();

            if (operator == null || operator.length() == 0) {
                return SIM_UNKNOW;
            }
            if (nsp != null) {
                nsp.delete(0, nsp.length());
                nsp.append(operator);
            }
            if (operator.compareToIgnoreCase(UIUtils.getString(R.string.str_zgyd)) == 0
                    || operator.compareToIgnoreCase("CMCC") == 0
                    || operator.compareToIgnoreCase("China Mobile") == 0
                    || operator.compareToIgnoreCase("46000") == 0
                    || operator.compareToIgnoreCase("46002") == 0) {
                return CHINA_MOBILE;
            } else if (operator.compareToIgnoreCase(UIUtils.getString(R.string.str_zjdx)) == 0
                    || operator.compareToIgnoreCase("China Telecom") == 0
                    || operator.compareToIgnoreCase("46003") == 0
                    || operator.compareToIgnoreCase("China Telcom") == 0) {
                return CHINA_TELECOM;
            } else if (operator.compareToIgnoreCase(UIUtils.getString(R.string.str_zglt)) == 0
                    || operator.compareToIgnoreCase("China Unicom") == 0
                    || operator.compareToIgnoreCase("46001") == 0
                    || operator.compareToIgnoreCase("CU-GSM") == 0) {
                return CHINA_UNICOM;
            } else {
                return SIM_UNKNOW;
            }
        } else {
            return SIM_NO;
        }
    }


}

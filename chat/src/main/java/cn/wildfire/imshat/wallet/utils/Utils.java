package cn.wildfire.imshat.wallet.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 */

public class Utils {

    private static int DEF_DIV_SCALE = 2;
    private static String sResult;

    
    public static String formatPhoneNO(String pNumber) {
        if (!TextUtils.isEmpty(pNumber) && pNumber.length() > 6) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pNumber.length(); i++) {
                char c = pNumber.charAt(i);
                if (i >= 3 && i <= 6) {
                    sb.append('*');
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return null;
        }

    }

    public static String formatCardNO(String pNumber) {
        if (!TextUtils.isEmpty(pNumber) && pNumber.length() > 6) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pNumber.length(); i++) {
                char c = pNumber.charAt(i);
                if (i >= 8 && i <= 12) {
                    sb.append('*');
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return null;
        }

    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return true;
        }
        return false;
    }

    
    public static String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

   
    public static String formatDouble1(double value) {
        BigDecimal decimal = new BigDecimal(value);
        decimal = decimal.setScale(1, BigDecimal.ROUND_HALF_UP);
        return "" + decimal;
    }

    
    public static String formatDouble2(double value) {
        BigDecimal decimal = new BigDecimal(value);
        decimal = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        return "" + decimal;
    }

    
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

   
    public static double div(double v1, double v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


   
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }



    public static boolean isInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> mPacks = pm.getInstalledPackages(0);
        for (PackageInfo info : mPacks) {
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                if (packageName.equals(info.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


   
    public static String add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Utils.toNormal(v1));
        BigDecimal b2 = new BigDecimal(Utils.toNormal(v2));


        String result = String.valueOf(b1.add(b2).doubleValue());
        return result;
    }

   
    public static String sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Utils.toNormal(v1));
        BigDecimal b2 = new BigDecimal(Utils.toNormal(v2));

        String v = String.valueOf(b1.subtract(b2).doubleValue());
        return v;
    }

    
    public static double sub2Double(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Utils.toNormal(v1));
        BigDecimal b2 = new BigDecimal(Utils.toNormal(v2));

        String v = String.valueOf(b1.subtract(b2).doubleValue());

        return Double.valueOf(v);
    }

   
    public static double multiply(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);

        return b1.multiply(b2).doubleValue();
    }

    public static String floatToPecent(float number) {
        DecimalFormat numberFormat = new DecimalFormat("0.00%");
        String format = numberFormat.format(number);
        return format;
    }

    public static String doubleToPecent(double number) {
        DecimalFormat numberFormat = new DecimalFormat("0.00%");
        String format = numberFormat.format(number);
        return format;
    }

    public static String doubleToPecent(String number) {
        DecimalFormat numberFormat = new DecimalFormat("0.00%");
        String format = numberFormat.format(number);
        return format;
    }

    public static String toNormal(double amount) {
        BigDecimal decimalFormat = new BigDecimal(String.valueOf(amount));
      //  LogUtil.i("----toNormal---->", "toNormal: " + decimalFormat.toPlainString());
        return decimalFormat.toPlainString();
    }

    public static String toNormal(String amount) {
        BigDecimal decimalFormat = new BigDecimal(amount);
        return decimalFormat.toPlainString();
    }

    public static String to4Decimal(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        return nf.format(amount);
    }

    public static String to4SubString(double amount) {
        String amountResult = Utils.toNormal(amount);
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder(split[0]);
            if (split.length > 1 && split[1].length() > 3) {
                stringBuilder.append(".").append(split[1].substring(0, 4));
                return Utils.toDoubleNormal(Double.parseDouble(stringBuilder.toString()), 4);
            } else {
                return Utils.toDoubleNormal(Double.parseDouble(stringBuilder.toString()), 4);
            }
        } else {
            return Utils.toDoubleNormal(Double.parseDouble(amountResult), 4);
        }
    }

   
    public static String toSubStringNo(double amount, int digister) {
        String amountResult = Utils.toNormal(amount);
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder(split[0]);
            if (split.length > 1 && split[1].length() > digister - 1) {
                stringBuilder.append(".").append(split[1].substring(0, digister));
                return Utils.toDoubleNormal(Double.parseDouble(stringBuilder.toString()), digister);
            } else {
                return Utils.toDoubleNormal(amount, digister);
            }
        } else {
            return Utils.toDoubleNormal(Double.parseDouble(amountResult), digister);
        }
    }


    
    public static String toSubStringDegist(double amount, int degist) {
        String amountResult = Utils.toNormal(amount);
        //2353453---->2353453.0
        if(!amountResult.contains("."))amountResult=amountResult+".0";
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder();
//            StringBuilder stringBuilder=new StringBuilder(split[0]);
            String strFront = Utils.toDoubleNormal(Double.parseDouble(split[0]), 0);
            if (split.length > 1 && split[1].length() > degist - 1) {
                stringBuilder.append(".").append(split[1].substring(0, degist));
                String strBack = stringBuilder.toString();
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + strBack;
                } else {
                    return strFront + strBack;
                }
            } else if (split.length > 1) {
                stringBuilder.append(".").append(split[1]);
                for (int i = 0; i < (degist) - split[1].length(); i++) {
                    stringBuilder.append("0");
                }
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + stringBuilder.toString();
                }
                return strFront + stringBuilder.toString();
            } else {
                return Utils.toDoubleNormal(Double.parseDouble(amountResult), degist);
            }
        } else {
            return Utils.to2Double(Double.parseDouble(amountResult));
        }
    }
   
    public static String toNoPointSubStringDegistIfDegistIsZero(double amount, int degist) {
        String amountResult = Utils.toNormal(amount);
        if(degist == 0){
            return new BigDecimal(amount).setScale(0, BigDecimal.ROUND_DOWN).toString();
        }
        //2353453---->2353453.0
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder();
//            StringBuilder stringBuilder=new StringBuilder(split[0]);
            String strFront = Utils.toDoubleNormal(Double.parseDouble(split[0]), 0);
            if (split.length > 1 && split[1].length() > degist - 1) {
                stringBuilder.append(".").append(split[1].substring(0, degist));
                String strBack = stringBuilder.toString();
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + strBack;
                } else {
                    return strFront + strBack;
                }
            } else if (split.length > 1) {
                stringBuilder.append(".").append(split[1]);
                for (int i = 0; i < (degist) - split[1].length(); i++) {
                    stringBuilder.append("0");
                }
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + stringBuilder.toString();
                }
                return strFront + stringBuilder.toString();
            } else {
                return Utils.toDoubleNormal(Double.parseDouble(amountResult), degist);
            }
        } else {
            return Utils.to2Double(Double.parseDouble(amountResult));
        }
    }


    
    public static String toSubStringDegistForChart(double amount, int degist,boolean supplement) {
        String amountResult = Utils.toNormal(amount);
        //2353453---->2353453.0
        if(!amountResult.contains("."))amountResult=amountResult+".0";
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder();
//            StringBuilder stringBuilder=new StringBuilder(split[0]);
            String strFront = Utils.toDoubleNormal(Double.parseDouble(split[0]), 0);
            if (split.length > 1 && split[1].length() > degist - 1) {
                stringBuilder.append(".").append(split[1].substring(0, degist));
                String strBack = stringBuilder.toString();
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + strBack;
                } else {
                    if(degist==0){
                        return strFront;
                    }
                    return strFront + strBack;
                }
            } else if (split.length > 1) {
                stringBuilder.append(".").append(split[1]);

                
                if(supplement){
                    for (int i = 0; i < (degist) - split[1].length(); i++) {
                        stringBuilder.append("0");
                    }
                }
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + stringBuilder.toString();
                }
                return strFront + stringBuilder.toString();
            } else {
                return Utils.toDoubleNormal(Double.parseDouble(amountResult), degist);
            }
        } else {
            return Utils.to2Double(Double.parseDouble(amountResult));
        }
    }



    
    public static String toSubStringDegistForChartStr(String amount, int degist,boolean supplement) {
        //    String amountResult = Utils.toNormal(amount);

        String amountResult = amount;
        //2353453---->2353453.0
        if(!amountResult.contains("."))amountResult=amountResult+".0";
        if (amountResult.contains(".")) {
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder = new StringBuilder();
//            StringBuilder stringBuilder=new StringBuilder(split[0]);
            String strFront = Utils.toDoubleNormal(Double.parseDouble(split[0]), 0);
            if (split.length > 1 && split[1].length() > degist - 1) {
                stringBuilder.append(".").append(split[1].substring(0, degist));
                String strBack = stringBuilder.toString();
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + strBack;
                } else {
                    return strFront + strBack;
                }
            } else if (split.length > 1) {
                stringBuilder.append(".").append(split[1]);

                
                if(supplement){
                    for (int i = 0; i < (degist) - split[1].length(); i++) {
                        stringBuilder.append("0");
                    }
                }
                if ("00".equals(strFront)) {
                    return strFront.replaceAll("00", "0") + stringBuilder.toString();
                }
                return strFront + stringBuilder.toString();
            } else {
                return Utils.toDoubleNormal(Double.parseDouble(amountResult), degist);
            }
        } else {
            return Utils.to2Double(Double.parseDouble(amountResult));
        }
    }




    
    public static String toSubStringDegistNo(String amount,int degist){
        // String amountResult =Utils.toNormal(amount);
        String amountResult =amount;

        if (amountResult.contains(".")){
            String[] split = amountResult.split("\\.");
            StringBuilder stringBuilder=new StringBuilder();
//            StringBuilder stringBuilder=new StringBuilder(split[0]);
            String strFront = Utils.toDoubleNormal(Double.parseDouble(split[0]), 0);
            if (split.length>1&&split[1].length()>degist-1){
                stringBuilder.append(".").append(split[1].substring(0,degist));
                String strBack = stringBuilder.toString();
                if ("00".equals(strFront)){
                    return (strFront.replaceAll("00","0")+strBack).replaceAll(",","");
                }else {
                    return (strFront+ strBack).replaceAll(",","");
                }
            }else if (split.length>1){
                stringBuilder.append(".").append(split[1]);
                for (int i = 0; i < (degist)-split[1].length(); i++) {
                    stringBuilder.append("0");
                }
                if ("00".equals(strFront)){
                    return (strFront.replaceAll("00","0")+stringBuilder.toString()).replaceAll(",","");
                }
                return (strFront+stringBuilder.toString()).replaceAll(",","");
            }else {
                return (Utils.toDoubleNormal(Double.parseDouble(amountResult),degist)).replaceAll(",","");
            }
        }else {
            return (Utils.to2Double(Double.parseDouble(amountResult))).replaceAll(",","");
        }
    }



    public static String to2Decimal(double amount) {
        BigDecimal decimalFormat = new BigDecimal(amount);
        decimalFormat.setScale(2, BigDecimal.ROUND_DOWN);
        return decimalFormat.toPlainString();
    }

    public static String to2Double(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    public static String toDoubleNormal(double amount, int digits) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(digits);
        return nf.format(amount);
    }


    public static String to8Double(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(8);
        return nf.format(amount);
    }

    public static String getVersionName(Context context) throws Exception {
        PackageManager packageManager = context.getPackageManager();
        
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String version = packInfo.versionName;
        return version;
    }

    public static int getPrecision(String amount) {
        int index = amount.indexOf("1") - 1;

        return index;
    }

    public static boolean isUsername(final CharSequence input) {
        return isMatch(REGEX_USERNAME, input);
    }

  
    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }

    
    public static final String REGEX_USERNAME = "^[A-Za-z0-9]{4,16}$";


}

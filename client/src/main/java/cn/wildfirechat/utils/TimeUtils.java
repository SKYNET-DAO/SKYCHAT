package cn.wildfirechat.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class TimeUtils {



    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 计算夏令（因为现在服务器返回的是 美国时间 美国在（3～11月是夏令时间），中国等亚洲不存在）
     * 实现思路：本地包含夏令 直接用服务器返回时间  不包含夏令  减去一个小时时间
     * 计算总的偏移量=（服务器返回时间）-偏移时间（即 相对格林偏移+夏令偏移）   亚洲部分国家取消夏令 ，西方国家使用
     * tip；节能模式多加一个小时是多余的
     */

    public static long calculateTime(long serverTime){

        long ONE_HOUR_MILLIS = 60 * 60 * 1000;
        //    Logger.e("----服务器返回时间----->"+serverTime);//服务器返回的时间已经包括了夏令时间
        // Current timezone and date
        TimeZone timeZone = TimeZone.getDefault();
        //   Logger.e("------当前时区----->"+timeZone.getDisplayName());
        //get offset with gmt+0
        long gmt0Mills=TimeUtils.getDefaultTimeZoneRawOffset();

        //  Logger.e("------当前时区和gmt+0偏差----->"+gmt0Mills/(60*60*1000)+" "+gmt0Mills);
        //get dts fofset
        long dstOffsetMills = timeZone.getDSTSavings();
        //  Logger.e("------当前时区dst偏差----->"+dstOffsetMills);
        long result=0;
        if(timeZone.useDaylightTime()){//light model
            //   Logger.e("------当前时区支持节能模式----->");
            if(timeZone.inDaylightTime(new Date(serverTime))){//is dst
                result=serverTime-ONE_HOUR_MILLIS;
                //    Logger.e("----当前时区在夏令制度区-->"+result);
            }else{
                result=serverTime;
                //   Logger.e("----当前时区不在在夏令制度区-->"+result);

            }

        }else{//not support model  判断指定时区是不是支持夏令（因为服务器是在美国）
            //   Logger.e("------当前时区不支持节能模式----->");
            if(!isInDSTforDate(serverTime)){
                //    Logger.e("----美国当前时区不在在夏令制度区-->"+serverTime);
                result=serverTime;
            }else{
                //
                result=serverTime-ONE_HOUR_MILLIS;
                //  Logger.e("----美国当前时区在夏令制度区-->"+serverTime+" ");

            }
        }
//        Log.e("----最终计算时间->",result+"");
        return result;

    }


    //判断美国洛杉矶时区返回的服务器时间  是不是支持夏令
    public static boolean isInDSTforDate(long serverTime){
        TimeZone america=TimeZone.getTimeZone("America/Los_Angeles");
        return america.inDaylightTime(new Date(serverTime));
    }



    /**
     *
     * @param localTime 本地时间
     * @return
     */
    public static long ReqFailSimulationAddOneH(long localTime){

        long ONE_HOUR_MILLIS = 60 * 60 * 1000;
        if(isInDSTforDate()){//美国在夏令时间
            localTime+=ONE_HOUR_MILLIS;
            Log.e("--ReqFailSimulation-->",localTime+"");

        }
        return localTime;
    }


    //获取美国洛杉矶时间是否是夏令时
    public static boolean isInDSTforDate(){
        TimeZone america=TimeZone.getTimeZone("America/Los_Angeles");
        //获取洛杉矶日历
        Calendar cal = Calendar.getInstance(america);
        //过去美国洛杉矶当前时间
        long currentTime= cal.getTimeInMillis();
        Log.e("---美国洛杉矶当前时间->",currentTime+"");
        //判断当前时间是不是夏令
        return isInDSTforDate(currentTime);

    }






    /**
     * 获取系统当前默认时区与UTC的时间差.(单位:毫秒)
     *
     * @return 系统当前默认时区与UTC的时间差.(单位:毫秒)
     */
    public static int getDefaultTimeZoneRawOffset() {
        return TimeZone.getDefault().getRawOffset();
    }


    public static String getFormatTimeyyyyMMddHHmm(long timeStamp) {
        return DATE_FORMAT.format(new Date(timeStamp));
    }




}

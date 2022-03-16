package cn.wildfire.imshat.kit.third.utils;

import android.content.res.Configuration;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.wildfire.imshat.app.MyApp;
import cn.wildfire.imshat.wallet.JsonUtil;
import cn.wildfirechat.imshat.R;


public class TimeUtils {

  
    public static String getMsgFormatTime(long msgTimeMillis) {

        DateTime nowTime = new DateTime();
//        LogUtils.sf("nowTime = " + nowTime);
        DateTime msgTime = new DateTime(msgTimeMillis);

//        LogUtils.sf("msgTime = " + msgTime);
        int days = Math.abs(Days.daysBetween(msgTime, nowTime).getDays());
//        LogUtils.sf("days = " + days);
        if (days < 1) {
            
            return getTime(msgTime);
        } else if (days == 1) {
            
            return UIUtils.getString(R.string.str_yesterday)+" " + getTime(msgTime);
        } else if (days <= 7) {
            
            switch (msgTime.getDayOfWeek()) {
                case DateTimeConstants.SUNDAY:
                    return UIUtils.getString(R.string.str_7)+" " + getTime(msgTime);
                case DateTimeConstants.MONDAY:
                    return UIUtils.getString(R.string.str_1)+" " + getTime(msgTime);
                case DateTimeConstants.TUESDAY:
                    return UIUtils.getString(R.string.str_2)+" " + getTime(msgTime);
                case DateTimeConstants.WEDNESDAY:
                    return UIUtils.getString(R.string.str_3)+" " + getTime(msgTime);
                case DateTimeConstants.THURSDAY:
                    return UIUtils.getString(R.string.str_4) +" "+ getTime(msgTime);
                case DateTimeConstants.FRIDAY:
                    return UIUtils.getString(R.string.str_5) +" "+ getTime(msgTime);
                case DateTimeConstants.SATURDAY:
                    return UIUtils.getString(R.string.str_6) +" "+ getTime(msgTime);
                default:
                    break;
            }
            return "";
        } else {
            
            

            return getTime1(msgTime);

        }
    }

    @NonNull
    private static String getTime(DateTime msgTime) {

//        yyyy-MM-dd HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss.SSS"

        int hourOfDay = msgTime.getHourOfDay();
        String when;
        if (hourOfDay >= 18) {//18-24
            when = UIUtils.getString(R.string.str_evening);
        } else if (hourOfDay >= 13) {//13-18
            when = UIUtils.getString(R.string.str_afternoon);
        } else if (hourOfDay >= 11) {//11-13
            when = UIUtils.getString(R.string.str_innoon);
        } else if (hourOfDay >= 5) {//5-11
            when = UIUtils.getString(R.string.str_morning);
        } else {//0-5
            when = UIUtils.getString(R.string.str_early);
        }
        return when + " " + msgTime.toString("hh:mm");

    }

    @NonNull
    private static String getTime1(DateTime msgTime) {

//        yyyy-MM-dd HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss.SSS"

//        int hourOfDay = msgTime.getHourOfDay();
//        String when;
//        if (hourOfDay >= 18) {//18-24
//            when = UIUtils.getString(R.string.str_evening);
//        } else if (hourOfDay >= 13) {//13-18
//            when = UIUtils.getString(R.string.str_afternoon);
//        } else if (hourOfDay >= 11) {//11-13
//            when = UIUtils.getString(R.string.str_innoon);
//        } else if (hourOfDay >= 5) {//5-11
//            when = UIUtils.getString(R.string.str_morning);
//        } else {//0-5
//            when = UIUtils.getString(R.string.str_early);
//        }
        //to show MM/dd/yyyy HH:mm after long time
        return  msgTime.toString("MM/dd/yyyy HH:mm");

    }



    public static String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }

    public static String timestamp2Utc(String timestamp){

      return   TimeUtils.format(new Date(timestamp),"yyyy-MM-dd HH:mm:ss");
    }


    public static Date getUTCTime(){
        Calendar cal = Calendar.getInstance();
        
        int offset = cal.get(Calendar.ZONE_OFFSET);
        
        int dstoff = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, - (offset + dstoff));
        return cal.getTime();

    }


    public static String getUTCTime(String format){
        String formatDate = format(getUTCTime(), format);
        return formatDate;
    }




    public static String format(Date date, String pattern) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            df.setTimeZone(TimeZone.getDefault());
            returnValue = df.format(date);
            Logger.e("------TimeZone.getDefault()--->"+TimeZone.getDefault().getDisplayName());
        }
        return (returnValue);
    }



    
    public static String formDate(Date value,int inout8) {
        String newValue = "null";
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            if (value == null) {
                return "null";
            }
            Calendar ca = Calendar.getInstance();
            ca.setTime(value);
            ca.add(Calendar.HOUR_OF_DAY, inout8);
            newValue =f.format(ca.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newValue;
    }



    public static int getDefaultTimeZoneRawOffset() {
              return TimeZone.getDefault().getRawOffset();
         }




      public static int getDiffTimeZoneRawOffset(String timeZoneId) {
             return TimeZone.getDefault().getRawOffset()
                      - TimeZone.getTimeZone(timeZoneId).getRawOffset();
           }


    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }



    public static String getUtc2Local(String dateStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = df.parse(dateStr);
        df.setTimeZone(TimeZone.getDefault());
        String formattedDate = df.format(date);
        return formattedDate;

    }







    public static long calculateTime(long serverTime){

        long ONE_HOUR_MILLIS = 60 * 60 * 1000;
    
        // Current timezone and date
        TimeZone timeZone = TimeZone.getDefault();
     
        //get offset with gmt+0
        long gmt0Mills=TimeUtils.getDefaultTimeZoneRawOffset();

      
        //get dts fofset
        long dstOffsetMills = timeZone.getDSTSavings();
      
        long result=0;
        if(timeZone.useDaylightTime()){//light model
         
            if(timeZone.inDaylightTime(new Date(serverTime))){//is dst
                result=serverTime-ONE_HOUR_MILLIS;
            
            }else{
                result=serverTime;
             

            }

        }else{
           
            if(!isInDSTforDate(serverTime)){
                
                result=serverTime;
            }else{
                //
                result=serverTime-ONE_HOUR_MILLIS;
                

            }
        }
        
        return result;

    }


    /**
     *
     * @param localTime 
     * @return
     */
    public static long ReqFailSimulationAddOneH(long localTime){

        long ONE_HOUR_MILLIS = 60 * 60 * 1000;

        return 0;

    }


    
    public static boolean isInDSTforDate(){
        TimeZone america=TimeZone.getTimeZone("America/Los_Angeles");
        
        Calendar cal = Calendar.getInstance(america);
        
       long currentTime= cal.getTimeInMillis();
       
        
        return isInDSTforDate(currentTime);

    }


    
    public static boolean isInDSTforDate(long serverTime){
        TimeZone america=TimeZone.getTimeZone("America/Los_Angeles");
        return america.inDaylightTime(new Date(serverTime));
    }


}

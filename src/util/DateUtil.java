package util;

public class DateUtil {

    public static String formatDateTime(long timeMillis){
        long day = timeMillis/(24*60*60*1000);
        long hour = (timeMillis/(60*60*1000)-day*24);
        long min = ((timeMillis/(60*1000))-day*24*60-hour*60);
        long s = (timeMillis/1000-day*24*60*60-hour*60*60-min*60);
        if (day>0){
            return day+"天"+hour+"时"+min+"分"+s+"秒";
        }else if (hour>0){
            return hour+"时"+min+"分"+s+"秒";
        }else if (min>0){
            return min+"分"+s+"秒";
        }else{
            return s+"秒";
        }
    }
}

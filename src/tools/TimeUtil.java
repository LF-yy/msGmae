//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools;

import gui.服务端输出信息;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    public static String[] webUrl = new String[]{"http://www.baidu.com", "http://www.taobao.com", "http://www.ntsc.ac.cn", "http://www.360.cn"};

    public TimeUtil() {
    }

    public static void main(String[] args) {
    }

    public static String getWebsiteDatetime(String webUrl) {
        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.connect();
            long ld = uc.getDate();
            Date date = new Date(ld);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            return sdf.format(date);
        } catch (MalformedURLException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

        return null;
    }

    public static String getWebsiteDatetime(int type) {
        try {
            if (type >= webUrl.length) {
                return "";
            }

            URL url = new URL(webUrl[type]);
            URLConnection uc = url.openConnection();
            uc.connect();
            long ld = uc.getDate();
            Date date = new Date(ld);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            return sdf.format(date);
        } catch (MalformedURLException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

        return null;
    }

    public static long getWebsiteTimeInMillis(String webUrl) {
        long nowTime = -1L;

        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.connect();
            long ld = uc.getDate();
            Date date = new Date(ld);
            nowTime = date.getTime();
            return nowTime;
        } catch (MalformedURLException var8) {
            服务端输出信息.println_err("【错误】获取时间错误，错误原因：" + var8);
            var8.printStackTrace();
        } catch (IOException var9) {
            服务端输出信息.println_err("【错误】获取时间错误，错误原因：" + var9);
            var9.printStackTrace();
        }

        return nowTime;
    }

    public static long getWebsiteTimeInMillis(int type) {
        long nowTime = -1L;

        try {
            if (type >= webUrl.length) {
                return nowTime;
            } else {
                URL url = new URL(webUrl[type]);
                URLConnection uc = url.openConnection();
                uc.connect();
                long ld = uc.getDate();
                Date date = new Date(ld);
                nowTime = date.getTime();
                return nowTime;
            }
        } catch (MalformedURLException var8) {
            服务端输出信息.println_err("【错误】获取时间错误，错误原因：" + var8);
            var8.printStackTrace();
            return -1L;
        } catch (IOException var9) {
            服务端输出信息.println_err("【错误】获取时间错误，错误原因：" + var9);
            var9.printStackTrace();
            return -1L;
        }
    }

    public static long getWebsiteTimeInMillis() {
        long nowTime = -1L;

        try {
            for(int i = 0; i < webUrl.length; ++i) {
                nowTime = getWebsiteTimeInMillis(i);
                if (nowTime > 0L) {
                    break;
                }
            }

            return nowTime;
        } catch (Exception var3) {
            服务端输出信息.println_err("【错误】获取时间错误，错误原因：" + var3);
            var3.printStackTrace();
            return -1L;
        }
    }

    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;
        Long day = ms / (long)dd;
        Long hour = (ms - day * (long)dd) / (long)hh;
        Long minute = (ms - day * (long)dd - hour * (long)hh) / (long)mi;
        Long second = (ms - day * (long)dd - hour * (long)hh - minute * (long)mi) / (long)ss;
        Long milliSecond = ms - day * (long)dd - hour * (long)hh - minute * (long)mi - second * (long)ss;
        StringBuffer sb = new StringBuffer();
        if (day > 0L) {
            sb.append(day + "天");
        }

        if (hour > 0L) {
            sb.append(hour + "小时");
        }

        if (minute > 0L) {
            sb.append(minute + "分");
        }

        if (second > 0L) {
            sb.append(second + "秒");
        }

        if (milliSecond > 0L) {
            sb.append(milliSecond + "毫秒");
        }

        return sb.toString();
    }
}

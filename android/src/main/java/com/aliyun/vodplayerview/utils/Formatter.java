package com.aliyun.vodplayerview.utils;

import java.util.Calendar;

/**
 * @Author: lifujun@alibaba-inc.com
 * @Date: 2016/12/29.
 * @Description:
 */

public class Formatter {

    public static String formatTime(int ms) {
        int totalSeconds = ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 60 / 60;
        String timeStr = "";
        if (hours > 9) {
            timeStr += hours + ":";
        } else if (hours > 0) {
            timeStr += "0" + hours + ":";
        }
        if (minutes > 9) {
            timeStr += minutes + ":";
        } else if (minutes > 0) {
            timeStr += "0" + minutes + ":";
        } else {
            timeStr += "00:";
        }
        if (seconds > 9) {
            timeStr += seconds;
        } else if (seconds > 0) {
            timeStr += "0" + seconds;
        } else {
            timeStr += "00";
        }

        return timeStr;
    }


    public static String formatDate(long seconds) {
        String finalStr = "";
        long mills = seconds * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        finalStr += (hour < 10 ? "0" + hour : hour) + ":";
        int minute = calendar.get(Calendar.MINUTE);
        finalStr += (minute < 10 ? "0" + minute : minute) + ":";
        int second = calendar.get(Calendar.SECOND);
        finalStr += (second < 10 ? "0" + second : second);

        return finalStr;

    }
    /**
     * double类型转换为日期  mm:ss
     * @param time
     * @return
     */
    public static String double2Date(double time) {
        long lTime = new Double(time).longValue();
        lTime = lTime - 28800;
        String s = formatDate(lTime);
        String substring = s.substring(3);
        return  substring;

    }
}

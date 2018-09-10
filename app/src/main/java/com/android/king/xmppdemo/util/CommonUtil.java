package com.android.king.xmppdemo.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.king.xmppdemo.R;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:38分
 * @since 2018-09-04
 * @author king
 */
public class CommonUtil {


    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {
        String result = "android";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getDeviceId() != null) {
                result = tm.getDeviceId();
            }
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * 格式化聊天记录时间
     *
     * @param time
     * @return
     */
    public static String formatTime(long time) {

        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH) + 1;
        int currentDay = current.get(Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if (currentYear == year && currentMonth == month) {
            if (currentDay == day) {
                return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
            } else if (currentDay - day > 1) {
                return (month < 10 ? "0" + month : month) + "月" + (day < 10 ? "0" + day : day) + "日";
            } else if (currentDay - day == 1) {
                return "昨天";
            }
        } else if (currentYear == year) {
            return (month < 10 ? "0" + month : month) + "月" + (day < 10 ? "0" + day : day) + "日";
        }
        return year + "年" + (month < 10 ? "0" + month : month) + "月" + (day < 10 ? "0" + day : day) + "日";
    }

    /**
     * 格式化消息记录时间
     *
     * @param time
     * @return
     */
    public static String formatMsgTime(long time) {

        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH);
        int currentDay = current.get(Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);

        if (currentYear == year && currentMonth == month) {
            if (currentDay == day) {
                return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
            } else if (currentDay - day > 1) {
                return (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + " " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
            } else if (currentDay - day == 1) {
                return "昨天 " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
            }
        } else if (currentYear == year) {
            return (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + " " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
        }
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + " " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
    }

    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }

    public static final String NOTICE_ID = "NOTICE_ID";
    public static final String NOTICE_NAME = "NOTICE_NAME";
    public static final String ACTION_RECEIVE_NOTICE = "cn.android.king.receive.notice";
    public static final int NOTICE_ID_TYPE_0 = R.string.app_name;

    public static void showNotify(Context context, String msg) {
        Intent intent = new Intent(ACTION_RECEIVE_NOTICE);
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTICE_ID, NOTICE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, NOTICE_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("提示");
        builder.setContentText(msg);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(NOTICE_ID_TYPE_0, notification);
    }

}

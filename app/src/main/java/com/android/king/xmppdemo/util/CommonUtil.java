package com.android.king.xmppdemo.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.ui.MainActivity;
import com.android.king.xmppdemo.ui.MessageActivity;

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
     * 判断网络是否可用
     * @param context
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
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
        int currentMonth = current.get(Calendar.MONTH) + 1;
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


    public static void showApplyNotify(Context context, String msg) {
        int requestCode = (int) SystemClock.uptimeMillis();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(AppConstants.FRIEND_CHANNEL_ID, AppConstants.FRIEND_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, AppConstants.FRIEND_CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("好友申请");
        builder.setContentText(msg);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(AppConstants.FRIEND_NOTIFY_ID, notification);
    }

    /**
     * @param context
     * @param title
     * @param msg
     * @param pendingIntent
     */
    public static void showMsgNotify(Context context, String title, String msg, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(AppConstants.MESSAGE_CHANNEL_ID, AppConstants.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, AppConstants.MESSAGE_CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(msg);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(AppConstants.MESSAGE_NOTIFY_ID, notification);
    }
}

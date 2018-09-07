package com.android.king.xmppdemo.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.king.xmppdemo.R;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.king.xmppdemo.ui.MainActivity;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:38分
 * @since 2018-09-04
 * @author king
 */
public class CommonUtil {


    public static String formatTime(long time) {

        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH);
        int currentDay = current.get(Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (currentYear == year && currentMonth == month) {
            if (currentDay == day) {
                return c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);
            } else if (currentDay - day > 1) {
                return c.get(Calendar.MONTH) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
            } else if (currentDay - day == 1) {
                return "昨天";
            }
        } else if (currentYear == year) {
            return c.get(Calendar.MONTH) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
        }
        return c.get(Calendar.YEAR) + "年" + c.get(Calendar.MONTH) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
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

    public static void showNotify(Context context, String title) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notify_friend_tip);
        remoteViews.setTextViewText(R.id.tv_msg, title);
        remoteViews.setImageViewResource(R.id.iv_icon, R.mipmap.ic_launcher);
        Intent intent = new Intent(ACTION_RECEIVE_NOTICE);
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.tv_receive, pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTICE_ID, NOTICE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, NOTICE_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
            notification.bigContentView = remoteViews;
        }
        notification.contentView = remoteViews;
        notificationManager.notify(NOTICE_ID_TYPE_0, notification);

    }

}

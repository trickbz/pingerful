package com.trickbz.pingerful.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.trickbz.pingerful.Host;
import com.trickbz.pingerful.MainActivity;
import com.trickbz.pingerful.R;

public final class NotificationService {

    public static void notifyPingFails(Context context, Host host)
    {
        String hostTitle = host.title.isEmpty() ?
                host.nameOrIp :
                String.format("%s (%s)", host.title, host.nameOrIp);
        notify(context, host, "Ping failed", hostTitle, R.string.pref_button_set_ping_fails_notification_ringtone, R.drawable.ping_red);
    }

    public static void notifyHostOnline(Context context, Host host)
    {
        String hostTitle = host.title.isEmpty() ?
                host.nameOrIp :
                String.format("%s (%s)", host.title, host.nameOrIp);
        notify(context, host, "Host online", hostTitle, R.string.pref_button_set_host_online_notification_ringtone, R.drawable.ping_green);
    }

    private static void notify(Context context, Host host, String title, String message, int ringtoneResourceId, int iconResourceId)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Uri defaultNotificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String ringtoneString = pref.getString(context.getString(ringtoneResourceId), null);
        Uri ringtoneUri = ringtoneString == null ? defaultNotificationUri : Uri.parse(ringtoneString);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setSmallIcon(iconResourceId)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 100, 1000})
                .setSound(ringtoneUri)
                .setContentIntent(intent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}

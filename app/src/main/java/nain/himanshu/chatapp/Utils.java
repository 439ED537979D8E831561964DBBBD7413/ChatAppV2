package nain.himanshu.chatapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class Utils {

    /*
    TODO:ADD IN APP
     */
    public static void createNotificationChannels(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(Config.GENERAL_NOTIF_CHANNEL, "General", NotificationManager.IMPORTANCE_DEFAULT),
                    channel2 = new NotificationChannel(Config.CHAT_NOTIF_CHANNEL, "Chats", NotificationManager.IMPORTANCE_DEFAULT);
            List<NotificationChannel> list = new ArrayList<>();
            list.add(channel1);
            list.add(channel2);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannels(list);

        }

    }

    public static void sendNotification(String title, String message, String Channel, Context context) {

        /*
            TODO:Improve notification to open chat
         */

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Channel)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(alarmSound);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(new Random().nextInt(), mBuilder.build());
    }

    public static String getTime(String dateTime) {

        dateTime = dateTime.replace("T"," ");
        dateTime = dateTime.replace("Z","");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        @SuppressLint("SimpleDateFormat")
        //SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");
        outputFormat.setTimeZone(TimeZone.getDefault());

        try {
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;

    }
}

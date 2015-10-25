package com.example.misha.gmailquickstart;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SchedulingService extends IntentService
{
    public static final int NOTIFICATION_ID = 1;

    public SchedulingService()
    {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            Calendar now = Calendar.getInstance();
            String strNow = DateFormat.getDateTimeInstance().format(now.getTime());
            sendNotification("I am here at " + strNow);
        }
        finally
        {
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void sendNotification(String msg)
    {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StartActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Title")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
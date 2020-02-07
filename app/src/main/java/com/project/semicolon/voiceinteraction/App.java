package com.project.semicolon.voiceinteraction;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    public static final String NOTIFICATION_CHANNEL = "notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "channel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Reminder Notification");
            Context context;
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }


        }
    }
}

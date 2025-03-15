package com.example.tnglogistics.Controller;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.tnglogistics.R;
import com.example.tnglogistics.View.MainActivity;

public class NotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // รับข้อมูลที่ส่งมาจาก Intent
        String title = intent.getStringExtra("notification_title");
        String message = intent.getStringExtra("notification_message");
        String activityName = intent.getStringExtra("destination_activity");

        // ตรวจสอบว่าได้ข้อมูลครบถ้วนหรือไม่
        if (title != null && message != null && activityName != null) {
            // สร้างการแจ้งเตือน
            Notification notification = createNotification(title, message);

            // เริ่มต้นการทำงานใน Foreground
            startForeground(1, notification);
        }

        return START_STICKY;
    }

    // สร้างการแจ้งเตือน
    private Notification createNotification(String title, String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "your_channel_id")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


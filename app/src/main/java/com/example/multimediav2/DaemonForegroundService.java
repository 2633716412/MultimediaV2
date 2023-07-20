package com.example.multimediav2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class DaemonForegroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "daemon_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知渠道（适用于 Android 8.0 及更高版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daemon Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在这里执行守护任务的逻辑

        // 将服务设置为前台服务，并显示通知
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);

        // 在任务完成后，如果服务不再需要运行，调用 stopSelf() 关闭服务
        // stopSelf();

        return START_STICKY; // 在服务被异常终止后，系统将尝试重新启动服务
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("守护服务正在运行")
                .setContentText("点击以打开应用")
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }
}

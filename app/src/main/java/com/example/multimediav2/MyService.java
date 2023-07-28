package com.example.multimediav2;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

public class MyService extends Service {

    /*private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(Paras.appContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            *//*Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);*//*
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(mRunnable, 10000); // 10 秒后执行
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/
    private static final long INTERVAL = 10000; // 监测间隔时间，单位毫秒
    private static final String TARGET_APP_PACKAGE_NAME = "com.example.multimediav2"; // 目标app的包名
    private Handler handler;
    private Runnable monitorRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                monitorApp();
                handler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(monitorRunnable, INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void monitorApp() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        if (runningTasks != null && runningTasks.size() > 0) {
            ComponentName topActivity = runningTasks.get(0).topActivity;
            if (topActivity != null && !topActivity.getPackageName().equals(TARGET_APP_PACKAGE_NAME)) {
                // 目标app没有在前台运行，启动目标app
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(TARGET_APP_PACKAGE_NAME);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                }
            }
        }
        if (activityManager != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (!processInfo.processName.equals(TARGET_APP_PACKAGE_NAME)) {
                    //当应用置于后台时置于前台
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(TARGET_APP_PACKAGE_NAME);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    //stopSelf();
                }
            }
        }
    }
}

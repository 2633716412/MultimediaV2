package com.example.multimediav2;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

public class AppService extends Service
{
    private PowerManager.WakeLock wakeLock = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AppService.class.getName());
        wakeLock.acquire();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    Paras.powerManager.setSystemTime(Paras.appContext);
                    try {
                        Thread.sleep(Paras.time_start_listen_power * 1000);
                        Paras.powerManager.StartListen();
                    } catch (Exception ex) {
                        LogHelper.Error(ex);
                    }
                }
            }
        }).start();*/
    }
    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}

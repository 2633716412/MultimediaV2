package com.example.multimediav2;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AppService extends Service
{
    private static final String TAG = "AppService";
    private static final long RESTART_DELAY = 10 * 1000; // 多少时间后重启检测(1小时)
    private MyBinder mBinder;

    // 此对象用于绑定的service与调用者之间的通信
    public class MyBinder extends Binder {

        /**
         * 获取service实例
         * @return
         */
        public AppService getService() {
            return AppService.this;
        }

        /**
         * 启动app重启任务
         */
        public void startRestartTask(final Context context) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(
                            getApplication().getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    System.exit(0);
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, RESTART_DELAY);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Create MyBinder object
        if (mBinder == null) {
            mBinder = new MyBinder();
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");
        super.onDestroy();
    }
}

package com.example.multimediav2;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import Modules.IMsgManager;
import Modules.LogHelper;
import Modules.Paras;

public class CustApplication extends Application implements Thread.UncaughtExceptionHandler, IMsgManager {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(this);

        Paras.appContext = this;

        Paras.Wiidth = getResources().getDisplayMetrics().widthPixels;
        Paras.Height = getResources().getDisplayMetrics().heightPixels;

        Paras.msgManager=this;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        LogHelper.Fatal(e.getMessage(), e);
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10 * 1000, restartIntent); // n秒后重启
        System.exit(0);
    }

    public void SendMsg(String msg) {
        Message message = new Message();
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            Toast.makeText(Paras.appContext, msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };

}
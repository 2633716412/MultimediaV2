package com.example.multimediav2;


import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.Nullable;

import Modules.LogHelper;

public class AppService extends Service
{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IProcessConnection.Stub() {};
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1,new Notification());
        //绑定建立链接
        bindService(new Intent(this,AppService2.class),
                mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            LogHelper.Debug("test","appService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(AppService.this,AppService2.class));
            //重新绑定
            bindService(new Intent(AppService.this,AppService2.class),
                    mServiceConnection, Context.BIND_IMPORTANT);
        }
    };


}

package com.example.multimediav2.Models;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import Modules.LogHelper;

public class MyBroadcastReceiver extends DeviceAdminReceiver {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    /*@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Objects.equals(action, Intent.ACTION_SCREEN_ON)) {
            Toast.makeText(context, "监听到开屏广播", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(action, Intent.ACTION_SCREEN_OFF)) {
            Toast.makeText(context, "监听到熄屏广播", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(action, "android.net.conn.CONNECTIVITY_CHANGE")) {
            Toast.makeText(context, "监听到网络变化广播", Toast.LENGTH_SHORT).show();
        }
    }*/
    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context,
                "设备管理器使能");
        LogHelper.Debug("设备管理器使能");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context,
                "设备管理器没有使能");
        LogHelper.Debug("设备管理器没有使能");
    }

}

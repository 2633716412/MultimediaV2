package com.example.multimediav2.PowerManager;

import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.Models.MyBroadcastReceiver;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Calendar;

import Modules.LogHelper;
import Modules.Paras;

public class PowerManagerDef extends BasePowerManager {
    private ComponentName adminReceiver;
    private PowerManager mPowerManager;
    private DevicePolicyManager policyManager;
    /*@Override
    public void ShutDown() {

        try {
            *//*final IntentFilter filter = new IntentFilter();
            // 屏幕灭屏广播
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            // 屏幕亮屏广播
            filter.addAction(Intent.ACTION_SCREEN_ON);
            MyBroadcastReceiver appService=new MyBroadcastReceiver ();
            Paras.appContext.registerReceiver(appService, filter);*//*
            //Paras.appContext.sendOrderedBroadcast(intent,null);
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            *//*localDataOutputStream.writeBytes("echo standby > /sys/power/state\n");
            localDataOutputStream.writeBytes("echo lock_name > /sys/power/wake_lock\n");*//*

            localDataOutputStream.writeBytes("echo 0 > /sys/class/backlight/pwm-backlight.0/brightness\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            LogHelper.Debug(ret + "");
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void ShutDown() {

        try {
            adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);
            mPowerManager=(PowerManager) Paras.appContext.getSystemService(POWER_SERVICE);
            policyManager=(DevicePolicyManager) Paras.appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            checkAndTurnOnDeviceManager(null);
            checkScreenOff(null);
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }

    @Override
    public void Open() {
        try {

        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
        //Reboot();
    }

    @Override
    public void Reboot() {
        try {

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            localDataOutputStream.writeBytes("reboot\n");
            localDataOutputStream.writeBytes("exit\n");
            /*localDataOutputStream.writeBytes("reboot\n");
            localDataOutputStream.writeBytes("exit\n");*/
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            LogHelper.Debug(ret + "");
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }

    @Override
    public void Install(String path) {
        File file=new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(Paras.appContext, "com.example.multimediav2.fileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        Paras.appContext.startActivity(intent);
    }

    @Override
    public void StatusBar() {

    }

    @Override
    public void StopUSB(boolean offOrOn) {

    }

    private PowerManager.WakeLock mWakeLock;
    synchronized private void getLock(Context context) {
        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Paras.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            } else {
                mWakeLock.acquire(300000);
            }
        }
        LogHelper.Debug("get lock");
    }

    synchronized private void releaseLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                LogHelper.Debug("release lock");
            }

            mWakeLock = null;
        }
    }

    /**
     * @param view 检测屏幕状态
     */
    public void checkScreen(View view) {
        PowerManager pm = (PowerManager) Paras.appContext.getSystemService(POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {//如果灭屏
            //相关操作
            LogHelper.Debug("屏幕是息屏");
        } else {
            LogHelper.Debug("屏幕是亮屏");

        }
    }


    /**
     * @param view 亮屏
     */
    @SuppressLint("InvalidWakeLockTag")
    public void checkScreenOn(View view) {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();
        mWakeLock.release();
    }

    /**
     * @param view 熄屏
     */
    public void checkScreenOff(View view) {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        policyManager.lockNow();
        if (admin) {
            policyManager.lockNow();
        } else {
            LogHelper.Debug("没有设备管理权限");
        }
    }
    /**
     * @param view 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager(View view) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二
        BaseActivity.currActivity.startActivityForResult(intent, 0);

    }
}

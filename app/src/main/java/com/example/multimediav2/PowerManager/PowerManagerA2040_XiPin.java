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

import androidx.core.content.FileProvider;

import com.example.multimediav2.BaseActivity;
import com.zcapi;

import java.io.File;
import java.util.Calendar;

import Modules.LogHelper;
import Modules.Paras;


public class PowerManagerA2040_XiPin extends BasePowerManager{
    private ComponentName adminReceiver;
    private PowerManager mPowerManager;
    private DevicePolicyManager policyManager;
    Context context;
    boolean isOpen=true;
    public PowerManagerA2040_XiPin(Context context) {

        this.context = context;
    }

    /*@Override
    public void ShutDown() {

        try {
            getLock(Paras.appContext);
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            //localDataOutputStream.writeBytes("echo 10000 > sys/class/rtc/rtc0/wakealarm\n");
            //localDataOutputStream.writeBytes("echo mem > /sys/power/state\n");

            localDataOutputStream.writeBytes("echo standby >/sys/power/state\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            LogHelper.Debug(ret + "");
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }*/
    @Override
    public void ShutDown(boolean checkScreen) {
        isOpen=false;
        Intent intent = new Intent("com.zc.zclcdoff");
        context.sendBroadcast(intent);
        Paras.volume=0;
    }

    /*@Override
    public void Open() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            localDataOutputStream.writeBytes("echo on > /sys/power/state\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            LogHelper.Debug(ret + "");
            releaseLock();
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
        //Reboot();
    }*/
    @Override
    public void Open() {
        isOpen=true;
        Paras.volume=100;
        Intent intent = new Intent("com.zc.zclcdon");
        context.sendBroadcast(intent);

    }

    /*@Override
    public void Reboot() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            localDataOutputStream.writeBytes("reboot\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            releaseLock();
            LogHelper.Debug(ret + "");
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }*/
    @Override
    public void Reboot() {
        isOpen=true;
        Intent intent = new Intent("wits.action.reboot");
        context.sendBroadcast(intent);
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
        //SilentInstallManager.silentInstallApk(Paras.appContext,path);

    }

    @Override
    public void StatusBar() {
        zcapi zcApi=new zcapi();
        zcApi.getContext(Paras.appContext);
        zcApi.setStatusBar(false);
    }

    @Override
    public void StopUSB(boolean offOrOn) {

    }

    @Override
    public boolean IsOpen()
    {
        return isOpen;
    }

    private PowerManager.WakeLock mWakeLock;
    synchronized private void getLock(Context context) {
        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Paras.class.getName());
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
        if (admin) {
            PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
            wakeLock.acquire();
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

package com.example.multimediav2.PowerManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.content.FileProvider;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Calendar;

import Modules.LogHelper;
import Modules.Paras;

public class PowerManagerDef extends BasePowerManager {
    @Override
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
    }

    @Override
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
    }

    @Override
    public void Reboot() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            localDataOutputStream.writeBytes("reboot\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            p.waitFor();
            int ret = p.exitValue();
            LogHelper.Debug(ret + "");
            releaseLock();
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
}

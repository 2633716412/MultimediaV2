package com.example.multimediav2.PowerManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.DataOutputStream;
import java.io.File;

import Modules.LogHelper;
import Modules.Paras;

public class PowerManagerDef extends BasePowerManager {
    @Override
    public void ShutDown() {

        try {
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
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }

    @Override
    public void Install(String path) {
        File file=new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(Paras.appContext, "com.example.multimediav2.fileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }

        Paras.appContext.startActivity(intent);
    }
}

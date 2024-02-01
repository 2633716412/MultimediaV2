package com.example.multimediav2.SystemTimeSetter;

import android.os.Build;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Modules.LogHelper;


public class SystemTimeSetterDef implements ISystemTimeSetter {

    @Override
    public void SetTime(Date date) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("MMddHHmmyyyy.ss", Locale.getDefault());
                String dateTime = format.format(date);
                String commend = "date " + dateTime + "\n busybox hwclock -w\n";
                execRootCmd(commend);
            } catch (Exception ex) {
                LogHelper.Error(ex.getMessage());
            }
        } else {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.getDefault());
                String dateTime = format.format(date);

                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("setprop persist.sys.timezone GMT\n");
                os.writeBytes("/system/bin/date -s " + dateTime + "\n");
                os.writeBytes("clock -w\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
                int ret = process.exitValue();

                LogHelper.Debug(" 设置系统时间 " + dateTime + " 结果=" + ret);
            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
        }
    }

    private int execRootCmd(String cmd) {

        Process process = null;
        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {

            LogHelper.Error(e);

            return -1;
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                process.destroy();
            } catch (Exception e) {
                LogHelper.Error("execRootCmd异常："+e.toString());
            }
        }
    }
}

package com.example.multimediav2.PowerManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.wits.serialport.SerialPort;

import java.io.File;
import java.io.OutputStream;

import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;

public class PowerManagerA2040 extends BasePowerManager {

    SerialPort serialPort;
    OutputStream mOutputStream;

    Context context;

    public PowerManagerA2040(Context context) {

        this.context = context;

        try {
            serialPort = new SerialPort(new File("/dev/ttyS2"), 9600, 0);
            mOutputStream = serialPort.getOutputStream();
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
    }

    @Override
    public void Handler() {

        if (InShutDwonTimeArea()) {
            try {
                opening = false;
                Paras.msgManager.SendMsg("A2040：预设时间已到，准备关机...");
                ShutDown();
            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
        }
    }


    public EDate GetNextOpenTime() {

        EDate now = EDate.Now();

        OSTime n = osTimes.get(0);
        int next = 7;

        for (int i = 0; i <= 7; i++) {

            n = osTimes.get((now.DayOfWeek() - 1 + i) % 7);

            if (!(n.close_hour == 0 && n.close_min == 0 && n.open_hour == 0 && n.open_min == 0)) {

                if (i == 0) {
                    EDate today_opentime = new EDate(now.Year(), now.Month(), now.Day(), n.open_hour, n.open_min, 0);
                    if (today_opentime.Sub(now).TotalSeconds() > 0) {
                        next = i;
                        break;
                    }
                } else {
                    next = i;
                    break;
                }
            }
        }

        EDate temp = now.AddDays(next);
        EDate nextOpenTime = new EDate(temp.Year(), temp.Month(), temp.Day(), n.open_hour, n.open_min, 0);
        return nextOpenTime;
    }

    private void writeOnTimeToMC(int flags, long times) {
        try {
            byte[] mBuffer = longToByteArray(flags, times);
            mOutputStream.write(mBuffer);
        } catch (Exception e) {
            LogHelper.Error(e);
        }
    }

    private static byte[] longToByteArray(int flags, long times) {
        byte[] result = new byte[9];
        result[0] = (byte) 0x00;
        result[1] = (byte) 0xaa;
        result[2] = (byte) 0xff;
        result[3] = (byte) 0x55;

        result[4] = (byte) (flags);

        result[5] = (byte) ((times >> 16) & 0xFF);
        result[6] = (byte) ((times >> 8) & 0xFF);
        result[7] = (byte) (times & 0xFF);

        result[8] = (byte) 0x55;

        return result;
    }

    @Override
    public void ShutDown() {

        EDate next = GetNextOpenTime();
        LogHelper.Debug("下次开机时间 " + next.ToString());

        long seconds = (next.Sub(EDate.Now())).TotalSeconds();

        if (seconds <= 0)
            seconds = 60;

        LogHelper.Debug(seconds + "秒后开机,现在关机");

        writeOnTimeToMC(1, seconds);
        Intent intent = new Intent("wits.action.shutdown");
        Paras.appContext.sendBroadcast(intent);
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
    public void Open() {

    }

    @Override
    public void Reboot() {
        Intent intent = new Intent("wits.action.reboot");
        Paras.appContext.sendBroadcast(intent);
    }
}

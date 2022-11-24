package com.example.multimediav2.PowerManager;

import android.content.Context;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;

abstract class BasePowerManager implements IPowerManager {

    List<OSTime> osTimes;

    volatile protected boolean opening = true;

    public boolean IsOpen() {
        return opening;
    }

    public BasePowerManager() {

        osTimes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            osTimes.add(new OSTime(i, 0, 0, 23, 59));
        }
    }

    public void SetTime(List<OSTime> osTimes) {
        this.osTimes = osTimes;
    }

    public void Handler() {

        boolean temp = opening;

        if (opening) {
            if (InShutDwonTimeArea()) {
                try {
                    opening = false;
                    LogHelper.Debug("预设时间已到，准备关机...");
                    Paras.msgManager.SendMsg("预设时间已到，准备关机...");
                    ShutDown();
                } catch (Exception ex) {
                    opening = temp;
                    LogHelper.Error(ex);
                }
            }
        } else {
            if (!InShutDwonTimeArea()) {
                try {
                    opening = true;
                    LogHelper.Debug("预设时间已到，准备开机...");
                    Paras.msgManager.SendMsg("预设时间已到，准备开机...");
                    Open();
                } catch (Exception ex) {
                    opening = temp;
                    LogHelper.Error(ex);
                }
            }
        }
    }

    public boolean InShutDwonTimeArea() {

        EDate now = EDate.Now();
        OSTime osTime = null;

        for (OSTime o : osTimes) {
            if (o.dayofweak == now.DayOfWeek()) {
                osTime = o;
                break;
            }
        }

        //未设置开关机策略，则什么都不做
        if (osTime == null)
            return false;

        EDate begin = new EDate(now.Year(), now.Month(), now.Day(), osTime.open_hour, osTime.open_min, 0);
        EDate end = new EDate(now.Year(), now.Month(), now.Day(), osTime.close_hour, osTime.close_min, 0);

        //开始时间>=结束时间时，什么都不做
        long b = begin.date.getTime();
        long e = end.date.getTime();

        if (b == e)
            return false;

        if (b > e) {
            long me = now.date.getTime();
            EDate hh00 = new EDate(now.date.getYear(), now.date.getMonth(), now.date.getDate(), 0, 0, 0);
            EDate hh24 = new EDate(now.date.getYear(), now.date.getMonth(), now.date.getDate(), 23, 59, 59);

            if ((me >= hh00.date.getTime() && me <= e) || (me >= b && me <= hh24.date.getTime())) {
                LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
                return false;
            } else {
                LogHelper.Debug("当前时间:" + now.ToString() + " 不在范围：" + begin.ToString() + " 至 " + end.ToString());
                return true;
            }
        }

        //开始时间<结束时间时，不在这个范围内就关机
        if (!now.Between(begin, end)) {
            LogHelper.Debug("当前时间:" + now.ToString() + " 不在范围：" + begin.ToString() + " 至 " + end.ToString());
            return true;
        }
        LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
        return false;
    }

    public class ListenThread extends Thread {

        boolean stop = false;

        public final static int SLEPP = Paras.time_loop_power * 1000;

        @Override
        public void run() {
            super.run();

            while (!stop) {

                try {
                    Handler();
                } catch (Exception ex) {
                    LogHelper.Error("开关机服务异常：" + ex);
                }

                try {
                    Thread.sleep(SLEPP);
                    LogHelper.Debug("开关机服务监听中...");
                } catch (Exception ex) {
                }
            }
        }
    }

    ListenThread listenThread;

    public void StartListen() {

        listenThread = new ListenThread();
        listenThread.start();
    }

    public void StopListen() {
        listenThread.stop = true;
    }

    public void setSystemTime(Context context) {

        try {
            String serverStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getTime"+"?device_id="+Paras.device_id);
            JSONObject obj= new JSONObject(serverStr);
            JSONObject dataObject = obj.getJSONObject("data");
            String time=dataObject.getString("time");
            //Date dateTime=new Date(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date serverTime = simpleDateFormat.parse(time);
            SimpleDateFormat format  = new SimpleDateFormat("yyyyMMdd.HHmmss");
            Date localTime = new Date();
            float min = Math.abs(localTime.getTime() - serverTime.getTime()) / 1000f / 60f;

            String t2 = simpleDateFormat.format(localTime);
            String updateTimeStr=format.format(serverTime);
            LogHelper.Debug("服务器=" + time + " ，本地时间=" + t2);

            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                format  = new SimpleDateFormat("MMddHHmmyyyy.ss");
                updateTimeStr=format.format(serverTime);
                localDataOutputStream.writeBytes("date " + updateTimeStr + " set \n");
                localDataOutputStream.writeBytes("busybox hwclock -w\n");
            } else {
                localDataOutputStream.writeBytes("setprop persist.sys.timezone GMT\n");
                localDataOutputStream.writeBytes("/system/bin/date -s " + updateTimeStr + "\n");
                localDataOutputStream.writeBytes("clock -w\n");
            }
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();

        } catch (Exception err) {
            LogHelper.Debug("系统时间被修改异常=" + err.toString());
        }
    }

    @Override
    public String GetName() {
        return "";
    }
}
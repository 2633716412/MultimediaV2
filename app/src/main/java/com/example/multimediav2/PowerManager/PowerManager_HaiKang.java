package com.example.multimediav2.PowerManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.hikvision.dmb.TimeSwitchConfig;
import com.hikvision.dmb.display.InfoDisplayApi;
import com.hikvision.dmb.system.InfoSystemApi;
import com.hikvision.dmb.time.InfoTimeApi;
import com.hikvision.dmb.util.InfoUtilApi;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Modules.DeviceData;
import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;

/***
 * 海康
 */
public class PowerManager_HaiKang implements IPowerManager {

    private Context context;
    private List<OSTime> osTimes;
    boolean isOpen=true;
    boolean offOrOn;
    public PowerManager_HaiKang(Context context) {
        this.context = context;
        InfoSystemApi.openAdb();
        InfoUtilApi.getRoot();
        final SPUnit spUnit = new SPUnit(context);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        offOrOn=deviceData.getStopUSB().equals("N");
        InfoUtilApi.setUsbSwitch(offOrOn);
        //判断监控程序是否在白名单
        boolean isInWhite=InfoUtilApi.isInWhiteList("com.nf.appmonitor");
        LogHelper.Debug("监控程序是否在白名单"+isInWhite);
        if (!isInWhite) {
            int whiteRes=InfoUtilApi.addWhiteList("com.nf.appmonitor");
            LogHelper.Debug("监控程序白名单添加结果"+whiteRes);
        }
        InfoSystemApi.setDeviceTestStatus(1);
        LogHelper.Debug("系统测试状态："+InfoSystemApi.getDeviceTestStatus());
        LogHelper.Debug("adb状态："+InfoSystemApi.getAdbStatus());
        osTimes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            osTimes.add(new OSTime(i, 0, 0, 23, 59));
        }
    }

    @Override
    public void Install(String path) {
        //更新的时候启动应用
        //InfoUtilApi.startUp("com.example.multimediav2","com.example.multimediav2.MainActivity");
        //使能
        //InfoUtilApi.enableProtection("com.example.multimediav2",true);
        Intent intent = Paras.appContext.getPackageManager().getLaunchIntentForPackage(Paras.appContext.getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(Paras.appContext, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) Paras.appContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10 * 1000, restartIntent);
        InfoUtilApi.silentInstallation(path);
        //InfoSystemApi.execCommand ("pm install -r "+path+" && am start com.example.multimediav2.MainActivity");
        //System.exit(0);
    }

    @Override
    public void StatusBar() {
        if(InfoDisplayApi.getStatusBarEnable()) {
            int statusBar=InfoDisplayApi.setStatusBarEnable(false);
            LogHelper.Debug("状态栏使能设置结果"+statusBar);
        }
        if(InfoDisplayApi.getNavigationBarEnable()) {
            int statusNav=InfoDisplayApi.setNavigationBarEnable(false);
            LogHelper.Debug("导航栏使能设置结果"+statusNav);
        }
        /*try {
            Thread.sleep(5000);
            int statusBar=InfoDisplayApi.setStatusBarEnable(false);
            LogHelper.Debug("状态栏使能设置结果"+statusBar);
            int statusNav=InfoDisplayApi.setNavigationBarEnable(false);
            LogHelper.Debug("导航栏使能设置结果"+statusNav);
        } catch (InterruptedException e) {
            LogHelper.Error("StatusBar异常："+e);
        }*/
    }

    @Override
    public void StopUSB(boolean offOrOn) {
        LogHelper.Debug("海康屏usb口"+offOrOn);
        int usbRes=InfoUtilApi.setUsbSwitch(offOrOn);
        LogHelper.Debug("海康屏usb使能设置结果"+usbRes);
    }

    @Override
    public boolean IsOpen() {
        if(InfoDisplayApi.getBacklightValue()<=0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void SetTime(List<OSTime> osTimes) {
        this.osTimes = osTimes;

        //海康每次开机，都要重新设置一次
        EDate now = EDate.Now();
        OSTime osTime = null;

        for (OSTime o : osTimes) {
            if (o.open_hour == 0 && o.open_min == 0 && o.close_hour == 23 && o.close_min == 59)
                continue;
            if (o.dayofweak == now.DayOfWeek()) {
                osTime = o;
                break;
            }
        }

        //未设置开关机策略，则什么都不做
        if (osTime == null)
            return;

        EDate begin = new EDate(now.Year(), now.Month(), now.Day(), osTime.open_hour, osTime.open_min, 0);
        EDate end = new EDate(now.Year(), now.Month(), now.Day(), osTime.close_hour, osTime.close_min, 0);

        long onTime = begin.date.getTime();
        long offTime = end.date.getTime();

        if (onTime == offTime)
            return;

        //String msg = "设置关机时间:" + end.ToString() + " " + offTime + "，开机时间：" + begin.ToString() + " " + onTime;
        String msg = "设置 关机时间：" + end.ToString() + "，开机时间：" + begin.ToString();
        LogHelper.Debug(msg);
        //Paras.msgManager.SendMsg(msg);
        setTimeSwitch(offTime, onTime);
    }

    /**
     * 获取定时开关机计划
     */
    public int setTimeSwitch(long offTime, long onTime) {
        TimeSwitchConfig config = InfoTimeApi.getTimeSwitch();
        config.isEnable = true;
        config.setOffTime = offTime;
        config.setOnTime = onTime;
        return InfoTimeApi.setTimeSwitch(offTime, onTime);
    }

    @Override
    public void ShutDown(boolean checkScreen) {
        isOpen=false;
        //息屏
        InfoDisplayApi.disableBacklight();
        //关机
        //InfoSystemApi.shutdown();
    }

    @Override
    public void Open() {
        isOpen=true;
        /*int res=InfoSystemApi.execCommand("sudo su\nfile /sys/power/state\ncat /sys/power/state\necho on > /sys/power/state\nexit\n");
        LogHelper.Debug("shell结果："+res);*/
        InfoDisplayApi.enableBacklight();
    }

    @Override
    public void Reboot() {
        isOpen=true;
        InfoSystemApi.reboot();
    }

    @Override
    public void setSystemTime(Context context) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getTime"+"?device_id="+Paras.device_id);
                        JSONObject obj= new JSONObject(serverStr);
                        JSONObject dataObject = obj.getJSONObject("data");
                        String time=dataObject.getString("time");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date serverTime = simpleDateFormat.parse(time);
                        Date localTime = new Date();
                        float min = Math.abs(localTime.getTime() - serverTime.getTime()) / 1000f / 60f;

                        String t1 = simpleDateFormat.format(serverTime);
                        String t2 = simpleDateFormat.format(localTime);

                        //String msg = "服务器时间=" + t1 + " 时间戳=" + serverTime.getTime() + " ，本地时间=" + t2;
                        String msg = "设置系统时间 当前服务器时间=" + t1 + " ，当前本地时间=" + t2;
                        LogHelper.Debug(msg);
                        Paras.msgManager.SendMsg(msg);
                        InfoTimeApi.setTime(serverTime.getTime());
                    } catch (Exception e) {
                        LogHelper.Error(e);
                    }
                }
            }).start();

        } catch (Exception err) {
            LogHelper.Debug("系统时间被修改异常=" + err.toString());
        }
    }

    @Override
    public void StartListen() {
    }

    @Override
    public void StopListen() {
    }

    @Override
    public String GetName() {
        return "HK" + InfoSystemApi.getSerialNumber();
    }
}

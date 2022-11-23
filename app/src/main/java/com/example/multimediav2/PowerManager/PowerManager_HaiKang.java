package com.example.multimediav2.PowerManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.hikvision.dmb.TimeSwitchConfig;
import com.hikvision.dmb.system.InfoSystemApi;
import com.hikvision.dmb.time.InfoTimeApi;
import com.hikvision.dmb.util.InfoUtilApi;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;

/***
 * 海康
 */
public class PowerManager_HaiKang implements IPowerManager {

    private Context context;
    private List<OSTime> osTimes;

    public PowerManager_HaiKang(Context context) {
        this.context = context;
        InfoSystemApi.openAdb();
        InfoUtilApi.getRoot();

        osTimes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            osTimes.add(new OSTime(i, 0, 0, 23, 59));
        }
    }

    @Override
    public void Install(String path) {
        Intent intent = Paras.appContext.getPackageManager().getLaunchIntentForPackage(Paras.appContext.getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(Paras.appContext, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) Paras.appContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 5 * 1000, restartIntent); // n秒后重启
        InfoUtilApi.silentInstallation(path);
        System.exit(0);
    }

    @Override
    public boolean IsOpen() {
        return false;
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
        Paras.msgManager.SendMsg(msg);
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
    public void ShutDown() {
        InfoSystemApi.shutdown();
    }

    @Override
    public void Open() {
    }

    @Override
    public void Reboot() {
        InfoSystemApi.reboot();
    }

    @Override
    public void setSystemTime(Context context) {
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

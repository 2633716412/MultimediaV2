package com.example.multimediav2.PowerManager;

import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.example.multimediav2.Utils.PollingUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;

public class PowerManager_HK6055 implements IPowerManager{
    private Context context;
    private List<OSTime> osTimes;
    private SmdtManager smdtManager;
    volatile protected boolean opening = true;

    public PowerManager_HK6055(Context context) {
        this.context = context;
        //使用 API
        smdtManager = SmdtManager.create(context);
        //看门狗
        smdtManager.smdtWatchDogEnable((char) 1);
        Thread dogThread=new Thread(new Runnable() {
            @Override
            public void run() {
                smdtManager.smdtWatchDogFeed ();
            }
        });
        if(!Paras.hasRun[3]) {
            PollingUtil pollingUtil=new PollingUtil(Paras.handler);
            pollingUtil.startPolling(dogThread,1500,true);
            Paras.hasRun[3]=true;
        }
        //打开usb调试模式
        //smdtManager.setUSBDebug(true);
        //打开adb调试模式
        //smdtManager. setNetworkDebug (true);
        //设置安装应用白名单，在禁止安装时白名单 APP 依旧可以安装
        //smdtManager.getNtpServer();
        osTimes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            osTimes.add(new OSTime(i, 0, 0, 23, 59));
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
        smdtManager.smdtSetStatusBar(Paras.appContext,false);
    }

    @Override
    public void StopUSB(boolean offOrOn) {
        /*LogHelper.Debug("海康屏usb口"+offOrOn);
        int usbRes1;
        int usbRes2;
        int usbRes3;
        int usbRes4;
        int usbRes5;
        int usbRes6;
        if(offOrOn) {
            usbRes1=smdtManager.smdtSetUsbPower(1,1,1);
            usbRes2=smdtManager.smdtSetUsbPower(1,2,1);
            usbRes3=smdtManager.smdtSetUsbPower(1,3,1);
            usbRes4=smdtManager.smdtSetUsbPower(2,1,1);
            usbRes5=smdtManager.smdtSetUsbPower(2,2,1);
            usbRes6=smdtManager.smdtSetUsbPower(2,3,1);
        } else {
            usbRes1=smdtManager.smdtSetUsbPower(1,1,0);
            usbRes2=smdtManager.smdtSetUsbPower(1,2,0);
            usbRes3=smdtManager.smdtSetUsbPower(1,3,0);
            usbRes4=smdtManager.smdtSetUsbPower(2,1,0);
            usbRes5=smdtManager.smdtSetUsbPower(2,2,0);
            usbRes6=smdtManager.smdtSetUsbPower(2,3,0);
        }

        LogHelper.Debug("海康屏usb使能设置结果"+usbRes1+usbRes2+usbRes3+usbRes4+usbRes5+usbRes6);*/
    }

    @Override
    public boolean IsOpen() {

        if(smdtManager.smdtGetLcdLightStatus()==1) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void SetTime(List<OSTime> osTimes) {
        this.osTimes = osTimes;
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
            //smdtManager.smdtSetTimingSwitchMachine ("20:30", "20:35","1");
            /*smdtManager.smdtSetPowerOnOff((char)0,(char)0,(char)0,(char)10,(char)3);
            smdtManager.smdtSetTimingSwitchMachine(end.getTimeString(),begin.getTimeString(),"1");*/
    }


    @Override
    public void ShutDown() {
        //息屏
        LogHelper.Debug("准备关机...");
        smdtManager.smdtSetLcdBackLight(0);
    }

    @Override
    public void Open() {
        //亮屏
        LogHelper.Debug("准备开机...");
        smdtManager.smdtSetLcdBackLight(1);
    }

    @Override
    public void Reboot() {

        smdtManager.smdtReboot("reboot");
    }

    @Override
    public void setSystemTime(Context context) {
        /*try {
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
                        smdtManager.setTime(context,serverTime.getYear(),serverTime.getMonth(),serverTime.getDay(),serverTime.getHours(),serverTime.getMinutes());
                    } catch (Exception e) {
                        LogHelper.Error(e);
                    }
                }
            }).start();

        } catch (Exception err) {
            LogHelper.Debug("系统时间被修改异常=" + err.toString());
        }*/
    }

    @Override
    public void StartListen() {
        ListenThread listenThread = new ListenThread();
        listenThread.start();
    }

    @Override
    public void StopListen() {
    }

    @Override
    public String GetName() {
        return "HK-6055" + smdtManager.getAndroidModel();
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
                    //LogHelper.Debug("开关机服务监听中...");
                } catch (Exception ex) {
                }
            }
        }
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
                if (Paras.lis_num==0) {
                    Paras.lis_num++;
                    int status = smdtManager.smdtGetLcdLightStatus();
                    LogHelper.Debug("当前屏幕状态："+status);
                    LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
                } else {
                    Paras.lis_num++;
                    if(Paras.lis_num>=5) {
                        Paras.lis_num=0;
                    }
                }
                return false;
            } else {
                if (Paras.lis_num==0) {
                    Paras.lis_num++;
                    int status = smdtManager.smdtGetLcdLightStatus();
                    LogHelper.Debug("当前屏幕状态："+status);
                    LogHelper.Debug("当前时间:" + now.ToString() + " 不在范围：" + begin.ToString() + " 至 " + end.ToString());
                } else {
                    Paras.lis_num++;
                    if(Paras.lis_num>=5) {
                        Paras.lis_num=0;
                    }
                }

                return true;
            }
        }

        //开始时间<结束时间时，不在这个范围内就关机
        if (!now.Between(begin, end)) {
            if (Paras.lis_num==0) {
                Paras.lis_num++;
                int status = smdtManager.smdtGetLcdLightStatus();
                LogHelper.Debug("当前屏幕状态："+status);
                LogHelper.Debug("当前时间:" + now.ToString() + " 不在范围：" + begin.ToString() + " 至 " + end.ToString());

            } else {
                Paras.lis_num++;
                if(Paras.lis_num>=5) {
                    Paras.lis_num=0;
                }
            }
            return true;
        }
        if (Paras.lis_num==0) {
            Paras.lis_num++;
            int status = smdtManager.smdtGetLcdLightStatus();
            LogHelper.Debug("当前屏幕状态："+status);
            LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
        } else {
            Paras.lis_num++;
            if(Paras.lis_num>=5) {
                Paras.lis_num=0;
            }
        }
        return false;
    }
}

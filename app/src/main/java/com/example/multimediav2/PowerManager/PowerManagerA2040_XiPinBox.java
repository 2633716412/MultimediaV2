package com.example.multimediav2.PowerManager;

import static android.content.Context.POWER_SERVICE;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.content.FileProvider;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.Models.MyBroadcastReceiver;
import com.zcapi;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Modules.DeviceData;
import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;

public class PowerManagerA2040_XiPinBox extends BasePowerManager{
    private ComponentName adminReceiver;
    private PowerManager mPowerManager;
    private DevicePolicyManager policyManager;
    private PowerManager.WakeLock wakeLock;
    zcapi zcApi=new zcapi();
    Context context;
    boolean isOpen=true;
    public PowerManagerA2040_XiPinBox(Context context) {
        this.context = context;
        zcApi.getContext(Paras.appContext);
        zcApi.setStatusBar(false);
        adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);
        mPowerManager=(PowerManager) Paras.appContext.getSystemService(POWER_SERVICE);
        policyManager=(DevicePolicyManager) Paras.appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (!admin) {
            checkAndTurnOnDeviceManager();
        }

    }

    @Override
    public void ShutDown(boolean checkScreen) {
        isOpen=false;
        /*Intent intent = new Intent("com.zc.zclcdoff");
        context.sendBroadcast(intent);*/
        if(checkScreen) {
            try {
                adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);
                mPowerManager=(PowerManager) Paras.appContext.getSystemService(POWER_SERVICE);
                policyManager=(DevicePolicyManager) Paras.appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                checkScreenOff();

                //Settings.System.putInt(Paras.appContext.getContentResolver(), "hdmi_enabled", 0);
            } catch (Exception ex) {
                LogHelper.Error("熄屏异常"+ex);
            }
        } else {
            Intent intent = new Intent("wits.action.shutdown");
            context.sendBroadcast(intent);
        }
        //zcApi.setLcdOnOff(false,1);
    }

    @Override
    public void Open() {
        isOpen=true;
//        Intent intent = new Intent("com.zc.zclcdon");
//        context.sendBroadcast(intent);
        /*Paras.volume=100;
        Intent intent = new Intent("wits.action.reboot");
        context.sendBroadcast(intent);*/
        try {
            checkScreenOn();

            //Settings.System.putInt(Paras.appContext.getContentResolver(), "hdmi_enabled", 0);
        } catch (Exception ex) {
            LogHelper.Error("亮屏异常"+ex);
        }

    }

    @Override
    public void Reboot() {
        isOpen=true;
        Intent intent = new Intent("wits.action.reboot");
        context.sendBroadcast(intent);
        Paras.volume=100;
    }

    @Override
    public boolean IsOpen()
    {
        return isOpen;
    }

    /**
     * 熄屏
     */
    public void checkScreenOff() {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
            wakeLock.acquire();
            policyManager.lockNow();
            wakeLock.release();
        } else {
            LogHelper.Debug("没有设备管理权限");
        }
    }

    /**
     * 亮屏
     */
    public void checkScreenOn() {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            //PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
            wakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakelockTag2");
            wakeLock.acquire();
            wakeLock.release();
        } else {
            LogHelper.Debug("没有设备管理权限");
        }
    }

    @Override
    public void SetTime(List<OSTime> osTimes) {
        this.osTimes = osTimes;
        zcApi.getContext(Paras.appContext);
        zcApi.setPowetOnOffTime(false,null,null);
        //每次开机，都要重新设置一次
        EDate now = EDate.Now();
        OSTime osTime = null;
        EDate openDate=null;
        EDate closeDate=null;
        for (OSTime o : osTimes) {
            if (o.open_hour == 0 && o.open_min == 0 && o.close_hour == 23 && o.close_min == 59)
                continue;
            if (o.dayofweak == now.DayOfWeek()) {
                osTime = o;
                //开机时间
                openDate = new EDate(now.Year(), now.Month(), now.Day(), osTime.open_hour, osTime.open_min, 0);
                closeDate = new EDate(now.Year(), now.Month(), now.Day(), osTime.close_hour, osTime.close_min, 0);

                //开机时间小于当前时间
                if(openDate.date.getTime()<=now.date.getTime()) {
                    //最后一天
                    if(osTimes.indexOf(o)==osTimes.size()-1) {
                        //第一天（循环的）
                        OSTime firstDate=osTimes.get(0);
                        int addDay=7+firstDate.dayofweak-o.dayofweak;
                        openDate=openDate.AddDays(addDay);
                        openDate = new EDate(openDate.Year(), openDate.Month(), openDate.Day(), firstDate.open_hour, firstDate.open_min, 0);

                    } else {
                        OSTime nextDate=osTimes.get(osTimes.indexOf(o)+1);
                        int addDay=nextDate.dayofweak-o.dayofweak;
                        openDate=openDate.AddDays(addDay);
                        openDate = new EDate(openDate.Year(), openDate.Month(), openDate.Day(), nextDate.open_hour, nextDate.open_min, 0);
                    }
                }
                //当关机时间小于当前时间
                if(closeDate.date.getTime()<=now.date.getTime()) {
                    closeDate=now.AddMins(2);
                }
                break;
            }
        }

        //未设置开关机策略，则什么都不做
        if (osTime == null)
            return;

        EDate begin = new EDate(openDate.Year(), openDate.Month(), openDate.Day(), openDate.date.getHours(), openDate.date.getMinutes(), 0);
        EDate end = new EDate(closeDate.Year(), closeDate.Month(), closeDate.Day(), closeDate.date.getHours(), closeDate.date.getMinutes(), 0);

        long onTimes = begin.date.getTime();
        long offTimes = end.date.getTime();

        if (onTimes == offTimes)
            return;

        //String msg = "设置关机时间:" + end.ToString() + " " + offTime + "，开机时间：" + begin.ToString() + " " + onTime;
        String msg = "设置 关机时间：" + end.ToString() + "，开机时间：" + begin.ToString();
        LogHelper.Debug(msg);
        /*String yearStr =now.ToString();
        int year = Integer.parseInt(yearStr.substring(0,4));*/
        int begin_year = Integer.parseInt(begin.ToString().substring(0,4));
        int end_year = Integer.parseInt(end.ToString().substring(0,4));
        int []onTime={begin_year,openDate.Month(),openDate.Day(),openDate.date.getHours(),openDate.date.getMinutes()};
        int []offTime={end_year,closeDate.Month(),closeDate.Day(),closeDate.date.getHours(),closeDate.date.getMinutes()};
        LogHelper.Debug("开机："+begin_year+"-"+openDate.Month()+"-"+openDate.Day()+" "+openDate.date.getHours()+openDate.date.getMinutes());
        LogHelper.Debug("关机："+end_year+"-"+closeDate.Month()+"-"+closeDate.Day()+" "+closeDate.date.getHours()+closeDate.date.getMinutes());
        zcApi.setPowetOnOffTime(true,onTime,offTime);
    }
    @Override
    public void StartListen() {
        Paras.executor.scheduleAtFixedRate(listenShutDown,5,30, TimeUnit.SECONDS);
    }
    @Override
    public void StopListen() {
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
        zcApi.setStatusBar(false);
    }

    @Override
    public void StopUSB(boolean offOrOn) {

    }

    /**
     * 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager() {
        ComponentName adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);;
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable USB blocking");
        //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二
        BaseActivity.currActivity.startActivityForResult(intent, 0);

    }

    public Runnable listenShutDown=new Runnable() {
        @Override
        public void run() {
            EDate now = EDate.Now();
            SPUnit spUnit = new SPUnit(context);
            DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
            OSTime osTime = null;
            EDate openDate=null;
            EDate closeDate=null;
            EDate willCloseDate=null;
            for (OSTime o : deviceData.getOsTimes()) {
                if (o.dayofweak == now.DayOfWeek()) {
                    openDate = new EDate(now.Year(), now.Month(), now.Day(), osTime.open_hour, osTime.open_min, 0);
                    closeDate = new EDate(now.Year(), now.Month(), now.Day(), osTime.close_hour, osTime.close_min, 0);
                    willCloseDate=closeDate.AddMins(-1);
                    //当开机时间小于关机时间情况下即将关机时重新设置开关机时间
                    if(now.Between(willCloseDate,closeDate)&&openDate.date.getTime()<closeDate.date.getTime()){
                        //最后一天
                        if(deviceData.getOsTimes().indexOf(o)==deviceData.getOsTimes().size()-1) {
                            //第一天（循环的）
                            OSTime firstDate=deviceData.getOsTimes().get(0);
                            int addDay=7+firstDate.dayofweak-o.dayofweak;
                            openDate=openDate.AddDays(addDay);
                            openDate = new EDate(openDate.Year(), openDate.Month(), openDate.Day(), firstDate.open_hour, firstDate.open_min, 0);

                        } else {
                            OSTime nextDate=osTimes.get(osTimes.indexOf(o)+1);
                            int addDay=nextDate.dayofweak-o.dayofweak;
                            openDate=openDate.AddDays(addDay);
                            openDate = new EDate(openDate.Year(), openDate.Month(), openDate.Day(), nextDate.open_hour, nextDate.open_min, 0);
                        }
                        int begin_year = Integer.parseInt(openDate.ToString().substring(0,4));
                        int end_year = Integer.parseInt(closeDate.ToString().substring(0,4));
                        int []onTime={begin_year,openDate.Month(),openDate.Day(),openDate.date.getHours(),openDate.date.getMinutes()};
                        int []offTime={end_year,closeDate.Month(),closeDate.Day(),closeDate.date.getHours(),closeDate.date.getMinutes()};
                        LogHelper.Debug("第二天开机时间："+begin_year+"-"+openDate.Month()+"-"+openDate.Day()+" "+openDate.date.getHours()+openDate.date.getMinutes());
                        LogHelper.Debug("关机："+end_year+"-"+closeDate.Month()+"-"+closeDate.Day()+" "+closeDate.date.getHours()+closeDate.date.getMinutes());
                        zcApi.setPowetOnOffTime(true,onTime,offTime);
                    }
                }
            }
        }
    };
}

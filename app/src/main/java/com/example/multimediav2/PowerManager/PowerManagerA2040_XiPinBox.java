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

import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;

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
        EDate secondDay=now.AddDays(1);
        OSTime osTime = null;
        OSTime secondOsTime = null;
        for (OSTime o : osTimes) {
            if (o.open_hour == 0 && o.open_min == 0 && o.close_hour == 23 && o.close_min == 59)
                continue;
            if (o.dayofweak == now.DayOfWeek()) {
                osTime = o;
                EDate nowDate = new EDate(now.Year(), now.Month(), now.Day(), osTime.open_hour, osTime.open_min, 0);
                if(nowDate.date.getTime()>now.date.getTime()) {
                    secondOsTime=osTime;
                    //secondDay=now;
                    break;
                }
                int index=osTimes.indexOf(0);
                for (OSTime o2 : osTimes) {
                    if (osTimes.indexOf(o2)>index) {
                        secondDay=now.AddDays(osTimes.indexOf(o2)-index);
                        secondOsTime = o2;
                        break;
                    }
                }
                break;
            }
        }

        //未设置开关机策略，则什么都不做
        if (osTime == null)
            return;
        if(secondOsTime==null) {
            secondOsTime=osTimes.get(0);
            int addDay=7+osTimes.get(0).dayofweak-osTime.dayofweak;
            secondDay=now.AddDays(addDay);
        }
        EDate begin = new EDate(secondDay.Year(), secondDay.Month(), secondDay.Day(), secondOsTime.open_hour, secondOsTime.open_min, 0);
        EDate end = new EDate(now.Year(), now.Month(), now.Day(), osTime.close_hour, osTime.close_min, 0);

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
        int []onTime={begin_year,secondDay.Month(),secondDay.Day(),secondOsTime.open_hour,secondOsTime.open_min};
        int []offTime={end_year,now.Month(),now.Day(),osTime.close_hour,osTime.close_min};
        LogHelper.Debug("开机："+secondDay.Year()+"-"+secondDay.Month()+"-"+secondDay.Day()+" "+secondOsTime.open_hour+secondOsTime.open_min);
        LogHelper.Debug("关机："+now.Year()+"-"+now.Month()+"-"+now.Day()+" "+osTime.close_hour+osTime.close_min);
        zcApi.setPowetOnOffTime(true,onTime,offTime);
    }
    @Override
    public void StartListen() {
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
}

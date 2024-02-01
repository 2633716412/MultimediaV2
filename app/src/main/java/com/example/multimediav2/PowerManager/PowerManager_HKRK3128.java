package com.example.multimediav2.PowerManager;

import android.content.Context;
import android.os.AsyncTask;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.FileUnit.FileUnitDef;
import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.ys.rkapi.MyManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Modules.DeviceData;
import Modules.EDate;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;

public class PowerManager_HKRK3128 implements IPowerManager {
    private Context context;
    private List<OSTime> osTimes;
    volatile protected boolean opening = true;
    private static MyManager  mMyManager;
    static FileUnitDef fileUnitDef;
    boolean offOrOn;
    public static String shutPath="";
    public PowerManager_HKRK3128(Context context) {
        this.context = context;

        initMyManager();
        /*mMyManager.bindAIDLService(Paras.appContext);
        mMyManager.setADBOpen(true);
        mMyManager.setConnectClickInterface(new MyManager.ServiceConnectedInterface() {
            @Override
            public void onConnect() {
                //回调成功,可以正常调用接口了
                //例如：mMyManager. getApiVersion();
            }
        });*/
        final SPUnit spUnit = new SPUnit(context);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        offOrOn=deviceData.getStopUSB().equals("N");
        StopUSB(offOrOn);
        osTimes = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            osTimes.add(new OSTime(i, 0, 0, 23, 59));
        }
    }
    private void initMyManager() {
        new MyManagerInitTask().execute();
    }

    private class MyManagerInitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            LogHelper.Debug("开始绑定服务");
            try {
                mMyManager = MyManager.getInstance(Paras.appContext);
                mMyManager.bindAIDLService(Paras.appContext);
            } catch (Exception e) {
                LogHelper.Error("绑定服务异常"+e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMyManager.setConnectClickInterface(new MyManager.ServiceConnectedInterface() {
                @Override
                public void onConnect() {
                    // 回调成功，可以正常调用接口了
                    LogHelper.Debug("绑定服务回调成功");
                    mMyManager.daemon("com.nf.appmonitor", 1);
                    mMyManager.setADBOpen(true);
                }
            });
        }
    }
    @Override
    public void Install(String path) {
        mMyManager = MyManager.getInstance(Paras.appContext);
        boolean success=mMyManager.silentInstallApk(path,true);
        LogHelper.Debug("安装结果："+success);
        /*File file=new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(Paras.appContext, "com.example.multimediav2.fileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }

        Paras.appContext.startActivity(intent);*/
    }

    @Override
    public void StatusBar() {
        mMyManager = MyManager.getInstance(Paras.appContext);
        mMyManager.setSlideShowNotificationBar(false);
        mMyManager.setSlideShowNavBar(false);
    }

    @Override
    public void StopUSB(boolean offOrOn) {
        LogHelper.Debug("海康屏usb口"+offOrOn);
        try {
            if(offOrOn) {
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream localDataOutputStream1 = new DataOutputStream(su.getOutputStream());

                    localDataOutputStream1.writeBytes("echo 1 > ./sys/devices/misc_power_en.19/host_vbus\n");
                    localDataOutputStream1.writeBytes("exit\n");
                    localDataOutputStream1.flush();
                    su.waitFor();
                    int ret1 = su.exitValue();
                    LogHelper.Debug("开启usb结果"+ret1);
                } catch (Exception var3) {
                    LogHelper.Error("开启usb失败"+var3);
                }
            } else {
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream localDataOutputStream1 = new DataOutputStream(su.getOutputStream());

                    localDataOutputStream1.writeBytes("echo 0 > ./sys/devices/misc_power_en.19/host_vbus\n");
                    localDataOutputStream1.writeBytes("exit\n");
                    localDataOutputStream1.flush();
                    su.waitFor();
                    int ret1 = su.exitValue();
                    LogHelper.Debug("禁用usb结果"+ret1);
                } catch (Exception var3) {
                    LogHelper.Error("禁用usb失败"+var3);
                }
            }
        } catch (Exception e) {
            LogHelper.Error("usb口禁用失败："+e);
        }


    }

    @Override
    public String GetName() {
        return null;
    }

    @Override
    public boolean IsOpen() {
        return opening;
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
            String msg = "设置 开机时间：" + begin.ToString() + "，关机时间：" + end.ToString();
            LogHelper.Debug(msg);
            Paras.msgManager.SendMsg(msg);

    }

    @Override
    public void ShutDown(boolean checkScreen) {
        //息屏
        opening=false;
        LogHelper.Debug("准备关机...");
        mMyManager = MyManager.getInstance(Paras.appContext);
        mMyManager.turnOffBackLight();
    }

    @Override
    public void Open() {
        //亮屏
        opening=true;
        LogHelper.Debug("准备开机...");
        mMyManager = MyManager.getInstance(Paras.appContext);
        mMyManager.turnOnBackLight();
    }

    @Override
    public void Reboot() {
        opening=true;
        LogHelper.Debug("准备重启...");
        mMyManager = MyManager.getInstance(Paras.appContext);
        mMyManager.reboot();
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
                        //LogHelper.Debug(msg);
                        //Paras.msgManager.SendMsg(msg);
                        mMyManager.setTime(serverTime.getYear(),serverTime.getMonth(),serverTime.getDay(),serverTime.getHours(),serverTime.getMinutes(),serverTime.getSeconds());
                    } catch (Exception e) {
                        LogHelper.Error(e);
                    }
                }
            }).start();

        } catch (Exception err) {
            LogHelper.Debug("系统时间被修改异常=" + err.toString());
        }
    }

    /*private static void shutScreen() {
        new shutScreenTask().execute();
    }

    private static class shutScreenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mMyManager.bindAIDLService(Paras.appContext);
            mMyManager.setConnectClickInterface(new MyManager.ServiceConnectedInterface() {
                @Override
                public void onConnect() {
                    // 回调成功，可以正常调用接口了

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }*/

    public static String Screenshot() {
        File fileSave = Paras.appContext.getExternalFilesDir("nf");
        String dir=fileSave.getPath();
        BaseActivity.deleteFile(dir,"jpg");
        try {
            fileUnitDef = new FileUnitDef();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String fn = formatter.format(new Date()) + ".jpg";
            shutPath=dir+fn;
            //mMyManager.bindAIDLService(Paras.appContext);
            //shutScreen();
            mMyManager = MyManager.getInstance(Paras.appContext);
            // 回调成功，可以正常调用接口了
            boolean res=mMyManager.takeScreenshot(shutPath);
            LogHelper.Debug("截屏结果："+res);
            /*byte[] bytes=BaseActivity.File2Bytes(file);
            file.delete();
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap bitmap1=BaseActivity.adjustPhotoRotation(bitmap,90);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            fileUnitDef.Save(dir,fn,stream.toByteArray());*/
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }
        return shutPath;
    }
    public void Handler() {

        boolean temp = opening;

        if (opening) {
            if (InShutDwonTimeArea()) {
                try {
                    opening = false;
                    LogHelper.Debug("预设时间已到，准备关机...");
                    Paras.msgManager.SendMsg("预设时间已到，准备关机...");
                    ShutDown(false);
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
                //LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
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
        //LogHelper.Debug("当前时间:" + now.ToString() + " 在范围：" + begin.ToString() + " 至 " + end.ToString());
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
                    //LogHelper.Debug("开关机服务监听中...");
                } catch (Exception ex) {
                }
            }
        }
    }

    ListenThread listenThread;

    @Override
    public void StartListen() {
        listenThread = new ListenThread();
        listenThread.setName("listenThread");
        boolean hasListenThread=false;
        Map<Thread, StackTraceElement[]> map = Thread.currentThread().getAllStackTraces();
        if (map != null && map.size() != 0) {
            Iterator keyIterator = map.keySet().iterator();
            while (keyIterator.hasNext()) {
                Thread eachThread = (Thread) keyIterator.next();
                if(Objects.equals(eachThread.getName(), listenThread.getName())) {
                    hasListenThread=true;
                }
            }
        }
        if(!hasListenThread) {
            listenThread.start();
        }
    }

    @Override
    public void StopListen() {

    }
}

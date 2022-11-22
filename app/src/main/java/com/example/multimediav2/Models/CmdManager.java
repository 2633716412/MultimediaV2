package com.example.multimediav2.Models;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.CacheServer.CacheServerFactory;
import com.example.multimediav2.FileUnit.FileUnitDef;
import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.PowerManager.PowerManagerFactory;
import com.example.multimediav2.Utils.AudioUtil;
import com.example.multimediav2.Utils.Base64FileUtil;
import com.example.multimediav2.Utils.PollingUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Modules.Action;
import Modules.DeviceData;
import Modules.LogHelper;
import Modules.MacUnit;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;
import Modules.TextSpeaker2;

public class CmdManager {
    static TextSpeaker2 textSpeaker2;
    static FileUnitDef fileUnitDef;
    static boolean firstShut=true;
    public void Init(final Context context, final Action<String> OnIniEnd) {
        Handler handler=new Handler();
        final SPUnit spUnit = new SPUnit(context);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);


        Paras.volume = 100;

        fileUnitDef = new FileUnitDef();
        Paras.name = deviceData.getDevice_name();
        Paras.cacheServer = CacheServerFactory.Get(context);
        Paras.devType=deviceData.device_type;
        deviceData.setMac(MacUnit.GetMac(context));
        //保存设备信息
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("id",deviceData.getId());
                    jsonObject.put("device_name",deviceData.getDevice_name());
                    jsonObject.put("device_ip",deviceData.getDevice_ip());
                    jsonObject.put("mac",deviceData.getMac());
                    jsonObject.put("os","");
                    String jsonStr= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/sava",jsonObject.toString());
                    JSONObject object= new JSONObject(jsonStr);
                    boolean res=object.getBoolean("success");
                    if(!res) {
                        LogHelper.Error("保存失败"+object.getString("msg"));
                    }
                    if(deviceData.getId()<=0) {
                        deviceData.setId(object.getLong("data"));
                        spUnit.Set("DeviceData",deviceData);
                    }
                } catch (Exception e) {
                    LogHelper.Error(e);
                }
            }
        });
        thread.start();
        try {
            textSpeaker2 = new TextSpeaker2(context);
        } catch (Exception ex) {
            LogHelper.Error("初始化语音异常：" + ex);
            Paras.msgManager.SendMsg("初始化语音异常：" + ex);
        }

        Paras.powerManager = PowerManagerFactory.Get();

        //设置当前时间的同时，设置开关机时间
        Paras.powerManager.SetTime(deviceData.osTimes);

        Thread task= new Thread(new Runnable() {

            @Override
            public void run() {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            /*while (deviceData.getId()>0) {*/
                            String jsonStr="";
                            try {
                                jsonStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getCmd"+"?device_id="+deviceData.getId());
                            } catch (Exception e) {
                                LogHelper.Error(e);
                            }
                            if(jsonStr!="") {
                                JSONObject object= new JSONObject(jsonStr);
                                JSONObject dataStr = object.getJSONObject("data");
                                String code = dataStr.getString("code");
                                String content=dataStr.getString("content");

                                JSONObject contentObject=new JSONObject();
                                if(!content.equals("")) {
                                    contentObject=new JSONObject(content);
                                }

                                //更新心跳时间
                                JSONObject updateObject=new JSONObject();
                                updateObject.put("device_id",deviceData.getId());
                                updateObject.put("is_record",1);
                                String updateRes= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/updateHeartTime",updateObject.toString());
                                if (!code.equals("")) {
                                    switch (code) {
                                        case "1002":
                                            Paras.powerManager.Open();
                                            break;
                                        case "1003":
                                            Paras.msgManager.SendMsg("正在准备关机...");
                                            Paras.powerManager.ShutDown();
                                            break;
                                        case "1004":
                                            Paras.msgManager.SendMsg("正在准备重启...");
                                            Paras.powerManager.Reboot();
                                            break;
                                        case "1005":
                                            LogHelper.Debug("截屏开始");
                                            Bitmap bmp = BaseActivity.Screenshot();
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                            String fn = formatter.format(new Date()) + ".jpg";
                                            //String dir = Environment.getExternalStorageDirectory() + "/nf";
                                            File fileSave = context.getExternalFilesDir("nf");
                                            String dir=fileSave.getPath();
                                            File file=new File(dir,fn);
                                            fileUnitDef.Save(dir, fn, stream.toByteArray());
                                            String base64Str = Base64FileUtil.encodeBase64File(file.getPath());
                                            JSONObject uploadObject=new JSONObject();
                                            uploadObject.put("device_id",deviceData.getId());
                                            uploadObject.put("fileFormat",".jpg");
                                            uploadObject.put("base64Str",base64Str);
                                            String res = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadFile",uploadObject.toString());
                                            JSONObject resObj= new JSONObject(res);
                                            if(!resObj.getBoolean("success")) {
                                                LogHelper.Error("截屏失败：" + dir + "/" + fn);
                                            }
                                            LogHelper.Debug("截屏完成：" + dir + "/" + fn);
                                            break;
                                        case "1006":
                                            Paras.volume = contentObject.getInt("volume");
                                            //spUnit.SetInt("UserVolume", Paras.volume);
                                            AudioUtil audioUtil=AudioUtil.getInstance(context);
                                            audioUtil.setMediaVolume(Paras.volume);
                                            Paras.msgManager.SendMsg("设置音量 = " + Paras.volume);
                                            break;
                                        case "1007":
                                            deviceData.setDevice_name(contentObject.getString("DeviceName"));
                                            spUnit.Set("DeviceData",deviceData);
                                            break;
                                        case "1008":
                                            try {
                                                Paras.msgManager.SendMsg("准备更新程序...");
                                                int endIndex=Paras.mulAPIAddr.lastIndexOf("/");
                                                String url=Paras.mulAPIAddr.substring(0,endIndex);
                                                JSONObject finalContentObject = contentObject;

                                                HttpUnitFactory.Get().DownLoad(url+contentObject.getString("filePath"), context.getExternalFilesDir("nf").getPath(), contentObject.getString("fileName"), new Action<Long>() {
                                                    @Override
                                                    public void Excute(Long value) {
                                                        //Paras.msgManager.SendMsg(String.format("已下载%sMB", value / 1024 / 1024));
                                                    }
                                                }, new Action() {
                                                    @Override
                                                    public void Excute(Object o) {
                                                        try {
                                                            String dir = context.getExternalFilesDir("nf").getPath();
                                                            String fn = finalContentObject.getString("fileName");
                                                            File file = new File(dir, fn);
                                                            if(!file.exists()) {
                                                                LogHelper.Error("更新包加载失败");
                                                            }
                                                            //Paras.msgManager.SendMsg("开始更新程序:" + dir + "/" + fn);
                                                            LogHelper.Debug("CMD1008 Path：" + dir + "/" + fn);
                                                            //更新包的方法
                                                            Paras.powerManager.Install(dir + "/" + fn);
                                                            Paras.msgManager.SendMsg("下载完成！");
                                                        } catch (Exception ex) {
                                                            LogHelper.Error("CMD1008(1) 更新程序异常：" + ex);
                                                        }
                                                    }
                                                });
                                            } catch (Exception ex) {
                                                LogHelper.Error("CMD1008(2) 异常：" + ex);
                                            }
                                            break;
                                        case "1009":
                                            Paras.msgManager.SendMsg("刷新节目");
                                            Paras.updateProgram=true;
                                            break;
                                        case "1010":
                                            String timeStr=contentObject.getString("Str");
                                            if(!timeStr.equals("")) {
                                                List<OSTime> list=new ArrayList<>();
                                                List<String> weekTime= Arrays.asList(timeStr.split("\\|"));
                                                for (String week:weekTime) {
                                                    OSTime dto=new OSTime();
                                                    List<String> times=Arrays.asList(week.split(","));
                                                    dto.setDayofweak(Integer.parseInt(times.get(0)));
                                                    List<String> startTime=Arrays.asList(times.get(1).split(":"));
                                                    dto.setOpen_hour(Integer.parseInt(startTime.get(0)));
                                                    dto.setOpen_min(Integer.parseInt(startTime.get(1)));
                                                    List<String> endTime=Arrays.asList(times.get(2).split(":"));
                                                    dto.setClose_hour(Integer.parseInt(endTime.get(0)));
                                                    dto.setClose_min(Integer.parseInt(endTime.get(1)));
                                                    list.add(dto);
                                                }
                                                deviceData.setOsTimes(list);
                                                Paras.powerManager.SetTime(list);
                                                spUnit.Set("DeviceData",deviceData);
                                            }
                                            break;
                                        case "1011":
                                            Paras.powerManager.setSystemTime(context);
                                        /*Date startTime=new Date();
                                        String serverStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getTime"+"?device_id="+deviceData.getId());
                                        Date endTime=new Date();
                                        JSONObject obj= new JSONObject(serverStr);
                                        JSONObject dataObject = obj.getJSONObject("data");
                                        long serverTime=dataObject.getLong("time");
                                        long sysTime=serverTime+(endTime.getTime()-startTime.getTime());
                                        SystemClock.setCurrentTimeMillis(sysTime);*/
                                            break;
                                        case "1012":
                                            InputStream inputStream = new FileInputStream(LogHelper.logFilePath);;
                                            InputStreamReader inputStreamReader=new InputStreamReader(inputStream, "utf-8");
                                            BufferedReader reader = new BufferedReader(inputStreamReader);
                                            StringBuilder sb = new StringBuilder("");
                                            String line;
                                            while ((line = reader.readLine()) != null) {
                                                sb.append(line);
                                                sb.append("\n");
                                            }
                                            String logContent=sb.toString();
                                            JSONObject logObject=new JSONObject();
                                            logObject.put("terminal_id",deviceData.getId());
                                            logObject.put("content",logContent);
                                            String result = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadLog",logObject.toString());
                                            JSONObject resultObj= new JSONObject(result);
                                            if(!resultObj.getBoolean("success")) {
                                                LogHelper.Error("日志提取失败：" + LogHelper.logFilePath);
                                            }
                                            //上传日志文件
                                        /*File logFile=new File(LogHelper.logFilePath);
                                        String base64LogStr = Base64FileUtil.encodeBase64File(logFile.getPath());
                                        JSONObject logObject=new JSONObject();
                                        logObject.put("device_id",deviceData.getId());
                                        logObject.put("fileFormat",".log");
                                        logObject.put("base64Str",base64LogStr);
                                        String result = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadFile",logObject.toString());
                                        JSONObject resultObj= new JSONObject(result);
                                        if(!resultObj.getBoolean("success")) {
                                            LogHelper.Error("日志提取失败：" + LogHelper.logFilePath);
                                        }*/
                                            LogHelper.Debug("日志提取完成：" + LogHelper.logFilePath);
                                            break;
                                        case "1013":
                                            String voiceTxt = contentObject.getString("VoiceData");
                                            Paras.msgManager.SendMsg("开始呼叫：" + voiceTxt);
                                            //TextSpeaker.Read(voiceTxt);
                                            textSpeaker2.read(voiceTxt);
                                            break;
                                    }
                                }
                            }
                   /* }
                    Thread.sleep(3000);*/
                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }
                }).start();

            }
        });

        PollingUtil pollingUtil=new PollingUtil(handler);
        pollingUtil.startPolling(task,3000,true);

        //task.start();

        //定时开关机监测
        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    Paras.powerManager.setSystemTime(context);
                    try {
                        Thread.sleep(Paras.time_start_listen_power * 1000);
                        Paras.powerManager.StartListen();
                    } catch (Exception ex) {
                        LogHelper.Error(ex);
                    }
                }
            }
        }).start();


        //每天的23:59:59获取一次日志
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = new FileInputStream(LogHelper.logFilePath);;
                    InputStreamReader inputStreamReader=new InputStreamReader(inputStream, "utf-8");
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuilder sb = new StringBuilder("");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    String logContent=sb.toString();
                    JSONObject logObject=new JSONObject();
                    logObject.put("terminal_id",deviceData.getId());
                    logObject.put("content",logContent);
                    String result = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadLog",logObject.toString());
                    JSONObject resultObj= new JSONObject(result);
                    if(!resultObj.getBoolean("success")) {
                        LogHelper.Error("日志提取失败：" + LogHelper.logFilePath);
                    }
                } catch (Exception e) {
                    LogHelper.Error(e);
                }
            }
        };

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long lastTime=calendar.getTime().getTime();
        long nowTime=new Date().getTime();
        long delay=lastTime-nowTime;
        timer.schedule(timerTask,delay ,85400000);

        //默认30分钟截屏一次

        Thread shutThread=new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            LogHelper.Debug("截屏开始");
                            Bitmap bmp = BaseActivity.Screenshot();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                            String fn = formatter.format(new Date()) + ".jpg";
                            File fileSave = Paras.appContext.getExternalFilesDir("nf");
                            String dir = fileSave.getPath();
                            File file = new File(dir, fn);
                            fileUnitDef.Save(dir, fn, stream.toByteArray());
                            String base64Str = Base64FileUtil.encodeBase64File(file.getPath());
                            JSONObject uploadObject = new JSONObject();
                            uploadObject.put("device_id", deviceData.getId());
                            uploadObject.put("fileFormat", ".jpg");
                            uploadObject.put("base64Str", base64Str);
                            String res = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadFile", uploadObject.toString());
                            JSONObject resObj = new JSONObject(res);
                            if (!resObj.getBoolean("success")) {
                                LogHelper.Error("截屏失败：" + dir + "/" + fn);
                            }
                            LogHelper.Debug("截屏完成：" + dir + "/" + fn);

                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }
                }).start();
            }
        });
        PollingUtil shutPolling=new PollingUtil(handler);
        shutPolling.startPolling(shutThread,1800000,true);
    }

    public static void openApplicationFromBackground(Context context) {
        Intent intent;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (!list.isEmpty() && list.get(0).topActivity.getPackageName().equals(context.getPackageName())) {
            //此时应用正在前台, 不作处理
            return;
        }
        /*for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(context.getPackageName())) {
                intent = new Intent();
                intent.setComponent(info.topActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (! (context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
                return;
            }
        }*/
        intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        context.startActivity(intent);
    }

    public static boolean isAppForeground(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        if (runningAppProcessInfoList==null){
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
            if (processInfo.processName.equals(context.getPackageName()) &&
                    processInfo.importance==ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                return true;
            }
        }
        return false;
    }

}

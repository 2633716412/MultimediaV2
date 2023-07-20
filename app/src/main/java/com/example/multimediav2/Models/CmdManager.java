package com.example.multimediav2.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.CacheServer.CacheServerFactory;
import com.example.multimediav2.FileUnit.FileUnitDef;
import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.PowerManager.PowerManagerFactory;
import com.example.multimediav2.Utils.AudioUtil;
import com.example.multimediav2.Utils.Base64FileUtil;
import com.example.multimediav2.Utils.PollingUtil;
import com.hikvision.dmb.display.InfoDisplayApi;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Init(final Context context, final Action<String> OnIniEnd) {
        //Handler handler=new Handler();
        final SPUnit spUnit = new SPUnit(context);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        Paras.volume = 100;
        fileUnitDef = new FileUnitDef();
        Paras.name = deviceData.getDevice_name();
        Paras.cacheServer = CacheServerFactory.Get(context);
        Paras.devType=deviceData.device_type;
        //获取海康白名单
        /*if(Objects.equals(deviceData.device_type, "hk")) {
            LogHelper.Debug(deviceData.device_type+Paras.appContext.getPackageName());
            int state=InfoUtilApi.setWhiteListState(true);
            if(state==0) {
                LogHelper.Debug("海康白名单功能开启成功");
            }
            int res=InfoUtilApi.addWhiteList("com.example.multimediav2");
            if(res==0) {
                LogHelper.Debug("白名单添加成功");
            }
            boolean isWhite=InfoUtilApi.isInWhiteList("com.example.multimediav2");
            LogHelper.Debug("是否在白名单"+isWhite);
        }*/
        //isIgnoringBatteryOptimizations();
        deviceData.setMac(MacUnit.GetMac(context));
        Paras.powerManager = PowerManagerFactory.Get();
        //保存设备信息
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped=false;
                while (!isStopped) {
                    try {
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("device_name",deviceData.getDevice_name());
                        jsonObject.put("device_ip",deviceData.getDevice_ip());
                        jsonObject.put("mac",deviceData.getMac());
                        String androidNumStr="Android "+ Build.VERSION.RELEASE;
                        jsonObject.put("os",androidNumStr);
                        jsonObject.put("sn",deviceData.getSn());
                        jsonObject.put("org_id",deviceData.getOrgId());
                        String jsonStr="";
                        try {
                            jsonStr= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/sava",jsonObject.toString());

                        } catch (Exception e) {
                            LogHelper.Error("保存设备异常："+e);
                        }
                        if(!Objects.equals(jsonStr, "")) {
                            isStopped=true;

                            JSONObject object= new JSONObject(jsonStr);
                            boolean res=object.getBoolean("success");
                            if(!res) {
                                LogHelper.Error("保存失败"+object.getString("msg"));
                            }
                            deviceData.setId(object.getLong("data"));
                            spUnit.Set("DeviceData",deviceData);
                        }
                    } catch (Exception e) {
                        LogHelper.Error(e);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                }

            }
        });
        thread.start();
        try {
            textSpeaker2 = new TextSpeaker2(Paras.appContext);
        } catch (Exception ex) {
            LogHelper.Error("初始化语音异常：" + ex);
            //Paras.msgManager.SendMsg("初始化语音异常：" + ex);
        }



        //设置当前时间的同时，设置开关机时间
        Paras.powerManager.SetTime(deviceData.osTimes);

        //心跳子线程
        PollingUtil pollingUtil=new PollingUtil(Paras.handler);
        Thread task= new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (!Objects.equals(deviceData.getSn(), "")) {
                                String jsonStr="";
                                //更新心跳时间
                                //Long device_status=Paras.powerManager.IsOpen()?0L:1L;
                                JSONObject updateObject=new JSONObject();
                                updateObject.put("sn",deviceData.getSn());
                                updateObject.put("is_record",1);
                                //updateObject.put("device_status",device_status);
                                String updateRes="";
                                try {

                                    updateRes= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/updateHeartTime",updateObject.toString());
                                } catch (Exception e) {
                                    /*LogHelper.Error("更新心跳时间异常："+e);
                                    Paras.msgManager.SendMsg("网络连接异常");*/
                                    if (Paras.fail_num==0) {
                                        Paras.fail_num++;
                                    } else {
                                        Paras.fail_num++;
                                        if(Paras.fail_num>=30) {
                                            Paras.fail_num=0;
                                            LogHelper.Error("更新心跳时间异常："+e);
                                            Paras.msgManager.SendMsg("网络连接异常");
                                            Paras.updateProgram=true;
                                        }
                                    }

                                }
                                if(!Objects.equals(updateRes, "")) {
                                    JSONObject timeObject= new JSONObject(updateRes);
                                    boolean res = timeObject.getBoolean("success");
                                    if(res) {
                                        Paras.fail_num=0;
                                        if (Paras.success_num==0) {
                                            LogHelper.Debug("更新心跳时间成功");
                                            Paras.success_num++;
                                        } else {
                                            Paras.success_num++;
                                            if(Paras.success_num>=60) {
                                                Paras.success_num=0;
                                            }
                                        }

                                    }
                                }
                                try {
                                    jsonStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getCmd"+"?sn="+deviceData.getSn());
                                } catch (Exception e) {
                                    LogHelper.Error("获取命令异常："+e);
                                }

                                if(!Objects.equals(jsonStr, "")) {
                                    JSONObject object= new JSONObject(jsonStr);
                                    JSONObject dataStr = object.getJSONObject("data");
                                    String code = dataStr.getString("code");
                                    String content=dataStr.getString("content");
                                    JSONObject contentObject=new JSONObject();
                                    if(!content.equals("")) {
                                        contentObject=new JSONObject(content);
                                    }
                                    if (!code.equals("")) {
                                        switch (code) {
                                            case "1001":
                                                LogHelper.Debug("下发节目");
                                                Paras.updateProgram=true;
                                                Paras.underUrl="";
                                                Paras.programUrl="";
                                                break;
                                            case "1002":
                                                LogHelper.Debug("开始开机");
                                                Paras.powerManager.Open();
                                                break;
                                            case "1003":
                                                Paras.msgManager.SendMsg("正在准备关机...");
                                                LogHelper.Debug("开始关机");
                                                Paras.powerManager.ShutDown();
                                                break;
                                            case "1004":
                                                Paras.msgManager.SendMsg("正在准备重启...");
                                                LogHelper.Debug("开始重启");
                                                Paras.powerManager.Reboot();
                                                break;
                                            case "1005":
                                                LogHelper.Debug("截屏开始");
                                                String base64Str="";
                                                String picPath=Paras.appContext.getExternalFilesDir("nf").getPath();
                                                if(Paras.HAI_KANG.equals(Paras.devType)) {
                                                    deleteFile(picPath,"jpg");
                                                    View dView = BaseActivity.currActivity.getWindow().getDecorView();
                                                    Bitmap bmp=InfoDisplayApi.screenShot(dView.getHeight(),dView.getWidth());
                                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                                    String fn = formatter.format(new Date()) + ".jpg";
                                                    File fileSave = context.getExternalFilesDir("nf");
                                                    String dir=fileSave.getPath();
                                                    File file=new File(dir,fn);
                                                    fileUnitDef.Save(dir, fn, stream.toByteArray());
                                                    base64Str = Base64FileUtil.encodeBase64File(file.getPath());
                                                    picPath+=fn;
                                                } else if(Paras.devType.equals(Paras.HAI_KANG_6055)) {
                                                    picPath = BaseActivity.HK6055Screenshot();
                                                    base64Str = Base64FileUtil.encodeBase64File(picPath);
                                                } else if(Paras.DEVA40_XiPin.equals(Paras.devType)) {
                                                    picPath = BaseActivity.A40XiPinScreenShot();
                                                    base64Str = Base64FileUtil.encodeBase64File(picPath);
                                                } else {
                                                    picPath = BaseActivity.Screenshot();
                                                    base64Str = Base64FileUtil.encodeBase64File(picPath);
                                                }
                                                JSONObject uploadObject=new JSONObject();
                                                uploadObject.put("device_id",deviceData.getId());
                                                uploadObject.put("fileFormat",".jpg");
                                                uploadObject.put("base64Str",base64Str);
                                                String res = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadFile",uploadObject.toString());
                                                JSONObject resObj= new JSONObject(res);
                                                if(!resObj.getBoolean("success")) {
                                                    LogHelper.Error("截屏失败：" + picPath);
                                                }
                                                LogHelper.Debug("截屏完成：" + picPath);
                                                break;
                                            case "1006":
                                                Paras.volume = contentObject.getInt("volume");
                                                //spUnit.SetInt("UserVolume", Paras.volume);
                                                AudioUtil audioUtil= AudioUtil.getInstance(context);
                                                audioUtil.setMediaVolume(Paras.volume);
                                                LogHelper.Debug("设置音量 = " + Paras.volume);
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
                                                    String dir1 = context.getExternalFilesDir("nf").getPath();
                                                    deleteFile(dir1,"apk");
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
                                                LogHelper.Debug("刷新节目");
                                                Paras.underUrl="";
                                                Paras.programUrl="";
                                                Paras.updateProgram=true;
                                                break;
                                            case "1010":
                                                LogHelper.Debug("设置开关机时间");
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
                                                        List<String> endTime= Arrays.asList(times.get(2).split(":"));
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
                                                LogHelper.Debug("开始同步时间");
                                                Paras.powerManager.setSystemTime(context);
                                                Date startTime=new Date();
                                                String serverStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getTime"+"?device_id="+deviceData.getId());
                                                Date endTime=new Date();
                                                JSONObject obj= new JSONObject(serverStr);
                                                JSONObject dataObject = obj.getJSONObject("data");
                                                long serverTime=dataObject.getLong("time");
                                                long sysTime=serverTime+(endTime.getTime()-startTime.getTime());
                                                SystemClock.setCurrentTimeMillis(sysTime);
                                                break;
                                            case "1012":
                                                InputStream inputStream = new FileInputStream(LogHelper.logFilePath);;
                                                InputStreamReader inputStreamReader=new InputStreamReader(inputStream, "utf-8");
                                                BufferedReader reader = new BufferedReader(inputStreamReader);
                                                StringBuilder sb = new StringBuilder("");
                                                String line;
                                                while ((line = reader.readLine()) != null) {
                                                    String lineStr=filterSpecialChars(line);
                                                    sb.append(lineStr);
                                                    if(!lineStr.equals("")) {
                                                        sb.append("\n");
                                                    }
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
                                                LogHelper.Debug("日志提取完成：" + LogHelper.logFilePath);
                                                DeleteFileDate();
                                                break;
                                            case "1013":
                                                //String voiceTxt = contentObject.getString("VoiceData");
                                                String voiceTxt=" 请张三到中医科室就诊";
                                                Paras.msgManager.SendMsg("开始呼叫：" + voiceTxt);
                                                LogHelper.Debug("开始呼叫：" + voiceTxt);
                                                textSpeaker2.read(voiceTxt);
                                                break;
                                            case "1014":
                                                String templateCode=contentObject.getString("templateCode");
                                                Long voiceVolume=contentObject.getLong("voiceVolume");
                                                String voiceDate=contentObject.getString("voiceData");
                                                Long voiceSpeed=contentObject.getLong("voiceSpeed");
                                                //Paras.msgManager.SendMsg("开始呼叫：" + voiceDate);
                                                LogHelper.Debug("开始呼叫：" + voiceDate);
                                                Paras.volume= Math.toIntExact(voiceVolume);
                                                //textSpeaker2.setSpeed(voiceSpeed);
                                                textSpeaker2.read(voiceDate);
                                                break;
                                            case "1015":
                                                /*DevicePolicyManager devicePolicyManager = (DevicePolicyManager) Paras.appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                                ComponentName adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);
                                                boolean admin = devicePolicyManager.isAdminActive(adminReceiver);
                                                if (devicePolicyManager != null && admin) {
                                                    devicePolicyManager.addUserRestriction(adminReceiver, UserManager.DISALLOW_USB_FILE_TRANSFER);
                                                }*/
                                                /*Intent intentOff = new Intent("android.hardware.usb.action.USB_STATE");
                                                intentOff.putExtra("connected", false);
                                                BaseActivity.currActivity.sendBroadcast(intentOff);*/
                                                Process p = Runtime.getRuntime().exec("su");
                                                DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());

                                                localDataOutputStream.writeBytes("echo 0 > /sys/class/android_usb/android0/enable\n");
                                                localDataOutputStream.writeBytes("exit\n");
                                                localDataOutputStream.flush();
                                                p.waitFor();
                                                int ret = p.exitValue();
                                                LogHelper.Debug(ret + "");
                                                break;
                                            case "1016":
                                                /*Intent intentOn = new Intent("android.hardware.usb.action.USB_STATE");
                                                intentOn.putExtra("connected", true);
                                                BaseActivity.currActivity.sendBroadcast(intentOn);*/
                                                Process p1 = Runtime.getRuntime().exec("su");
                                                DataOutputStream localDataOutputStream1 = new DataOutputStream(p1.getOutputStream());

                                                localDataOutputStream1.writeBytes("echo 1 > /sys/class/android_usb/android0/enable\n");
                                                localDataOutputStream1.writeBytes("exit\n");
                                                localDataOutputStream1.flush();
                                                p1.waitFor();
                                                int ret1 = p1.exitValue();
                                                LogHelper.Debug(ret1 + "");
                                                break;
                                            case "1017":
                                                int streamType=contentObject.getInt("streamType");
                                                deviceData.setStream_type(streamType);
                                                spUnit.Set("DeviceData",deviceData);
                                                textSpeaker2=new TextSpeaker2(Paras.appContext);
                                                LogHelper.Debug("语音类型调整为："+streamType);
                                                break;
                                            case "1033":
                                                File logFile=new File(LogHelper.logFilePath);
                                                FileWriter fileWriter=new FileWriter(logFile);
                                                fileWriter.write("");
                                                fileWriter.flush();
                                                fileWriter.close();
                                                LogHelper.Debug("日志清理完毕");
                                                break;
                                            default:break;
                                        }
                                    }
                                    else {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                            //Thread.sleep(Paras.heart_time*1000);
                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }
                }).start();
            }
        });
        if(!Paras.hasRun[0]) {
            pollingUtil.startPolling(task,Paras.heart_time*1000,true);
            Paras.hasRun[0]=true;
        }
        /*MyThread myThread=new MyThread();
        if(!Paras.hasRun[0]) {
            pollingUtil.startPolling(myThread,Paras.heart_time*1000,true);
            Paras.hasRun[0]=true;
        }*/
        //定时开关机监测
        Thread listenThread=new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    Paras.powerManager.setSystemTime(context);
                    try {
                        //Thread.sleep(Paras.time_start_listen_power * 1000);
                        Paras.powerManager.StartListen();
                    } catch (Exception ex) {
                        LogHelper.Error(ex);
                    }
                }
            }
        });
        if(!Paras.hasRun[1]) {
            pollingUtil.startPolling(listenThread,Paras.time_start_listen_power * 1000,false);
            Paras.hasRun[1]=true;
        }

        /*listenThread.setName("listenThread");
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
        }*/
        //每天的23:59:50获取一次日志
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
        calendar.set(Calendar.SECOND, 50);
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
                            Thread.sleep(5000);
                            SPUnit spUnit = new SPUnit(Paras.appContext);
                            DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                            LogHelper.Debug("截屏开始");
                            String base64Str="";
                            String picPath=Paras.appContext.getExternalFilesDir("nf").getPath();
                            if(Paras.HAI_KANG.equals(Paras.devType)) {
                                deleteFile(picPath,"jpg");
                                View dView = BaseActivity.currActivity.getWindow().getDecorView();
                                Bitmap bmp= InfoDisplayApi.screenShot(dView.getHeight(),dView.getWidth());
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                String fn = formatter.format(new Date()) + ".jpg";
                                File fileSave = Paras.appContext.getExternalFilesDir("nf");
                                String dir=fileSave.getPath();
                                File file=new File(dir,fn);
                                fileUnitDef.Save(dir, fn, stream.toByteArray());
                                base64Str = Base64FileUtil.encodeBase64File(file.getPath());
                                picPath+=fn;
                            } else if(Paras.devType.equals(Paras.HAI_KANG_6055)) {
                                picPath = BaseActivity.HK6055Screenshot();
                                base64Str = Base64FileUtil.encodeBase64File(picPath);
                            } else if(Paras.DEVA40_XiPin.equals(Paras.devType)) {
                                picPath = BaseActivity.A40XiPinScreenShot();
                                base64Str = Base64FileUtil.encodeBase64File(picPath);
                            } else {
                                picPath = BaseActivity.Screenshot();
                                base64Str = Base64FileUtil.encodeBase64File(picPath);
                            }
                            JSONObject uploadObject=new JSONObject();
                            uploadObject.put("device_id",deviceData.getId());
                            uploadObject.put("fileFormat",".jpg");
                            uploadObject.put("base64Str",base64Str);
                            String res = HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/uploadFile",uploadObject.toString());
                            JSONObject resObj= new JSONObject(res);
                            if(!resObj.getBoolean("success")) {
                                LogHelper.Error("截屏失败：" + picPath);
                            }
                            LogHelper.Debug("截屏完成：" + picPath);
                        } catch (Exception e) {
                            LogHelper.Error("截屏失败："+e.getMessage());
                        }
                    }
                }).start();
            }
        });
        if(!Paras.hasRun[2]) {
            pollingUtil.startPolling(shutThread,1800000,true);
            Paras.hasRun[2]=true;
        }
    }
    public static void deleteFile(String path,String type) {
        File file = new File(path);
        // 获取当前目录下的目录和文件
        File[] listFiles = file.listFiles();
        for (File f:listFiles) {
            //判断是否是目录
            if (f.isDirectory()) {
                //是目录，进入目录继续删除
                String path2 = f.getPath();
                deleteFile(path2,"");
            }else {
                //符合文件类型 调用delete()方法删除
                String fileType = f.getName().substring(f.getName().lastIndexOf(".")+1);
                if(fileType.equals(type)){
                    LogHelper.Debug("删除文件：" + f.getAbsolutePath());
                    f.delete();
                }
            }
        }
    }
    //删除7天以上的日志文件
    public void DeleteFileDate() {
        String logFilePath = Paras.appContext.getExternalFilesDir(null)
                + File.separator + "nf" + File.separator + "logs"
                + File.separator;
        File dirPath = new File(logFilePath);
        if (dirPath.exists()) { //文件或文件夹是否存在
            if (dirPath.isDirectory()) { //判断是不是目录
                //得到文件里面全部的文件及文件夹
                File[] files = dirPath.listFiles();
                assert files != null;
                for (File f : files) {
                    //得到绝对路径下的文件及文件夹
                    File absFile = f.getAbsoluteFile();
                    long currTime = System.currentTimeMillis(); //当前时间
                    long lastTime = absFile.lastModified(); //文件被最后一次修改的时间
                    long diffen = currTime - lastTime;
                    if (diffen > 7 * 24 * 60 * 60 * 1000) { // 删除大于7天的文件
                        LogHelper.Debug("删除日志" + absFile.getName());
                        absFile.delete();
                    }
                }
            }
        }
    }
    //判断是否在白名单
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) Paras.appContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(Paras.appContext.getPackageName());
        }
        LogHelper.Debug("是否在白名单："+isIgnoring);
        return isIgnoring;
    }

    public static String filterSpecialChars(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        String pattern = "[^a-zA-Z0-9\\u4E00-\\u9FA5\\s\\[\\]\\{\\}\\(\\),\\\"':./\\\\-]"; // 只允许字母、数字和中文
        return str.replaceAll(pattern, "");
    }

    /*public class UsbSetting {
        final private static String TAG = "UsbSetting";
        public  void AllowUseUsb() {    //允许使用USB
            Command.command("setprop persist.sys.usb.config mtp,adb");
        }
        public  void DisallowUseUsb() {   //禁止使用USB
            Command.command("setprop persist.sys.usb.config none");
        }
    }

    public static class Command {
        final private static String TAG = "Command";
        public static void command(String com) {
            try {
                Log.i(TAG, "Command : " + com);
                Runtime.getRuntime().exec(com);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }*/
}

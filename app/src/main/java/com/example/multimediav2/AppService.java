package com.example.multimediav2;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.example.multimediav2.FileUnit.FileUnitDef;
import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Models.CmdManager;
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
import java.util.Objects;

import Modules.Action;
import Modules.DeviceData;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;
import Modules.TextSpeaker2;

public class AppService extends Service
{
    private static final String TAG = "AppService";
    private static final long RESTART_DELAY = 10 * 1000; // 多少时间后重启检测(1小时)
    private MyBinder mBinder;

    // 此对象用于绑定的service与调用者之间的通信
    public class MyBinder extends Binder {

        /**
         * 获取service实例
         * @return
         */
        public AppService getService() {
            return AppService.this;
        }

        /**
         * 启动app重启任务
         */
        public void startTask(final Context context,DeviceData deviceData,Handler handler,SPUnit spUnit,FileUnitDef fileUnitDef,TextSpeaker2 textSpeaker2) {
            Thread task= new Thread(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (deviceData.getId()>0) {
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
                                        if(!Objects.equals(updateRes, "")) {
                                            JSONObject timeObject= new JSONObject(updateRes);
                                            boolean res = timeObject.getBoolean("success");
                                            if(res) {
                                                LogHelper.Debug("更新心跳时间成功");
                                            }
                                        }
                                        if (!code.equals("")) {
                                            switch (code) {
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
                                                    Bitmap bmp = BaseActivity.Screenshot();
                                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                                    String fn = formatter.format(new Date()) + ".jpg";
                                                    //String dir = Environment.getExternalStorageDirectory() + "/nf";
                                                    File fileSave = Paras.appContext.getExternalFilesDir("nf");
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
                                                    AudioUtil audioUtil=AudioUtil.getInstance(Paras.appContext);
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

                                                        HttpUnitFactory.Get().DownLoad(url+contentObject.getString("filePath"), Paras.appContext.getExternalFilesDir("nf").getPath(), contentObject.getString("fileName"), new Action<Long>() {
                                                            @Override
                                                            public void Excute(Long value) {
                                                                //Paras.msgManager.SendMsg(String.format("已下载%sMB", value / 1024 / 1024));
                                                            }
                                                        }, new Action() {
                                                            @Override
                                                            public void Excute(Object o) {
                                                                try {
                                                                    String dir = Paras.appContext.getExternalFilesDir("nf").getPath();
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
                                                    LogHelper.Debug("开始同步时间");
                                                    Paras.powerManager.setSystemTime(Paras.appContext);
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
                                                    LogHelper.Debug("日志提取完成：" + LogHelper.logFilePath);
                                                    break;
                                                case "1013":
                                                    String voiceTxt = contentObject.getString("VoiceData");
                                                    Paras.msgManager.SendMsg("开始呼叫：" + voiceTxt);
                                                    LogHelper.Debug("开始呼叫：" + voiceTxt);
                                                    //TextSpeaker.Read(voiceTxt);
                                                    //textSpeaker2.speak(voiceTxt);
                                                    textSpeaker2.read(voiceTxt);
                                                    break;
                                                default:break;
                                            }
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogHelper.Error(e);
                            }
                        }
                    }).start();

                }
            });

            PollingUtil pollingUtil=new PollingUtil(handler);
            pollingUtil.startPolling(task,4000,true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Create MyBinder object
        if (mBinder == null) {
            mBinder = new MyBinder();
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    private PowerManager.WakeLock mWakeLock;
    @Override
    public void onCreate() {
        getLock(Paras.appContext);
        Paras.volume = 100;
    }

    @Override
    public void onDestroy() {
        releaseLock();
        super.onDestroy();
    }


    synchronized private void getLock(Context context) {
        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CmdManager.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            } else {
                mWakeLock.acquire(300000);
            }
        }
        LogHelper.Debug("get lock");
    }

    synchronized private void releaseLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                LogHelper.Debug("release lock");
            }

            mWakeLock = null;
        }
    }
}

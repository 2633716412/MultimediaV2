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
        //?????????????????????
        /*if(Objects.equals(deviceData.device_type, "hk")) {
            LogHelper.Debug(deviceData.device_type+Paras.appContext.getPackageName());
            int state=InfoUtilApi.setWhiteListState(true);
            if(state==0) {
                LogHelper.Debug("?????????????????????????????????");
            }
            int res=InfoUtilApi.addWhiteList("com.example.multimediav2");
            if(res==0) {
                LogHelper.Debug("?????????????????????");
            }
            boolean isWhite=InfoUtilApi.isInWhiteList("com.example.multimediav2");
            LogHelper.Debug("??????????????????"+isWhite);
        }*/
        //isIgnoringBatteryOptimizations();
        deviceData.setMac(MacUnit.GetMac(context));
        //??????????????????
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
                            LogHelper.Error("?????????????????????"+e);
                        }
                        if(!Objects.equals(jsonStr, "")) {
                            isStopped=true;
                            JSONObject object= new JSONObject(jsonStr);
                            boolean res=object.getBoolean("success");
                            if(!res) {
                                LogHelper.Error("????????????"+object.getString("msg"));
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
            LogHelper.Error("????????????????????????" + ex);
            Paras.msgManager.SendMsg("????????????????????????" + ex);
        }

        Paras.powerManager = PowerManagerFactory.Get();

        //???????????????????????????????????????????????????
        Paras.powerManager.SetTime(deviceData.osTimes);

        //???????????????

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
                                //??????????????????
                                JSONObject updateObject=new JSONObject();
                                updateObject.put("sn",deviceData.getSn());
                                updateObject.put("is_record",1);
                                String updateRes="";
                                try {
                                    updateRes= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/updateHeartTime",updateObject.toString());
                                } catch (Exception e) {
                                    LogHelper.Error("???????????????????????????"+e);
                                    Paras.updateProgram=true;
                                }
                                if(!Objects.equals(updateRes, "")) {
                                    JSONObject timeObject= new JSONObject(updateRes);
                                    boolean res = timeObject.getBoolean("success");
                                    if(res) {
                                        if (Paras.num==0) {
                                            LogHelper.Debug("????????????????????????");
                                            Paras.num++;
                                            if(Paras.num>=5) {
                                                Paras.num=0;
                                            }
                                        }

                                    }
                                }
                                try {
                                    jsonStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getCmd"+"?sn="+deviceData.getSn());
                                } catch (Exception e) {
                                    LogHelper.Error("?????????????????????"+e);
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
                                            case "1002":
                                                LogHelper.Debug("????????????");
                                                Paras.powerManager.Open();
                                                break;
                                            case "1003":
                                                Paras.msgManager.SendMsg("??????????????????...");
                                                LogHelper.Debug("????????????");
                                                Paras.powerManager.ShutDown();
                                                break;
                                            case "1004":
                                                Paras.msgManager.SendMsg("??????????????????...");
                                                LogHelper.Debug("????????????");
                                                Paras.powerManager.Reboot();
                                                break;
                                            case "1005":
                                                LogHelper.Debug("????????????");
                                                String base64Str="";
                                                String picPath=Paras.appContext.getExternalFilesDir("nf").getPath();
                                                if("hk".equals(Paras.devType)) {
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
                                                    LogHelper.Error("???????????????" + picPath);
                                                }
                                                LogHelper.Debug("???????????????" + picPath);
                                                break;
                                            case "1006":
                                                Paras.volume = contentObject.getInt("volume");
                                                //spUnit.SetInt("UserVolume", Paras.volume);
                                                AudioUtil audioUtil= AudioUtil.getInstance(context);
                                                audioUtil.setMediaVolume(Paras.volume);
                                                LogHelper.Debug("???????????? = " + Paras.volume);
                                                Paras.msgManager.SendMsg("???????????? = " + Paras.volume);
                                                break;
                                            case "1007":
                                                deviceData.setDevice_name(contentObject.getString("DeviceName"));
                                                spUnit.Set("DeviceData",deviceData);
                                                break;
                                            case "1008":
                                                try {
                                                    Paras.msgManager.SendMsg("??????????????????...");
                                                    int endIndex=Paras.mulAPIAddr.lastIndexOf("/");
                                                    String url=Paras.mulAPIAddr.substring(0,endIndex);
                                                    JSONObject finalContentObject = contentObject;
                                                    String dir1 = context.getExternalFilesDir("nf").getPath();
                                                    deleteFile(dir1,"apk");
                                                    HttpUnitFactory.Get().DownLoad(url+contentObject.getString("filePath"), context.getExternalFilesDir("nf").getPath(), contentObject.getString("fileName"), new Action<Long>() {
                                                        @Override
                                                        public void Excute(Long value) {
                                                            //Paras.msgManager.SendMsg(String.format("?????????%sMB", value / 1024 / 1024));
                                                        }
                                                    }, new Action() {
                                                        @Override
                                                        public void Excute(Object o) {
                                                            try {
                                                                String dir = context.getExternalFilesDir("nf").getPath();
                                                                String fn = finalContentObject.getString("fileName");
                                                                File file = new File(dir, fn);
                                                                if(!file.exists()) {
                                                                    LogHelper.Error("?????????????????????");
                                                                }
                                                                //Paras.msgManager.SendMsg("??????????????????:" + dir + "/" + fn);
                                                                LogHelper.Debug("CMD1008 Path???" + dir + "/" + fn);
                                                                //??????????????????
                                                                Paras.powerManager.Install(dir + "/" + fn);
                                                                Paras.msgManager.SendMsg("???????????????");
                                                            } catch (Exception ex) {
                                                                LogHelper.Error("CMD1008(1) ?????????????????????" + ex);
                                                            }
                                                        }
                                                    });
                                                } catch (Exception ex) {
                                                    LogHelper.Error("CMD1008(2) ?????????" + ex);
                                                }
                                                break;
                                            case "1009":
                                                Paras.msgManager.SendMsg("????????????");
                                                LogHelper.Debug("????????????");
                                                Paras.updateProgram=true;
                                                break;
                                            case "1010":
                                                LogHelper.Debug("?????????????????????");
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
                                                LogHelper.Debug("??????????????????");
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
                                                    LogHelper.Error("?????????????????????" + LogHelper.logFilePath);
                                                }
                                                LogHelper.Debug("?????????????????????" + LogHelper.logFilePath);
                                                DeleteFileDate();
                                                break;
                                            case "1013":
                                                //String voiceTxt = contentObject.getString("VoiceData");
                                                String voiceTxt=" ??????????????????????????????";
                                                Paras.msgManager.SendMsg("???????????????" + voiceTxt);
                                                LogHelper.Debug("???????????????" + voiceTxt);
                                                textSpeaker2.read(voiceTxt);
                                                break;
                                            case "1014":
                                                String templateCode=contentObject.getString("templateCode");
                                                Long voiceVolume=contentObject.getLong("voiceVolume");
                                                String voiceDate=contentObject.getString("voiceData");
                                                Long voiceSpeed=contentObject.getLong("voiceSpeed");
                                                Paras.msgManager.SendMsg("???????????????" + voiceDate);
                                                Paras.volume= Math.toIntExact(voiceVolume);
                                                //textSpeaker2.setSpeed(voiceSpeed);
                                                textSpeaker2.read(voiceDate);
                                                break;
                                            case "1033":
                                                File logFile=new File(LogHelper.logFilePath);
                                                FileWriter fileWriter=new FileWriter(logFile);
                                                fileWriter.write("");
                                                fileWriter.flush();
                                                fileWriter.close();
                                                LogHelper.Debug("??????????????????");
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
        //?????????????????????
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
        //?????????23:59:50??????????????????
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
                        LogHelper.Error("?????????????????????" + LogHelper.logFilePath);
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

        //??????30??????????????????

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
                            LogHelper.Debug("????????????");
                            String base64Str="";
                            String picPath=Paras.appContext.getExternalFilesDir("nf").getPath();
                            if("hk".equals(Paras.devType)) {
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
                                LogHelper.Error("???????????????" + picPath);
                            }
                            LogHelper.Debug("???????????????" + picPath);
                        } catch (Exception e) {
                            LogHelper.Error("???????????????"+e.getMessage());
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
        // ???????????????????????????????????????
        File[] listFiles = file.listFiles();
        for (File f:listFiles) {
            //?????????????????????
            if (f.isDirectory()) {
                //????????????????????????????????????
                String path2 = f.getPath();
                deleteFile(path2,"");
            }else {
                //?????????????????? ??????delete()????????????
                String fileType = f.getName().substring(f.getName().lastIndexOf(".")+1);
                if(fileType.equals(type)){
                    LogHelper.Debug("???????????????" + f.getAbsolutePath());
                    f.delete();
                }
            }
        }
    }
    //??????7????????????????????????
    public void DeleteFileDate() {
        String logFilePath = Paras.appContext.getExternalFilesDir(null)
                + File.separator + "nf" + File.separator + "logs"
                + File.separator;
        File dirPath = new File(logFilePath);
        if (dirPath.exists()) { //??????????????????????????????
            if (dirPath.isDirectory()) { //?????????????????????
                //?????????????????????????????????????????????
                File[] files = dirPath.listFiles();
                assert files != null;
                for (File f : files) {
                    //??????????????????????????????????????????
                    File absFile = f.getAbsoluteFile();
                    long currTime = System.currentTimeMillis(); //????????????
                    long lastTime = absFile.lastModified(); //????????????????????????????????????
                    long diffen = currTime - lastTime;
                    if (diffen > 7 * 24 * 60 * 60 * 1000) { // ????????????7????????????
                        LogHelper.Debug("????????????" + absFile.getName());
                        absFile.delete();
                    }
                }
            }
        }
    }
    //????????????????????????
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) Paras.appContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(Paras.appContext.getPackageName());
        }
        LogHelper.Debug("?????????????????????"+isIgnoring);
        return isIgnoring;
    }
}

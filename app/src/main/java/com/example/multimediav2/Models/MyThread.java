package com.example.multimediav2.Models;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.FileUnit.FileUnitDef;
import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.PowerManager.PowerManager_HKRK3128;
import com.example.multimediav2.Utils.AudioUtil;
import com.example.multimediav2.Utils.Base64FileUtil;
import com.example.multimediav2.Utils.NetWorkUtils;
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

public class MyThread extends Thread {
    static FileUnitDef fileUnitDef= new FileUnitDef();
    final SPUnit spUnit = new SPUnit(Paras.appContext);
    final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
    public boolean isStop=false;
    public MyThread(){

    }
    @Override
    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    try {
                        String jsonStr="";
                        //更新心跳时间
                        Long device_status=Paras.powerManager.IsOpen()?0L:1L;
                        JSONObject updateObject=new JSONObject();
                        updateObject.put("sn",deviceData.getSn());
                        updateObject.put("is_record",1);
                        updateObject.put("device_status",device_status);
                        if (Paras.update_num==0) {
                            String updateRes="";
                            try {
                                if(NetWorkUtils.isNetworkAvailable(Paras.appContext)) {
                                    updateRes= HttpUnitFactory.Get().Post(Paras.mulAPIAddr + "/media/third/updateHeartTime",updateObject.toString());
                                }

                            } catch (Exception e) {
                                    /*LogHelper.Error("更新心跳时间异常："+e);
                                    Paras.msgManager.SendMsg("网络连接异常");*/
                                LogHelper.Error("更新心跳时间异常："+e);
                                Paras.msgManager.SendMsg("网络连接异常");
                                Paras.updateProgram=true;
                                Paras.underUrl="";
                                Paras.programUrl="";
                            }
                            if(!Objects.equals(updateRes, "")) {
                                JSONObject timeObject= new JSONObject(updateRes);
                                boolean res = timeObject.getBoolean("success");
                                if(res) {
                                    if (Paras.success_num==0) {
                                        LogHelper.Debug("更新心跳时间成功");
                                        Paras.success_num++;
                                    } else {
                                        Paras.success_num++;
                                        if(Paras.success_num>=10) {
                                            Paras.success_num=0;
                                        }
                                    }

                                }
                            }
                            Paras.update_num++;
                        } else {
                            Paras.update_num++;
                            if(Paras.update_num>=10) {
                                Paras.update_num=0;

                            }
                        }
                        try {
                            if(NetWorkUtils.isNetworkAvailable(Paras.appContext)) {
                                jsonStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getCmd"+"?sn="+deviceData.getSn());
                            }
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
                                        boolean checkScreen=false;
                                        try {
                                            checkScreen = Objects.equals(contentObject.getString("checkScreen"), "Y");
                                        } catch (Exception EE) {

                                        }
                                        if(Paras.devType.equals(Paras.DEVA20_XiPinBox)) {
                                            Paras.powerManager.ShutDown(checkScreen);
                                        } else {
                                            Paras.powerManager.ShutDown(false);
                                        }
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
                                        } else if(Paras.HAI_KANG_RK3128.equals(Paras.devType)) {
                                            picPath= PowerManager_HKRK3128.Screenshot();
                                            base64Str=Base64FileUtil.encodeBase64File(picPath);
                                        } else {
                                            picPath = BaseActivity.A40XiPinScreenShot();
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
                                        AudioUtil audioUtil= AudioUtil.getInstance(Paras.appContext);
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
                                            String dir1 = Paras.appContext.getExternalFilesDir("nf").getPath();
                                            deleteFile(dir1,"apk");
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
                                        Paras.powerManager.setSystemTime(Paras.appContext);
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
                                        Paras.textSpeaker2.read(voiceTxt);
                                        break;
                                    case "1014":
                                        String templateCode=contentObject.getString("templateCode");
                                        Long voiceVolume=contentObject.getLong("voiceVolume");
                                        String voiceDate=contentObject.getString("voiceData");
                                        Long voiceSpeed=contentObject.getLong("voiceSpeed");
                                        LogHelper.Debug("开始呼叫：" + voiceDate);
                                        Paras.volume= Math.toIntExact(voiceVolume);
                                        //textSpeaker2.setSpeed(voiceSpeed);
                                        Paras.textSpeaker2.read(voiceDate);
                                        break;
                                    case "1015":
                                        String stopUSB=contentObject.getString("enable");
                                        deviceData.setStopUSB(stopUSB);
                                        spUnit.Set("DeviceData",deviceData);
                                        boolean offOrOn=stopUSB.equals("N");
                                        Paras.powerManager.StopUSB(offOrOn);
                                                /*Process p = Runtime.getRuntime().exec("su");
                                                DataOutputStream localDataOutputStream = new DataOutputStream(p.getOutputStream());

                                                localDataOutputStream.writeBytes("echo 0 > /sys/class/android_usb/android0/enable\n");
                                                localDataOutputStream.writeBytes("exit\n");
                                                localDataOutputStream.flush();
                                                p.waitFor();
                                                int ret = p.exitValue();
                                                LogHelper.Debug(ret + "");*/
                                        break;
                                            /*case "1016":
                                                Process p1 = Runtime.getRuntime().exec("su");
                                                DataOutputStream localDataOutputStream1 = new DataOutputStream(p1.getOutputStream());

                                                localDataOutputStream1.writeBytes("echo 1 > /sys/class/android_usb/android0/enable\n");
                                                localDataOutputStream1.writeBytes("exit\n");
                                                localDataOutputStream1.flush();
                                                p1.waitFor();
                                                int ret1 = p1.exitValue();
                                                LogHelper.Debug(ret1 + "");
                                                break;*/
                                    case "1017":
                                        int streamType=contentObject.getInt("streamType");
                                        deviceData.setStream_type(streamType);
                                        spUnit.Set("DeviceData",deviceData);
                                        Paras.textSpeaker2=new TextSpeaker2(Paras.appContext);
                                        LogHelper.Debug("语音类型调整为："+streamType);
                                        break;
                                    case "1018":
                                        String enable=contentObject.getString("enable");
                                        int screenTime=contentObject.getInt("ShutScreenTime");
                                        deviceData.setScreenEnable(enable);
                                        deviceData.setScreenTime(screenTime);
                                        spUnit.Set("DeviceData",deviceData);
                                        LogHelper.Debug("是否开启自动截屏："+enable);
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

                            }
                        } else {
                            LogHelper.Error("接口数据获取失败");
                        }
                        Thread.sleep(Paras.heart_time*1000);
                    } catch (Exception e) {
                        LogHelper.Error("heartTask"+e);
                    }

                }

            }
        },"heartChildTask").start();

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

    public static String filterSpecialChars(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        String pattern = "[^a-zA-Z0-9\\u4E00-\\u9FA5\\s\\[\\]\\{\\}\\(\\),\\\"':./\\\\-]"; // 只允许字母、数字和中文
        return str.replaceAll(pattern, "");
    }

    public void IsStop() {
        isStop=true;
    }

}

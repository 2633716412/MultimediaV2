package com.example.multimediav2;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Modules.IMsgManager;
import Modules.LogHelper;
import Modules.Paras;

public class CustApplication extends Application implements Thread.UncaughtExceptionHandler, IMsgManager {

    private ScheduledExecutorService scheduledExecutorService;
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(this);

        Paras.appContext = this.getApplicationContext();

        Paras.Wiidth = getResources().getDisplayMetrics().widthPixels;
        Paras.Height = getResources().getDisplayMetrics().heightPixels;

        Paras.msgManager=this;
        scheduledExecutorService = Executors.newScheduledThreadPool(10);
        scheduledExecutorService.scheduleAtFixedRate(SendRunnable,0,5, TimeUnit.SECONDS);
        /*LeakCanary.Config config = LeakCanary.getConfig().newBuilder()
                .retainedVisibleThreshold(3)
                .computeRetainedHeapSize(false)
                .build();
        LeakCanary.setConfig(config);*/

    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        LogHelper.Fatal("uncaughtException"+e.getMessage(), e);
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10 * 1000, restartIntent); // n秒后重启
        System.exit(0);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        // 当应用程序终止时，取消所有任务并关闭线程池
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }
    public void SendMsg(String msg) {
        Message message = new Message();
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            Toast.makeText(Paras.appContext, msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };
    private Runnable SendRunnable =new Runnable() {
        @Override
        public void run() {
            sendTimeData(); // 发送时间数据的方法
        }
    };
    public void sendTimeData() {
        // 创建一个包含时间数据的 Intent
        Intent intent = new Intent();
        intent.setAction("com.example.ACTION_SEND_TIME");
        intent.putExtra("time", System.currentTimeMillis());
        sendBroadcast(intent);
    }

    /*public static void copyAssetsTBS(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssetsTBS(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            LogHelper.Error("文件复制失败"+e.toString());
        }
    }*/
}
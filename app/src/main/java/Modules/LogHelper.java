package Modules;

import android.os.Build;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogHelper {

    private static Logger logger;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static String dateStr=simpleDateFormat.format(new Date());
    public final static String logFilePath=Paras.appContext.getExternalFilesDir(null)
            + File.separator + "nf" + File.separator + "logs"
            + File.separator + dateStr+"log4j.log";

    static {
        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setMaxBackupSize(5);
        logConfigurator.setFileName(logFilePath);
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.INFO);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setResetConfiguration(true);
        logConfigurator.setUseLogCatAppender(false);
        logConfigurator.configure();

        logger = Logger.getLogger(LogHelper.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void SaveAppInfo()
    {
        Debug(String.format("内存:%s 线程:%s",getMemory()/1024, Thread.activeCount()));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static int getMemory() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        // dalvikPrivateClean + nativePrivateClean + otherPrivateClean;
        int totalPrivateClean = memoryInfo.getTotalPrivateClean();
        // dalvikPrivateDirty + nativePrivateDirty + otherPrivateDirty;
        int totalPrivateDirty = memoryInfo.getTotalPrivateDirty();
        // dalvikPss + nativePss + otherPss;
        int totalPss = memoryInfo.getTotalPss();
        // dalvikSharedClean + nativeSharedClean + otherSharedClean;
        int totalSharedClean = memoryInfo.getTotalSharedClean();
        // dalvikSharedDirty + nativeSharedDirty + otherSharedDirty;
        int totalSharedDirty = memoryInfo.getTotalSharedDirty();
        // dalvikSwappablePss + nativeSwappablePss + otherSwappablePss;
        int totalSwappablePss = memoryInfo.getTotalSwappablePss();

        int total = totalPrivateClean + totalPrivateDirty + totalPss + totalSharedClean + totalSharedDirty + totalSwappablePss;
        return total;
    }


    static  public  void  Fatal(String msg, Throwable e)
    {
        try {
            Log.wtf("Fatal",msg);
            logger.fatal(msg,e);
        }
        catch (Exception ex)
        {

        }
    }


    static public void Error(Exception ex, boolean write) {
        Error(ex, "错误",write);
    }

    static public void Error(Exception ex) {
        Error(ex, true);
    }


    static public void Error(String msg, boolean write) {
        try {
            Log.e("错误", msg);

            if(write)
            {
                logger.error(String.format("%s %s", "错误", msg));
            }
        } catch (Exception ee) {

        }
    }

    static public void Error(String msg)
    {
        Error(msg,true);
    }

    static public void Error(Exception ex, String addtMsg, boolean write) {

        try {
            StringBuilder sb=new StringBuilder();
            sb.append(ex.getMessage()+"\r\n");

            for(StackTraceElement ste:ex.getStackTrace())
            {
                sb.append(ste.toString()+"\r\n");
            }

            Log.e(addtMsg,sb.toString());

           // Log.e(addtMsg, ex.getMessage() + ex.getStackTrace());

            Error(String.format("%s %s", addtMsg, ex.getMessage() + ex.getStackTrace()),write);
        } catch (Exception ee) {

        }
    }

    static public void Error(Exception ex, String addtMsg)
    {
        Error(ex,addtMsg,true);
    }

    static public void Debug(String msg) {
        Debug(msg, "调试");
    }

    static public void Debug(String msg, String addtMsg) {
        Log.d(addtMsg, msg);
        logger.debug(String.format("%s %s", addtMsg, msg));
    }
}
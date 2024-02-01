package Modules;

import android.content.Context;

import com.example.multimediav2.CacheServer.ICacheServer;
import com.example.multimediav2.PowerManager.IPowerManager;
import com.example.multimediav2.VolumeManager.IVolumeManager;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

public class Paras {

    //<editor-fold desc="常量">
    public static final int heart_time=1;
    public static final int time_start_listen_power = 60;

    public static final int time_loop_power = 60;

    public static final Long device_id = 10001L;

    public static final boolean DEVELOPMODE = true;

    public static final String DEVMR = "test";//mr

    public static final String DEVA20_XiPinBox = "a40box";

    public static final String DEVA40_XiPin = "a40xp";
    public static final String HAI_KANG="hk";
    public static final String HAI_KANG_6055="hk_6055";
    public static final String HAI_KANG_RK3128="hk_rk3128";
    //</editor-fold>

    //<editor-fold desc="全局参数">

    public static Context appContext;

    public static int Wiidth = 1920;

    public static int Height = 1080;

    public static String devType = DEVMR;

    public static String mulAPIAddr = "http://ip:port/selfv2api";

    public static String mulHtmlAddr = "http://ip:port/app/index.html";

    public static String name = "";

    public static String androidNumber="";


    public static int volume = 100;
    //</editor-fold>

    //<editor-fold desc="单例">
    public static IPowerManager powerManager;

    public static ICacheServer cacheServer;

    public static IVolumeManager volumnManager;

    public static IMsgManager msgManager;

    public static TextSpeaker2 textSpeaker2;

    public static boolean first = true;
    //判断是否刷新节目单
    public static boolean updateProgram = false;
    //0命令线程 1定时开关机 2截屏 3海康喂狗
    public static boolean[] hasRun=new boolean[4];

    public static int update_num=0;

    public static int success_num=0;

    public static int lis_num=0;
    //</editor-fold>
    //上层节目url
    public static String programUrl="";
    //底层url
    public static String underUrl="";
    public static boolean refresh=false;
    public static Date programEndDate=new Date();
    public static ScheduledExecutorService executor;//线程池
}

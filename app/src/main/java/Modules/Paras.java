package Modules;

import android.content.Context;

import com.example.multimediav2.CacheServer.ICacheServer;
import com.example.multimediav2.PowerManager.IPowerManager;
import com.example.multimediav2.VolumeManager.IVolumeManager;

public class Paras {

    //<editor-fold desc="常量">

    public static final int time_start_listen_power = 10;

    public static final int time_loop_power = 10;

    public static final Long device_id = 10001L;

    public static final boolean DEVELOPMODE = true;

    public static final String FILEPROVICE = "com.ningfan.fileprovider";

    public static final String DEVMR = "test";//mr

    public static final String DEVA40 = "a40";

    public static final String DEVA20 = "a20";

    public static final String DEVA20_XiPin = "a20xp";

    public static final String DEVA40_XiPin = "a40xp";
    //</editor-fold>

    //<editor-fold desc="全局参数">

    public static Context appContext;

    public static int Wiidth = 1920;

    public static int Height = 1080;

    public static String devType = DEVMR;

    public static String mulAPIAddr = "http://192.168.9.201:14084/selfv2api";

    public static String mulHtmlAddr = "http://192.168.9.201:14084/selfpc2/app/index.html";

    public static String name = "";

    public static String androidNumber="";

    public static int volume = 100;
    //</editor-fold>

    //<editor-fold desc="单例">
    public static IPowerManager powerManager;

    public static ICacheServer cacheServer;

    public static IVolumeManager volumnManager;

    public static IMsgManager msgManager;

    public static boolean first = true;

    public static boolean updateProgram = false;

    //</editor-fold>

}

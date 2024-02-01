package com.example.multimediav2.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.webkit.WebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import Modules.LogHelper;
import Modules.Paras;

public class NetWorkUtils {

    static public String GetIP(Context context) {

        if (Paras.DEVELOPMODE) {
            //return "192.168.9.32";
        }

        String ip = NetWorkUtils.getLocalIpAddress(context);

        if (ip.equals("0.0.0.0")) {
            ip = NetWorkUtils.getLocalIp();
        }

        return ip;
    }

    static String getLocalIp() {

        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("tag", "网络名字" + interfaceName);

                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Log.i("tag", inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            LogHelper.Error("getLocalIp异常："+e.toString());
        }
        return "";
    }


    static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            LogHelper.Error(ex);
        }
        return null;
    }

    static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
        // return null;
    }
    /**
     * 检查网络是否可用
     *
     * @param context 上下文对象
     * @return true表示网络可用，false表示网络不可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            // 获取软件版本名称
            versionName = context.getPackageManager().getPackageInfo(
                    "com.example.multimediav2", PackageManager.GET_ACTIVITIES).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.Error(e.toString());
        }
        return versionName;
    }

    //备份
    /*public void GetProgramData(String sn) {
        try {
            boolean isStopped=false;
            while (!isStopped) {
                String jsonStr="";
                try {
                    if(NetWorkUtils.isNetworkAvailable(Paras.appContext)) {
                        jsonStr = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getProgramData?sn=" + sn);
                        if(Paras.devType.equals(Paras.DEVA40_XiPin)) {
                            //清除浏览器缓存
                            webView1.post(new Runnable() {
                                @Override
                                public void run() {
                                    webView1.clearCache(true);
                                }
                            });
                            webView2.post(new Runnable() {
                                @Override
                                public void run() {
                                    webView2.clearCache(true);
                                }
                            });
                        }

                    } else {
                        Paras.updateProgram=true;
                        LogHelper.Error("NetWorkUtils网络异常");
                    }

                } catch (Exception e) {
                    LogHelper.Error("获取节目异常："+e);
                }
                if(!Objects.equals(jsonStr, "")) {
                    JSONObject object = new JSONObject(jsonStr);
                    StringBuilder url = new StringBuilder(Paras.mulHtmlAddr);
                    StringBuilder wvUrl=new StringBuilder("");
                    JSONArray itemArray = object.getJSONArray("data");
                    final boolean[] first = {false};
                    if(object.getBoolean("success")) {
                        isStopped=true;
                        for (int i = 0; i < itemArray.length(); i++) {
                            JSONObject object1 = itemArray.getJSONObject(i);
                            String repeatDay = object1.getString("repet_day");
                            Long programId = object1.getLong("program_id");
                            String underUrl=object1.getString("under_url");
                            DateUtil dateUtil = new DateUtil();
                            String nowWeek = String.valueOf(dateUtil.DayOfWeek());
                            if (repeatDay.contains(nowWeek)) {
                                JSONArray timeList = object1.getJSONArray("time_list");
                                for (int j = 0; j < timeList.length(); j++) {
                                    JSONObject timeObject = timeList.getJSONObject(j);
                                    String startStr = timeObject.getString("begin_time");
                                    String endStr = timeObject.getString("end_time");
                                    DateUtil begin = DateUtil.GetByHourMin(startStr);
                                    DateUtil end = DateUtil.GetByHourMin(endStr);
                                    DateUtil now = DateUtil.Now();
                                    if (now.Between(begin, end)&& !first[0]) {
                                        List<String> timeStr= Arrays.asList(endStr.split(":"));
                                        Calendar start = Calendar.getInstance();
                                        int hour= Integer.parseInt(timeStr.get(0));
                                        int minutes= Integer.parseInt(timeStr.get(1));
                                        start.setTime(new Date());
                                        start.set( Calendar.HOUR_OF_DAY,hour);
                                        start.set( Calendar.MINUTE, minutes);
                                        start.set( Calendar.SECOND,0);
                                        Paras.programEndDate=start.getTime();
                                        if(url.toString().contains("http://ip:port/app/index.html"))
                                        {
                                            Paras.updateProgram=true;
                                        }
                                        url.append("?id=").append(programId);
                                        if(underUrl!=null&& !underUrl.equals("")) {
                                            wvUrl.append(underUrl);
                                        }
                                        first[0] =true;
                                    }
                                }
                            }
                        }

                        ShowActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if(Paras.devType.equals(Paras.DEVA40_XiPin)) {
                                        webView2.loadUrl(url.toString());
                                        webView1.loadUrl(wvUrl.toString());
                                        LogHelper.Debug("url："+url.toString());
                                        LogHelper.Debug("wvUrl："+wvUrl.toString());
                                    } else {
                                        if(!url.toString().equals(Paras.programUrl)||!wvUrl.toString().equals(Paras.underUrl)) {
                                            webView2.loadUrl(url.toString());
                                            webView1.loadUrl(wvUrl.toString());
                                            if(Paras.devType.equals(Paras.HAI_KANG)) {
                                                Paras.powerManager.StatusBar();
                                            }
                                            LogHelper.Debug("url："+url.toString());
                                            LogHelper.Debug("wvUrl："+wvUrl.toString());
                                            Paras.programUrl=url.toString();
                                            Paras.underUrl=wvUrl.toString();
                                        }
                                    *//*webView2.loadUrl(url.toString());
                                    webView1.loadUrl(wvUrl.toString());
                                    if(Paras.devType.equals(Paras.HAI_KANG)) {
                                        Paras.powerManager.StatusBar();
                                    }*//*
                                    }

                                } catch (Exception e) {
                                    LogHelper.Error("更新url"+e.toString());
                                }

                            }
                        });
                    } else if(object.getBoolean("success")) {
                        LogHelper.Error("GetProgramData接口返回错误"+jsonStr);
                    }
                }
            }

        } catch (Exception e) {
            LogHelper.Error("GetProgramData"+e.toString());
        }
    }*/

    public static String GetGBIp() {
        String gbIp="";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address.isSiteLocalAddress()) {
                        InetAddress item=calculateBroadcastAddress(address);
                        if(item!=null) {
                            return item.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.Error("GetLocalIp获取失败"+e.toString());
        }
        //gbIp=gbIp.substring(0,gbIp.lastIndexOf('.'));
        //return gbIp+".255";//广播地址一般为当前局域网的最后一个
        return null;
    }

    public static InetAddress calculateBroadcastAddress(InetAddress inetAddress) throws SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
        if (networkInterface != null) {
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcastAddress = interfaceAddress.getBroadcast();
                if (broadcastAddress != null) {
                    return broadcastAddress;
                }
            }
        }
        return null;
    }
    //截取屏幕Bitmap
    public static Bitmap captureWebView(final WebView webView) {
        int width = getWebViewWidth(webView);
        int height = getWebViewHeight(webView);

        if (width <= 0 || height <= 0) {
            // 处理宽度或高度无效的情况
            return null;
        }
        Picture picture = webView.capturePicture();
        /*int width = picture.getWidth();
        int height = picture.getHeight();*/
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        picture.draw(canvas);
        return bitmap;
        /*Bitmap bmp = null;
        try {
            if (BaseActivity.currActivity != null) {
                View dView = BaseActivity.currActivity.getWindow().getDecorView();
                dView.setDrawingCacheEnabled(true);
                dView.destroyDrawingCache();
                dView.buildDrawingCache();
                bmp = dView.getDrawingCache();
            }
        } catch (Exception ex) {
            LogHelper.Error(ex);
        } finally {
            return bmp;
        }*/
    }
    //判断是否白屏
    public static boolean isWhiteScreen(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            if (red != 255 || green != 255 || blue != 255) {
                return false;
            }
        }

        return true;
    }
    private static int getWebViewWidth(WebView webView) {
        return webView.getWidth() > 0 ? webView.getWidth() : webView.getMeasuredWidth();
    }

    private static int getWebViewHeight(WebView webView) {
        return webView.getHeight() > 0 ? webView.getHeight() : webView.getMeasuredHeight();
    }

    public static InputStream getFileInputStream(String filePath) {
        InputStream inputStream = null;
        try {
            File file=new File(filePath);
            inputStream = new FileInputStream(file);
        } catch (IOException e) {
            LogHelper.Error("getFileInputStream转换失败"+e.toString());
        } /*finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogHelper.Error("getFileInputStream流关闭异常"+e.toString());
                }
            }
        }*/
        return inputStream;
    }
}
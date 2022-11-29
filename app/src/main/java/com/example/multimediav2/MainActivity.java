package com.example.multimediav2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Models.CmdManager;
import com.example.multimediav2.Models.DropData;

import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import Modules.DeviceData;
import Modules.IMsgManager;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;

public class MainActivity extends BaseActivity implements IMsgManager {

    private EditText device_name;
    private EditText inter1;
    private EditText inter2;
    private EditText inter3;
    private EditText inter4;
    private EditText port;
    private Spinner device_type;
    private Button btu_save;
    private TextView switch_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paras.appContext=this;
        Paras.msgManager=this;

        /*startService(new Intent(this, AppService.class));
        startService(new Intent(this, AppService2.class));*/
        /*Intent intent = new Intent(Paras.appContext, AppService.class);
        startService(intent);*/
        Paras.androidNumber= "Android"+android.os.Build.VERSION.RELEASE;
        Paras.Wiidth = getResources().getDisplayMetrics().widthPixels;
        Paras.Height = getResources().getDisplayMetrics().heightPixels;
        checkPermission();
        SPUnit spUnit = new SPUnit(MainActivity.this);
        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        device_type=findViewById(R.id.device_type);
        switch_text=findViewById(R.id.switch_text);
        List<DropData> dropList=new ArrayList<DropData>();
        DropData dev0=new DropData("test","TEST");
        dropList.add(dev0);
        DropData dev1=new DropData("a20","DEVA20");
        dropList.add(dev1);
        DropData dev2=new DropData("a40","DEVA40");
        dropList.add(dev2);
        DropData dev3=new DropData("a20xp","DEVA20_XiPin");
        dropList.add(dev3);
        DropData dev4=new DropData("a40xp","DEVA40_XiPin");
        dropList.add(dev4);
        DropData dev5=new DropData("hk","HAI_KANG");
        dropList.add(dev5);
        ArrayAdapter<DropData> adapter = new ArrayAdapter<DropData>(MainActivity.this, android.R.layout.simple_spinner_item, dropList);
        device_type.setAdapter(adapter);

        if(deviceData.getId()>0) {
            device_name=findViewById(R.id.device_name);
            inter1=findViewById(R.id.inter1);
            inter2=findViewById(R.id.inter2);
            inter3=findViewById(R.id.inter3);
            inter4=findViewById(R.id.inter4);
            port=findViewById(R.id.port);
            if(deviceData.getOsTimes().size()>0) {
                StringBuilder timeStr= new StringBuilder();
                for(int i=0;i<7;i++) {
                    String item="";
                    for(OSTime osTime:deviceData.getOsTimes()) {
                        if(osTime.dayofweak==i+1) {
                            String week=GetCnWeek(osTime.dayofweak);
                            item="周"+week+" "+JudgeTime(osTime.open_hour)+":"+JudgeTime(osTime.open_min)+"开"+" "+JudgeTime(osTime.close_hour)+":"+JudgeTime(osTime.close_min)+"关";
                        }
                    }
                    if(item.equals("")) {

                        item="周"+GetCnWeek(i+1)+" 休息";
                    }
                    timeStr.append(item);
                    if(i!=6) {
                        timeStr.append("\n");
                    }
                }
                switch_text.setText(timeStr);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Paras.mulAPIAddr=GetApiUrl(Paras.mulAPIAddr,deviceData.getApi_ip(),deviceData.getApi_port());
                    String urlSuffix="";
                    if(!Objects.equals(deviceData.getApi_ip(), "")) {
                        try {
                            String result= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getUrlSuffix");
                            if(!Objects.equals(result, "")) {
                                JSONObject object = new JSONObject(result);
                                urlSuffix = object.getString("data");

                            }
                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }
                    Paras.mulHtmlAddr=GetUrl(Paras.mulHtmlAddr,deviceData.getApi_ip(),deviceData.getApi_port(),urlSuffix);
                }
            }).start();

            device_name.setText(deviceData.getDevice_name());
            if(!Objects.equals(deviceData.getApi_ip(), "")) {
                List<String> inters= Arrays.asList(deviceData.getApi_ip().split("\\."));
                inter1.setText(inters.get(0));
                inter2.setText(inters.get(1));
                inter3.setText(inters.get(2));
                inter4.setText(inters.get(3));
            }
            port.setText(deviceData.getApi_port());
            for(int i=0;i<dropList.size();i++) {
                DropData data=dropList.get(i);
                if(Objects.equals(deviceData.getDevice_type(), data.getCode())) {
                    device_type.setSelection(i);
                }
            }

            if (Paras.first) {
                CmdManager iIniHanlder = new CmdManager();
                iIniHanlder.Init(MainActivity.this, null);
                //startService(new Intent(this, AppService.class));
                SkipTo(ShowActivity.class);
            }
        }

        btu_save=findViewById(R.id.btu_save);
        btu_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    device_name=findViewById(R.id.device_name);
                    inter1=findViewById(R.id.inter1);
                    inter2=findViewById(R.id.inter2);
                    inter3=findViewById(R.id.inter3);
                    inter4=findViewById(R.id.inter4);
                    port=findViewById(R.id.port);
                    device_type=findViewById(R.id.device_type);
                    SPUnit spUnit = new SPUnit(MainActivity.this);
                    DeviceData data=spUnit.Get("DeviceData", DeviceData.class);
                    //获取本地ip
                    WifiManager wifiManager = (WifiManager) Paras.appContext.getSystemService(Paras.appContext.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (!wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(true);
                    }

                    int ipAddress = wifiInfo.getIpAddress();
                    if(!intToIp(ipAddress).equals("0.0.0.0")) {
                        data.setDevice_ip(intToIp(ipAddress));
                    } else {
                        String ip=getLocalIpAddress();
                        data.setDevice_ip(ip);
                    }

                    StringBuilder ipStr=new StringBuilder(inter1.getText().toString());
                    ipStr.append(".");
                    ipStr.append(inter2.getText().toString());
                    ipStr.append(".");
                    ipStr.append(inter3.getText().toString());
                    ipStr.append(".");
                    ipStr.append(inter4.getText().toString());
                    data.setDevice_name(device_name.getText().toString());
                    data.setApi_ip(ipStr.toString());
                    data.setApi_port(port.getText().toString());

                    DropData deviceType=(DropData)device_type.getSelectedItem();
                    data.setDevice_type(deviceType.getCode());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Paras.mulAPIAddr=GetApiUrl(Paras.mulAPIAddr,data.getApi_ip(),data.getApi_port());
                            String urlSuffix="";
                            if(!Objects.equals(data.getApi_ip(), "")) {
                                try {
                                    String result= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getUrlSuffix");
                                    if(!Objects.equals(result, "")) {
                                        JSONObject object = new JSONObject(result);
                                        urlSuffix = object.getString("data");

                                    }
                                } catch (Exception e) {
                                    LogHelper.Error(e);
                                }
                            }
                            Paras.mulHtmlAddr=GetUrl(Paras.mulHtmlAddr,data.getApi_ip(),data.getApi_port(),urlSuffix);
                        }
                    }).start();

                    spUnit.Set("DeviceData",data);
                    CmdManager iIniHanlder = new CmdManager();
                    iIniHanlder.Init(MainActivity.this, null);
                    Paras.msgManager.SendMsg("修改配置完成");
                    Paras.updateProgram=true;
                    SkipTo(ShowActivity.class);
                } catch (Exception ex) {
                    LogHelper.Error(ex);
                    //Paras.msgManager.SendMsg("修改配置异常：" + ex.getMessage());
                }
            }
        });

        final Button btn_tts = findViewById(R.id.btu_tts);
        btn_tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });

    }

    private static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }

    void checkPermission() {

        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if ((checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.INTERNET);

            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionsList.size() != 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
            }
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

    private String GetCnWeek(int n) {
        switch (n) {
            case 1:return "一";
            case 2:return "二";
            case 3:return "三";
            case 4:return "四";
            case 5:return "五";
            case 6:return "六";
            default:return "日";
        }
    }

    private String JudgeTime(int n) {
        if(n<10) {
            return "0"+n;
        } else {
            return String.valueOf(n);
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            LogHelper.Error(ex);
            return "";
        }
        return "";
    }

    public String GetApiUrl(String oldUrl,String ip,String port) {
        String newStr="";
        String tallStr=oldUrl.substring(oldUrl.indexOf("/self"));
        String headStr=oldUrl.substring(0,oldUrl.indexOf("//")+2);
        newStr=headStr+ip+":"+port+tallStr;
        return newStr;
    }
    public String GetUrl(String oldUrl,String ip,String port,String urlSuffix) {
        String newStr="";
        String tallStr=oldUrl.substring(oldUrl.indexOf("/app"));
        String headStr=oldUrl.substring(0,oldUrl.indexOf("//")+2);
        newStr=headStr+ip+":"+port+"/"+urlSuffix+tallStr;
        return newStr;
    }
}
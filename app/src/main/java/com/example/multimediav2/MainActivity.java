package com.example.multimediav2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Models.DropData;
import com.example.multimediav2.Models.MyAdapter;
import com.example.multimediav2.Models.MyBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import Modules.DeviceData;
import Modules.LogHelper;
import Modules.OSTime;
import Modules.Paras;
import Modules.SPUnit;

public class MainActivity extends BaseActivity {

    private EditText device_name;
    private EditText inter1;
    private EditText inter2;
    private EditText inter3;
    private EditText inter4;
    private EditText port;
    private Spinner device_type;
    private Button btu_save;
    private TextView switch_text;
    private Spinner spinner;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mServiceIntent = new Intent(this, MyService.class);
        Paras.androidNumber= "Android"+ Build.VERSION.RELEASE;
        Paras.Wiidth = getResources().getDisplayMetrics().widthPixels;
        Paras.Height = getResources().getDisplayMetrics().heightPixels;
        checkPermission();
        SPUnit spUnit = new SPUnit(Paras.appContext);
        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        device_type=findViewById(R.id.device_type);
        switch_text=findViewById(R.id.switch_text);
        spinner=findViewById(R.id.spinner);
        inter1=findViewById(R.id.inter1);
        inter2=findViewById(R.id.inter2);
        inter3=findViewById(R.id.inter3);
        inter4=findViewById(R.id.inter4);
        port=findViewById(R.id.port);
        spinner=findViewById(R.id.spinner);
        List<DropData> dropList=new ArrayList<DropData>();
        DropData dev1=new DropData("a40xp","DEVA40_XiPin");
        dropList.add(dev1);
        DropData dev2=new DropData("a40box","DEVA40_XiPinBox");
        dropList.add(dev2);
        DropData dev3=new DropData("hk","HAI_KANG");
        dropList.add(dev3);
        DropData dev4=new DropData("hk_6055","HAI_KANG_6055触摸");
        dropList.add(dev4);
        DropData dev5=new DropData("hk_rk3128","HAI_KANG_RK3128");
        dropList.add(dev5);

        ArrayAdapter<DropData> adapter = new ArrayAdapter<DropData>(MainActivity.this, android.R.layout.simple_spinner_item, dropList);
        device_type.setAdapter(adapter);
        LogHelper.Debug("app开启");
        if(!Objects.equals(deviceData.getSn(), "")) {
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
                    boolean isStopped=false;
                    while (!isStopped) {
                        SPUnit spUnit = new SPUnit(MainActivity.this);
                        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                        Paras.mulAPIAddr=GetApiUrl(Paras.mulAPIAddr,deviceData.getApi_ip(),deviceData.getApi_port());
                        String urlSuffix="";
                        if(!Objects.equals(deviceData.getApi_ip(), "")) {
                            try {
                                String result= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getUrlSuffix");
                                if(!Objects.equals(result, "")) {
                                    JSONObject object = new JSONObject(result);
                                    urlSuffix = object.getString("data");
                                    isStopped=true;
                                }
                            } catch (Exception e) {
                                LogHelper.Error("获取节目地址异常："+e);
                            }
                        }
                        Paras.mulHtmlAddr=GetUrl(Paras.mulHtmlAddr,deviceData.getApi_ip(),deviceData.getApi_port(),urlSuffix);
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }
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
            //获取本地ip
            WifiManager wifiManager = (WifiManager) Paras.appContext.getSystemService(Paras.appContext.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            int ipAddress = wifiInfo.getIpAddress();
            if(!intToIp(ipAddress).equals("0.0.0.0")) {
                if(!intToIp(ipAddress).isEmpty()) {
                    deviceData.setDevice_ip(intToIp(ipAddress));
                }
            } else {
                String ip=getLocalIpAddress();
                if(!ip.isEmpty()) {
                    deviceData.setDevice_ip(ip);
                }

            }
            if(deviceData.getDevice_ip()!=null&& !Objects.equals(deviceData.getDevice_ip(), "")) {
                spUnit.Set("DeviceData",deviceData);
            }
            if (Paras.first) {
                deviceData.setSn(getUniquePsuedoID()+deviceData.getDevice_ip());
                spUnit.Set("DeviceData",deviceData);
                //跳转节目页时禁用软键盘
                StopInputMethod();
                Paras.updateProgram=true;
                Paras.underUrl="";
                Paras.programUrl="";
                //VideoUrlParser.deleteCacheFile();
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
                    SPUnit spUnit = new SPUnit(Paras.appContext);
                    DeviceData data=spUnit.Get("DeviceData", DeviceData.class);
                    //获取本地ip
                    WifiManager wifiManager = (WifiManager) Paras.appContext.getSystemService(Paras.appContext.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (!wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(true);
                    }

                    int ipAddress = wifiInfo.getIpAddress();
                    if(!intToIp(ipAddress).equals("0.0.0.0")) {
                        if(!intToIp(ipAddress).isEmpty()) {
                            data.setDevice_ip(intToIp(ipAddress));
                        }

                    } else {
                        String ip=getLocalIpAddress();
                        if(!ip.isEmpty()) {
                            data.setDevice_ip(ip);
                        }

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
                    spUnit.Set("DeviceData",data);
                    DropData deviceType=(DropData)device_type.getSelectedItem();
                    data.setDevice_type(deviceType.getCode());
                    /*if(data.getDevice_type().equals(Paras.DEVA20_XiPinBox)) {
                        checkAndTurnOnDeviceManager(null);
                    }*/
                    // 检查线程池是否已经终止
                    /*if (Paras.executor.isTerminated()) {
                        // 创建一个新的线程池
                        Paras.executor = Executors.newScheduledThreadPool(10);
                    }
                    Paras.executor.execute(suffixTask);*/
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isStopped=false;
                            while (!isStopped) {
                                SPUnit spUnit = new SPUnit(MainActivity.this);
                                DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                                Paras.mulAPIAddr=GetApiUrl(Paras.mulAPIAddr,deviceData.getApi_ip(),deviceData.getApi_port());
                                String urlSuffix="";
                                if(!Objects.equals(deviceData.getApi_ip(), "")) {
                                    try {
                                        String result= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getUrlSuffix");
                                        if(!Objects.equals(result, "")) {
                                            JSONObject object = new JSONObject(result);
                                            urlSuffix = object.getString("data");
                                            isStopped=true;
                                        }
                                    } catch (Exception e) {
                                        LogHelper.Error("获取节目地址异常："+e);
                                    }
                                }
                                Paras.mulHtmlAddr=GetUrl(Paras.mulHtmlAddr,deviceData.getApi_ip(),deviceData.getApi_port(),urlSuffix);
                                try {
                                    Thread.sleep(5000);
                                } catch (Exception e) {
                                    LogHelper.Error(e);
                                }
                            }
                        }
                    }).start();
                    data.setSn(getUniquePsuedoID()+data.getDevice_ip());
                    spUnit.Set("DeviceData",data);
                    Paras.msgManager.SendMsg("修改配置完成");
                    Paras.updateProgram=true;
                    Paras.underUrl="";
                    Paras.programUrl="";
                    Paras.first=true;
                    SkipTo(ShowActivity.class);
                } catch (Exception ex) {
                    LogHelper.Error(ex);
                    //Paras.msgManager.SendMsg("修改配置异常：" + ex.getMessage());
                }
            }
        });

        //实现编辑框监听回车换行或者点号的时候切换到下一个编辑框
        inter1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str=s.toString();
                if(str.contains(".")|| str.contains("\r") || str.contains("\n")) {
                    inter1.setText(str.replace(".","").replace("\r","").replace("\n",""));
                    inter2.requestFocus();
                }
            }
        });
        inter2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str=s.toString();
                if(str.contains(".")|| str.contains("\r") || str.contains("\n")) {
                    inter2.setText(str.replace(".","").replace("\r","").replace("\n",""));
                    inter3.requestFocus();
                }
            }
        });
        inter3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str=s.toString();
                if(str.contains(".")|| str.contains("\r") || str.contains("\n")) {
                    inter3.setText(str.replace(".","").replace("\r","").replace("\n",""));
                    inter4.requestFocus();
                }
            }
        });
        inter4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str=s.toString();
                if(str.contains(".")|| str.contains("\r") || str.contains("\n")) {
                    inter4.setText(str.replace(":","").replace("\r","").replace("\n",""));
                    port.requestFocus();
                }
            }
        });

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                boolean isStopped = false;
                while (!isStopped) {
                    if(!inter1.getText().toString().trim().equals("")&&!inter2.getText().toString().trim().equals("")&&!inter3.getText().toString().trim().equals("")&&!inter4.getText().toString().trim().equals("")&&!port.getText().toString().trim().equals("")) {
                        StringBuilder ipStr=new StringBuilder(inter1.getText().toString());
                        ipStr.append(".");
                        ipStr.append(inter2.getText().toString());
                        ipStr.append(".");
                        ipStr.append(inter3.getText().toString());
                        ipStr.append(".");
                        ipStr.append(inter4.getText().toString());
                        Paras.mulAPIAddr=GetApiUrl(Paras.mulAPIAddr,ipStr.toString(),port.getText().toString());
                        try {
                            String result = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/orgList");
                            String orgRes = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getOrg" + "?sn=" + deviceData.getSn());
                            if (!Objects.equals(orgRes, "")) {
                                JSONObject orgObj = new JSONObject(orgRes);
                                boolean orgSuc = orgObj.getBoolean("success");
                                if (orgSuc) {
                                    long orgId = orgObj.getLong("data");
                                    if (orgId > 0) {
                                        SPUnit spUnit = new SPUnit(MainActivity.this);
                                        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                                        deviceData.setOrgId(orgId);
                                        spUnit.Set("DeviceData", deviceData);
                                    }
                                } else {
                                    LogHelper.Error("获取设备机构失败：" + orgObj.getString("msg"));
                                }

                            }
                            if (!Objects.equals(result, "")) {
                                JSONObject object = new JSONObject(result);
                                boolean suc = object.getBoolean("success");
                                if (suc) {
                                    JSONArray jsonArray = object.getJSONArray("data");
                                    ArrayList<DropData> list = new ArrayList<DropData>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        DropData dropdata = new DropData();
                                        JSONObject obj = jsonArray.getJSONObject(i);
                                        dropdata.setId(obj.getLong("id"));
                                        dropdata.setName(obj.getString("org_name"));
                                        list.add(dropdata);
                                    }
                                    //ArrayAdapter<DropData> adapter=new ArrayAdapter<>(Paras.appContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,list);
                                    MyAdapter myAdapter = new MyAdapter(list);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                SPUnit spUnit = new SPUnit(MainActivity.this);
                                                DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                                                spinner.setAdapter(myAdapter);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        DropData data = (DropData) spinner.getSelectedItem();
                                                        SPUnit spUnit = new SPUnit(MainActivity.this);
                                                        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
                                                        deviceData.setOrgId(data.getId());
                                                        spUnit.Set("DeviceData", deviceData);
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                                if (deviceData.getOrgId() > 0) {
                                                    DropData d = list.stream().filter(p -> Objects.equals(p.getId(), deviceData.getOrgId())).collect(Collectors.toList()).get(0);
                                                    spinner.setSelection(list.indexOf(d));
                                                }
                                            } catch (Exception e) {
                                                LogHelper.Error(e);
                                            }

                                        }
                                    });

                                    isStopped = true;
                                } else {
                                    LogHelper.Error("获取机构下拉失败：" + object.getString("msg"));
                                }

                            }
                        } catch (Exception e) {
                            LogHelper.Error("获取机构列表异常：" + e);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            LogHelper.Error(e);
                        }
                    }

                }
            }
        }).start();

        final Button btn_tts = findViewById(R.id.btu_tts);
        btn_tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //禁用软键盘
    private void StopInputMethod() {
        //收起软键盘
        device_name=findViewById(R.id.device_name);
        inter1=findViewById(R.id.inter1);
        inter2=findViewById(R.id.inter2);
        inter3=findViewById(R.id.inter3);
        inter4=findViewById(R.id.inter4);
        port=findViewById(R.id.port);
        device_name.postDelayed(new Runnable() {
            @Override
            public void run() {
                device_name.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(device_name.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    device_name.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        inter1.postDelayed(new Runnable() {
            @Override
            public void run() {
                inter1.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inter1.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    inter1.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        inter2.postDelayed(new Runnable() {
            @Override
            public void run() {
                inter2.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inter2.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    inter2.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        inter3.postDelayed(new Runnable() {
            @Override
            public void run() {
                inter3.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inter3.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    inter3.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        inter4.postDelayed(new Runnable() {
            @Override
            public void run() {
                inter4.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inter4.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    inter4.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        port.postDelayed(new Runnable() {
            @Override
            public void run() {
                port.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(port.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    port.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

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

    //申请白名单
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            LogHelper.Error("申请白名单失败："+e);
        }
    }

    //获取设备唯一标识，卸载重装后依然能获得唯一值
    public static String getUniquePsuedoID() {

        String[] supportedABIArray;
        if (Build.VERSION.SDK_INT >= 21) {
            supportedABIArray = Build.SUPPORTED_ABIS;
        } else {
            supportedABIArray = new String[] {Build.CPU_ABI};
        }

        String supportedABIs = "";
        try {
            for (String s : supportedABIArray) {
                supportedABIs += s;
            }
        } catch (Exception e) {
            supportedABIs = "";
        }

        String m_szDevIDShort = "35";
        if (null != Build.BOARD) m_szDevIDShort += (Build.BOARD.length() % 10);
        if (null != Build.BRAND) m_szDevIDShort += (Build.BRAND.length() % 10);
        m_szDevIDShort += (supportedABIs.length() % 10);
        if (null != Build.DEVICE) m_szDevIDShort += (Build.DEVICE.length() % 10);
        if (null != Build.MANUFACTURER) m_szDevIDShort += (Build.MANUFACTURER.length() % 10);
        if (null != Build.MODEL) m_szDevIDShort += (Build.MODEL.length() % 10);
        if (null != Build.PRODUCT) m_szDevIDShort += (Build.PRODUCT.length() % 10);

        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();

            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {

            serial = "" + Calendar.getInstance().getTimeInMillis();
        }

        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * @param view 检测并去激活设备管理器权限
     */
    public void checkAndTurnOnDeviceManager(View view) {
        ComponentName adminReceiver= new ComponentName(Paras.appContext, MyBroadcastReceiver.class);;
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable USB blocking");
        //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二
        BaseActivity.currActivity.startActivityForResult(intent, 0);

    }


}
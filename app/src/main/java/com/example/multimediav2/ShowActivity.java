package com.example.multimediav2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Utils.DateUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Modules.DeviceData;
import Modules.LogHelper;
import Modules.Paras;
import Modules.SPUnit;

public class ShowActivity extends BaseActivity {

    private WebView webView1;
    private WebView webView2;
    private Button btn;
    private Handler handler=new Handler();
    private boolean waitDouble = true;
    private Date endTime=new Date();
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn=findViewById(R.id.back);
        //btn.getBackground().setAlpha(0);
        Paras.appContext=this;
        SPUnit spUnit = new SPUnit(ShowActivity.this);
        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        webView1=findViewById(R.id.webView1);
        WebSettings webSetting1=webView1.getSettings();
        webSetting1.setJavaScriptEnabled(true);
        webSetting1.setLoadsImagesAutomatically(true);
        webSetting1.setUseWideViewPort(true);
        webView1.setWebChromeClient(new WebChromeClient());
        //webView1.setBackgroundColor(0); // 设置背景色
        webView2=findViewById(R.id.webView2);
        webView2.setBackgroundColor(0); // 设置背景色
        WebSettings webSetting2=webView2.getSettings();
        webSetting2.setJavaScriptEnabled(true);
        webSetting2.setDomStorageEnabled(true);
        webSetting2.setUseWideViewPort(true);
        webView2.setWebChromeClient(new WebChromeClient());
        webSetting2.setLoadsImagesAutomatically(true);
        webSetting2.setMediaPlaybackRequiresUserGesture(false);
        //webView2.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        //webView2.loadUrl("http://192.168.9.201:14084/selfpc2/app/index.html?id=10024");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(deviceData.getId()>0) {
                    GetProgramData(deviceData.getId());
                    while (true) {
                        Date nowTime=new Date();
                        if(Paras.updateProgram) {
                            endTime=GetProgramData(deviceData.getId());
                            Paras.updateProgram=false;
                        }
                        if(nowTime.getTime()>endTime.getTime()) {
                            Paras.updateProgram=true;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            LogHelper.Error(e);
                        }
                    }
                }

            }
        }).start();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waitDouble == true){
                    waitDouble = false;
                    Thread thread = new Thread(){
                        @Override
                        public void run(){
                            try {
                                sleep(2000);
                                if(waitDouble == false){
                                    waitDouble = true;
                                }
                            } catch (InterruptedException e) {
                                LogHelper.Error(e);
                            }
                        }
                    };
                    thread.start();
                }else{
                    LogHelper.Debug("跳转配置页");
                    waitDouble = true;
                    Paras.first=false;
                    SkipTo(MainActivity.class);
                }
            }
        });
        //截屏，默认隔30分钟截屏一次
    }

    public Date GetProgramData(Long id) {
        Date date=new Date();
        try {
            String jsonStr="";
            try {
                jsonStr = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getProgramData?device_id=" + id);
            } catch (Exception e) {
                LogHelper.Error(e);
            }
            if(!Objects.equals(jsonStr, "")) {
                JSONObject object = new JSONObject(jsonStr);
                StringBuilder url = new StringBuilder(Paras.mulHtmlAddr);
                String wvUrl="";
                JSONArray itemArray = object.getJSONArray("data");
                boolean first=false;
                if(object.getBoolean("success")) {
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
                                if (now.Between(begin, end)&& !first) {
                                    List<String> timeStr= Arrays.asList(endStr.split(":"));
                                    Calendar start = Calendar.getInstance();
                                    int hour= Integer.parseInt(timeStr.get(0));
                                    int minutes= Integer.parseInt(timeStr.get(1));
                                    start.setTime(new Date());
                                    start.set( Calendar.HOUR_OF_DAY,hour);
                                    start.set( Calendar.MINUTE, minutes);
                                    start.set( Calendar.SECOND,0);
                                    date=start.getTime();
                                    url.append("?id=").append(programId);
                                    if(underUrl!=null&& !underUrl.equals("")) {
                                        wvUrl=underUrl;
                                    }
                                    first=true;
                                }
                            }
                        }
                    }
                    String finalWvUrl = wvUrl;
                    ShowActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                webView2.loadUrl(url.toString());
                                webView1.loadUrl(finalWvUrl);
                            } catch (Exception e) {
                                LogHelper.Error(e);
                            }

                        }
                    });
                }
            }
            //Thread.sleep(3000);
        } catch (Exception e) {
            LogHelper.Error(e);
        }
        return date;
    }


}
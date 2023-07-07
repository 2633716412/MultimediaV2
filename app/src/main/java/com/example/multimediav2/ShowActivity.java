package com.example.multimediav2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Utils.DateUtil;
import com.example.multimediav2.Utils.PollingUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Modules.DeviceData;
import Modules.LogHelper;
import Modules.NetWorkUtils;
import Modules.Paras;
import Modules.SPUnit;
import Modules.StringUnit;

public class ShowActivity extends BaseActivity {

    private WebView webView1;
    private WebView webView2;
    private Button btn;
    private EditText et_input;
    private boolean waitDouble = true;
    private Date endTime=new Date();
    public static KeepFocusThread keepFocusThread;
    private Thread programThread;
    private Intent mServiceIntent;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn=findViewById(R.id.back);
        btn.getBackground().setAlpha(0);
        Paras.appContext=this;
        TextView versionText=findViewById(R.id.versionText);

        // 隐藏状态栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //隐藏状态栏时也可以把ActionBar也隐藏掉
        ActionBar actionBar = getActionBar();
        mServiceIntent = new Intent(this, MyService.class);
        SPUnit spUnit = new SPUnit(ShowActivity.this);
        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        LogHelper.Debug("版本："+getVersionName(Paras.appContext)+"IP:"+deviceData.getDevice_ip());
        versionText.setText("IP:"+deviceData.getDevice_ip()+" 版本："+getVersionName(Paras.appContext));
        webView1=findViewById(R.id.webView1);
        WebSettings webSetting1=webView1.getSettings();
        webSetting1.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSetting1.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting1.setMediaPlaybackRequiresUserGesture(false);
        webView1.setWebChromeClient(new WebChromeClient());
        //webView1.setBackgroundColor(0); // 设置背景色
        webView2=findViewById(R.id.webView2);
        webView2.setBackgroundColor(0); // 设置背景色
        WebSettings webSetting2=webView2.getSettings();
        webSetting2.setJavaScriptEnabled(true);
        //webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView2.setWebViewClient(new WebViewClient() {
            // 解决H5的音视频不能自动播放的问题
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //view.loadUrl("javascript:palyVideo()");
            }

            /*@Override
            public void onPageStarted(WebView view, String url,
                                      Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
            }*/
            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //为了使webview加载完数据后resize高度，之所以不放在onPageFinished里，是因为onPageFinished不是每次加载完都会调用
                int w = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                //重新测量
                view.measure(w, h);

            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                handler.proceed();
            }
        });
        webView2.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
        webSetting2.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting2.setLoadsImagesAutomatically(true);
        webSetting2.setDomStorageEnabled(true);
        webSetting2.setAppCacheEnabled(true);
        webSetting2.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting2.setDatabaseEnabled(true);
        webSetting2.setAllowContentAccess(true);
        webSetting2.setAllowFileAccess(true);
        webSetting2.setDefaultTextEncodingName("utf-8");
        webSetting2.setMediaPlaybackRequiresUserGesture(false);
        //webView2.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        //webView2.loadUrl("http://192.168.9.201:14084/selfpc2/app/index.html?id=10024");
        PollingUtil pollingUtil=new PollingUtil(Paras.handler);
        programThread=new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!Objects.equals(deviceData.getSn(), "")) {
                            //GetProgramData(deviceData.getSn());
                            Date nowTime=new Date();
                            if(Paras.updateProgram) {
                                endTime=GetProgramData(deviceData.getSn());
                                Paras.updateProgram=false;
                            }
                            if(nowTime.getTime()>endTime.getTime()) {
                                Paras.updateProgram=true;
                            }
                        }
                    }
                }).start();
            }
        });
        /*mRefreshRunnable=new Runnable(){
            @Override
            public void run() {

            }
        };*/
        pollingUtil.startPolling(programThread,5 * 1000,false);
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
                    if(null != webView2) {
                        webView2.onPause();
                    }
                    if(null!=webView1) {
                        webView1.onPause();
                    }
                    webView2.loadUrl("about:blank");
                    webView2.stopLoading();
                    webView2.setWebChromeClient(null);
                    webView2.setWebViewClient(null);
                    webView2.destroy();
                    webView2 = null;
                    freshThead.interrupt();
                    keepFocusThread.interrupt();
                    SkipTo(MainActivity.class);
                }
            }
        });
        et_input = findViewById(R.id.et_input);
        et_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            Date last = new Date();

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String text = v.getText().toString();

                if (StringUnit.isEmpty(text)) {
                    return true;
                }

                Date now = new Date();
                long n = now.getTime();
                long l = last.getTime();

                if (n - l < 2000) {
                    v.setText("");
                    return true;
                } else {
                    last = now;
                }

                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Paras.msgManager.SendMsg("扫：" + text);
                    try{
                        Checkin(text);
                    } catch (Exception e) {
                        LogHelper.Error("扫码异常"+e);
                    }

                    v.setText("");
                    return true;
                } else {
                    return false;
                }
            }
        });
        keepFocusThread = new KeepFocusThread();
        freshThead.start();
        keepFocusThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在 Activity 可见时开始执行刷新任务
        Paras.handler.post(programThread);
        stopService(mServiceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在 Activity 不可见时移除刷新任务，避免内存泄漏
        Paras.handler.removeCallbacks(programThread);
        startService(mServiceIntent);
    }

    public Date GetProgramData(String sn) {
        Date date=new Date();
        try {
            //boolean isStopped=false;
            //while (!isStopped) {
                String jsonStr="";
                try {
                    jsonStr = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getProgramData?sn=" + sn);
                    //isStopped=true;
                } catch (Exception e) {
                    LogHelper.Error("获取节目异常："+e);
                    Paras.updateProgram=true;
                }
                if(!Objects.equals(jsonStr, "")) {
                    JSONObject object = new JSONObject(jsonStr);
                    StringBuilder url = new StringBuilder(Paras.mulHtmlAddr);
                    StringBuilder wvUrl=new StringBuilder("");
                    JSONArray itemArray = object.getJSONArray("data");
                    final boolean[] first = {false};
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
                                    if (now.Between(begin, end)&& !first[0]) {
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
                                    webView2.loadUrl(url.toString());
                                    webView1.loadUrl(wvUrl.toString());
                                } catch (Exception e) {
                                    LogHelper.Error(e);
                                }

                            }
                        });
                    }
                //}
            }

        } catch (Exception e) {
            LogHelper.Error(e);
        }
        return date;
    }

    public class KeepFocusThread extends Thread {
        public boolean stop = false;

        @Override
        public void run() {
            super.run();
            while (!stop) {
                try {
                    Thread.sleep(500);
                    et_input.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_input.setFocusable(true);
                            et_input.setFocusableInTouchMode(true);
                            et_input.requestFocus();

                            //Log.e("KeepFocusThread", "requestFocus!!!");

                        }
                    }, 100);
                } catch (Exception ex) {
                    LogHelper.Error(ex);
                }
            }
        }
    }

    private void Checkin(String cardNo) {
        webView2.post(new Runnable() {
            @Override
            public void run() {
                String ip = NetWorkUtils.GetIP(Paras.appContext);
                LogHelper.Debug("checkin " + ip + " " + cardNo);
                webView2.loadUrl("javascript:checkin(\"" + ip + "\"," + "\"" + cardNo + "\")");
            }
        });
    }

    protected void Fresh() {
        webView1.post(new Runnable() {
            @Override
            public void run() {
                String ip = NetWorkUtils.GetIP(Paras.appContext);
                try {
                    LogHelper.Debug("调用refresh："+ip);
                    webView1.loadUrl("javascript:refresh(\"" + ip + "\")");
                } catch (Exception e)
                {
                    LogHelper.Error(e.toString());
                }

            }
        });
    }

    private FreshThead freshThead = new FreshThead();

    private class FreshThead extends Thread {

        volatile boolean stop = false;

        @Override
        public void run() {

            while (!stop) {
                try {
                    Fresh();
                    Thread.sleep(5000);
                } catch (Exception ex) {
                    LogHelper.Error(ex);
                }
            }
        }
    }
    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    private String getVersionName(Context context) {
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
}
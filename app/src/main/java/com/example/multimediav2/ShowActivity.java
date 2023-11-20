package com.example.multimediav2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Utils.DateUtil;
import com.example.multimediav2.Utils.NetWorkUtils;
import com.example.multimediav2.Utils.PollingUtil;
import com.example.multimediav2.Utils.VerifyUtil;
import com.example.multimediav2.Utils.VideoUrlParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Modules.DeviceData;
import Modules.LogHelper;
import Modules.Paras;
import Modules.SPUnit;
import Modules.StringUnit;
import Modules.TextSpeaker2;

public class ShowActivity extends BaseActivity {

    private WebView webView1;
    private WebView webView2;
    private Button btn;
    private EditText et_input;
    private boolean waitDouble = true;
    public static KeepFocusThread keepFocusThread;
    private SendRunnable sendRunnable;
    private Thread programThread;
    //private Intent mServiceIntent;
    private int pageFinish=0;
    private int focusCount=0;
    private PollingUtil pollingUtil=new PollingUtil(Paras.handler);
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn=findViewById(R.id.back);
        btn.getBackground().setAlpha(0);
        Paras.textSpeaker2=new TextSpeaker2(Paras.appContext);
        //Paras.appContext=this;
        TextView versionText=findViewById(R.id.versionText);

        // 隐藏状态栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //隐藏状态栏时也可以把ActionBar也隐藏掉
        ActionBar actionBar = getActionBar();
        try {
            Paras.powerManager.StatusBar();
        } catch (Exception e) {
            LogHelper.Error("隐藏导航栏失败："+e.getMessage());
        }

        //mServiceIntent = new Intent(this, MyService.class);
        SPUnit spUnit = new SPUnit(ShowActivity.this);
        DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        LogHelper.Debug("版本："+NetWorkUtils.getVersionName(Paras.appContext)+"IP:"+deviceData.getDevice_ip());
        versionText.setText("IP:"+deviceData.getDevice_ip()+" 版本："+NetWorkUtils.getVersionName(Paras.appContext));
        webView1=findViewById(R.id.webView1);
        //webView1.getSettings().setLoadsImagesAutomatically(true); // 自动加载图片
        //webView1.getSettings().setAppCacheEnabled(true); // 启用应用程序缓存
        webView1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageFinish++;
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 当加载失败时，重复刷新页面
                //view.reload();
                LogHelper.Error("WebViewClient.onReceivedError："+error.toString());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 在这里处理链接的打开操作，可以使用 WebView 来加载链接
                view.loadUrl(url);
                return true; // 返回 true 表示已经处理了链接的打开操作
            }
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                // 处理HTTP加载错误
                LogHelper.Error("onReceivedHttpError");
            }
        });
        webView1.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
            @Deprecated
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                LogHelper.Error("webView1.onReceivedError："+error.toString());
                //view.reload();
            }
        });
        WebSettings webSetting1=webView1.getSettings();
        webSetting1.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        // 启用链接预览模式
        webSetting1.setSupportMultipleWindows(true);
        webSetting1.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting1.setMediaPlaybackRequiresUserGesture(false);
        //webSetting1.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //WebView的debug日志级别关闭
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView1.setWebContentsDebuggingEnabled(false);
        }
        //禁止缩放
        webSetting1.setBuiltInZoomControls(false);
        webSetting1.setSupportZoom(false);
        webSetting1.setLoadsImagesAutomatically(true);
        webSetting1.setDomStorageEnabled(true);
        //缓存
        /*webSetting2.setAppCacheEnabled(true);
        webSetting2.setCacheMode(WebSettings.LOAD_DEFAULT);*/

        webSetting1.setDatabaseEnabled(true);
        webSetting1.setAllowContentAccess(true);
        webSetting1.setAllowFileAccess(true);
        webSetting1.setCacheMode(WebSettings.LOAD_NO_CACHE);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        LogHelper.Debug("屏幕分辨率：宽"+screenWidth+"高"+screenHeight);
        if(screenWidth>0&&screenHeight>0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) webView1.getLayoutParams();
            params.width = dip2px(this, screenWidth);
            params.height = dip2px(this, screenHeight);
            // params.setMargins(dip2px(MainActivity.this, 1), 0, 0, 0); // 可以实现设置位置信息，如居左距离，其它类推
            // params.leftMargin = dip2px(MainActivity.this, 1);
            webView1.setLayoutParams(params);
        }
        webView1.getSettings().setUseWideViewPort(false);
        webView1.getSettings().setLoadWithOverviewMode(false);
        webView1.setInitialScale(100); // 100% 缩放级别

        webView2=findViewById(R.id.webView2);
        webView1.setBackgroundColor(0);// 设置背景色
        webView2.setBackgroundColor(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView2.setWebContentsDebuggingEnabled(false);
        }
        webView2.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageFinish++;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //LogHelper.Debug("请求的地址："+request.getUrl().toString());
                    if (VideoUrlParser.isVideoResource(request.getUrl().toString())) {
                        // 如果是视频资源，尝试从缓存加载
                        VideoUrlParser.downloadVideo(request.getUrl().toString());
                        InputStream cachedVideoInputStream = VideoUrlParser.getCachedVideo(request.getUrl().toString());
                        if (cachedVideoInputStream != null) {
                            return new WebResourceResponse("video/mp4", "UTF-8", cachedVideoInputStream);
                        }
                    } else if(VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                        VideoUrlParser.downloadVideo(request.getUrl().toString());
                        InputStream cachedVideoInputStream = VideoUrlParser.getCachedVideo(request.getUrl().toString());
                        if (cachedVideoInputStream != null) {
                            String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                            if(pre.equals("png")) {
                                return new WebResourceResponse("image/png", "UTF-8", cachedVideoInputStream);
                            } else if(pre.equals("jpg")){
                                return new WebResourceResponse("image/jpg", "UTF-8", cachedVideoInputStream);
                            } else if(pre.equals("jpeg")){
                                return new WebResourceResponse("image/jpeg", "UTF-8", cachedVideoInputStream);
                            } else if(pre.equals("bmp")){
                                return new WebResourceResponse("image/bmp", "UTF-8", cachedVideoInputStream);
                            } else if(pre.equals("gif")){
                                return new WebResourceResponse("image/gif", "UTF-8", cachedVideoInputStream);
                            }
                        }
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }
            /*@Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                // 当加载失败时，重复刷新页面
                view.reload();
            }*/
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; // 返回 true 表示已经处理了链接的打开操作
            }
        });
        webView2.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
            @Deprecated
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                LogHelper.Error("webView2.onReceivedError："+error.toString());
                //view.reload();
            }
        });

        WebSettings webSetting2=webView2.getSettings();
        webSetting2.setJavaScriptEnabled(true);
        webView2.getSettings().setUseWideViewPort(true);
        webView2.getSettings().setLoadWithOverviewMode(true);
        //webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //webSetting2.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 启用链接预览模式
        webSetting2.setSupportMultipleWindows(true);
        webSetting2.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting2.setLoadsImagesAutomatically(true);
        webSetting2.setDomStorageEnabled(true);
        //缓存
        /*webSetting2.setAppCacheEnabled(true);
        webSetting2.setCacheMode(WebSettings.LOAD_DEFAULT);*/

        webSetting2.setDatabaseEnabled(true);
        webSetting2.setAllowContentAccess(true);
        webSetting2.setAllowFileAccess(true);
        webSetting2.setDefaultTextEncodingName("utf-8");
        //webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webSetting2.setMediaPlaybackRequiresUserGesture(false);
        //webView2.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        //webView2.loadUrl("http://192.168.9.201:14084/selfpc2/app/index.html?id=10024");
        //PollingUtil pollingUtil=new PollingUtil(Paras.handler);
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
                                GetProgramData(deviceData.getSn());
                                //Paras.updateProgram=false;
                            }
                            if(Paras.programEndDate!=null&&nowTime.getTime()>Paras.programEndDate.getTime()) {
                                LogHelper.Debug("节目结束时间："+Paras.programEndDate);

                                Paras.underUrl="";
                                Paras.programUrl="";
                                Paras.updateProgram=true;
                            }
                        }
                    }
                },"programChildThread").start();
            }
        });
        programThread.setName("programThread");
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
                        webView2.setWebChromeClient(null);
                        webView2.setWebViewClient(null);
                        webView2.onPause();
                    }
                    if(null!=webView1) {
                        webView1.onPause();
                    }
                    SkipTo(MainActivity.class);
                    //SkipTo(VerifyActivity.class);
                }
            }
        });
        et_input = findViewById(R.id.et_input);

        et_input.postDelayed(new Runnable() {
            @Override
            public void run() {
                et_input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_input.getWindowToken(), 0);
                boolean isKeyboardHidden = !imm.isAcceptingText();
                if(isKeyboardHidden) {
                    et_input.clearFocus();
                }
            }
        }, 200); // 200毫秒延迟
        et_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            Date last = new Date();

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String text = v.getText().toString();
                LogHelper.Debug("扫码值"+text);
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
        freshThead.setName("freshThread");
        pollingUtil.startPolling(freshThead,5000,false);
        keepFocusThread = new KeepFocusThread();
        keepFocusThread.setName("keepFocusThread");
        pollingUtil.startPolling(keepFocusThread,500,false);
        sendRunnable=new SendRunnable();
        sendRunnable.setName("sendRunnable");
        pollingUtil.startPolling(sendRunnable,5000,false);
        //webView2.addJavascriptInterface(new MyJavaScriptInterface(), "Android");
        /*keepFocusThread = new KeepFocusThread();
        freshThead.start();
        keepFocusThread.start();*/
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 在返回按钮点击时显示退出确认对话框
        VerifyUtil.showExitConfirmationDialog2(this,this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Paras.updateProgram=true;
        LogHelper.Debug("刷新");
        // 在 Activity 可见时开始执行刷新任务
        /*Paras.underUrl="";
        Paras.programUrl="";
        pollingUtil.startPolling(programThread,5 * 1000,false);*/
        //Paras.handler.post(programThread);
        //stopService(mServiceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //验证密码
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 在这里显示对话框
                SkipTo(VerifyActivity.class);
            }
        }, 500); // 延迟0.5秒显示对话框，可以根据需要调整延迟时间*/

        // 在 Activity 不可见时移除刷新任务，避免内存泄漏
        Paras.underUrl="";
        Paras.programUrl="";
        Paras.updateProgram=true;
        /*freshThead.interrupt();
        keepFocusThread.interrupt();*/
    }
    /**
     * dp转为px
     * @param context  上下文
     * @param dipValue dp值
     * @return
     */
    private int dip2px(Context context,float dipValue)
    {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Paras.underUrl="";
        Paras.programUrl="";
        pollingUtil.endPolling(programThread);
        if(null != webView2) {
            webView2.setWebChromeClient(null);
            webView2.setWebViewClient(null);
            webView2.onPause();
        }
        if(null!=webView1) {
            webView1.setWebChromeClient(null);
            webView1.setWebViewClient(null);
            webView1.onPause();
        }
        pollingUtil.endPolling(freshThead);
        pollingUtil.endPolling(keepFocusThread);
        pollingUtil.endPolling(sendRunnable);
        Paras.heartThread.IsStop();
    }

    public void GetProgramData(String sn) {
        try {
            String jsonStr="";
            try {
                if(NetWorkUtils.isNetworkAvailable(Paras.appContext)) {
                    jsonStr = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getProgramData?sn=" + sn);
                } else {
                    throw new Exception("NetWorkUtils网络异常");
                }

            } catch (Exception e) {
                throw new Exception("获取节目异常："+e);
            }
            if(!Objects.equals(jsonStr, "")) {
                JSONObject object = new JSONObject(jsonStr);
                StringBuilder url = new StringBuilder(Paras.mulHtmlAddr);
                StringBuilder wvUrl=new StringBuilder("");
                JSONArray itemArray = object.getJSONArray("data");
                final boolean[] first = {false};
                if(object.getBoolean("success")) {
                    if(pageFinish>0) {
                        Paras.updateProgram=false;
                    }
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
                    if(Paras.devType.equals(Paras.HAI_KANG)||Paras.devType.equals(Paras.HAI_KANG_RK3128)) {
                        //重新加载
                        webView1.post(new Runnable() {
                            @Override
                            public void run() {
                                webView1.reload();
                            }
                        });
                        webView2.post(new Runnable() {
                            @Override
                            public void run() {
                                webView2.reload();
                            }
                        });
                    }
                    //Paras.updateProgram=false;
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
                                    ShowActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                if(Paras.devType.equals(Paras.DEVA40_XiPin)) {
                                                    webView2.loadUrl(url.toString());
                                                    webView2.requestFocus();
                                                    webView1.loadUrl(wvUrl.toString());
                                                    webView1.requestFocus();

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
                                                }

                                            } catch (Exception e) {
                                                LogHelper.Error("更新url"+e.toString());
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    }


                } else if(object.getBoolean("success")) {
                    LogHelper.Error("GetProgramData接口返回错误"+jsonStr);
                }
            }

        } catch (Exception e) {
            LogHelper.Error("GetProgramData"+e.toString());
        }
    }

    public class KeepFocusThread extends Thread {
        public boolean stop = false;

        @Override
        public void run() {
            super.run();
            try {
                //Thread.sleep(500);
                et_input.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        et_input.setFocusable(true);
                        et_input.setFocusableInTouchMode(true);
                        et_input.requestFocus();
                        focusCount++;
                        //et_input.setVisibility(View.INVISIBLE);//影响输入数据
                        if(focusCount>20) {
                            LogHelper.Debug("KeepFocusThread", "requestFocus!!!");
                            focusCount=0;
                        }
                        //LogHelper.Debug("KeepFocusThread", "requestFocus!!!");

                    }
                }, 100);
            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
            /*while (!stop) {
                try {
                    //Thread.sleep(500);
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
            }*/
        }
    }

    private void Checkin(String cardNo) {
        webView1.post(new Runnable() {
            @Override
            public void run() {
                String ip = NetWorkUtils.GetIP(Paras.appContext);
                LogHelper.Debug("checkin " + ip + " " + cardNo);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView1.evaluateJavascript("javascript:checkin(\"" + ip + "\"," + "\"" + cardNo + "\")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String result) {
                            // result 包含 JavaScript 方法的返回值
                            if (result != null) {
                                // 处理 JavaScript 方法的返回值
                                // result 是一个包含返回值的字符串
                                // 在这里你可以根据返回值执行相应的操作
                            }
                        }
                    });
                } else  {
                    webView1.loadUrl("javascript:checkin(\"" + ip + "\"," + "\"" + cardNo + "\")");
                }
            }
        });
    }

    protected void Fresh() {
        webView1.post(new Runnable() {
            @Override
            public void run() {
                String ip = NetWorkUtils.GetIP(Paras.appContext);
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView1.evaluateJavascript("javascript:refresh(\"" + ip + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String result) {
                                // result 包含 JavaScript 方法的返回值
                                if (result != null) {
                                    // 处理 JavaScript 方法的返回值
                                    // result 是一个包含返回值的字符串
                                    // 在这里你可以根据返回值执行相应的操作
                                }
                            }
                        });
                    } else  {
                        webView1.loadUrl("javascript:refresh(\"" + ip + "\")");
                    }
                } catch (Exception e) {
                    LogHelper.Error("refresh"+e.toString());
                }

            }
        });
    }

    private FreshThead freshThead = new FreshThead();

    private class FreshThead extends Thread {

        volatile boolean stop = false;

        @Override
        public void run() {
            Fresh();
        }
    }

    private class SendRunnable extends Thread {
        @Override
        public void run() {
            sendTimeData(); // 发送时间数据的方法
            //Paras.handler.postDelayed(this, 5000); // 间隔5秒
        }
    };
    public void sendTimeData() {
        // 创建一个包含时间数据的 Intent
        Intent intent = new Intent();
        intent.setAction("com.example.ACTION_SEND_TIME");
        intent.putExtra("time", System.currentTimeMillis());
        // 指定服务的包名和类名
        //intent.setComponent(new ComponentName("com.example.appmonitor", "com.example.appmonitor.AppMonitorService"));
        sendBroadcast(intent);
    }
    // JavaScript 接口类
    /*public class MyJavaScriptInterface {
        @JavascriptInterface
        public void videoClick() {
            // 在这里执行你的点击操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.Debug("调用videoClick");
                    webView2.requestFocus();
                    //点击方法
                    //webView2.performClick(); // 调用webView的 performClick 方法
                }
            });
        }
    }*/
}
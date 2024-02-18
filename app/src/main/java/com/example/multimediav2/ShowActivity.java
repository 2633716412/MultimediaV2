package com.example.multimediav2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
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

import androidx.core.content.FileProvider;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;
import com.example.multimediav2.Models.CmdManager;
import com.example.multimediav2.Models.NetworkChangeReceiver;
import com.example.multimediav2.Models.ResSize;
import com.example.multimediav2.Utils.DateUtil;
import com.example.multimediav2.Utils.NetWorkUtils;
import com.example.multimediav2.Utils.VerifyUtil;
import com.example.multimediav2.Utils.VideoUrlParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Modules.DeviceData;
import Modules.EDate;
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
    private int clickCount  = 0;
    private long lastClickTime = 0;
    private int pageFinish=0;
    private int focusCount=0;
    private int programCount=0;
    private NetworkChangeReceiver receiver;
    public static List<ResSize> resList;
    public static CmdManager cmdManager;
    private InputStream fileInputStream = null;
    public static int sum=0;
    private boolean isReload=false;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        btn=findViewById(R.id.back);
        btn.getBackground().setAlpha(0);
        resList = new ArrayList<>();
        Paras.executor = Executors.newScheduledThreadPool(10);
        Paras.textSpeaker2=new TextSpeaker2(Paras.appContext);

        //Paras.appContext=this;
        TextView versionText=findViewById(R.id.versionText);

        // 隐藏状态栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //隐藏状态栏时也可以把ActionBar也隐藏掉
        ActionBar actionBar = getActionBar();

        //mServiceIntent = new Intent(this, MyService.class);
        Paras.spUnit = new SPUnit(Paras.appContext);
        Paras.deviceData = Paras.spUnit.Get("DeviceData", DeviceData.class);
        LogHelper.Debug("版本："+NetWorkUtils.getVersionName(Paras.appContext)+"IP:"+Paras.deviceData.getDevice_ip());
        versionText.setText("IP:"+Paras.deviceData.getDevice_ip()+" 版本："+NetWorkUtils.getVersionName(Paras.appContext));
        webView1=findViewById(R.id.webView1);
        //webView1.getSettings().setLoadsImagesAutomatically(true); // 自动加载图片
        //webView1.getSettings().setAppCacheEnabled(true); // 启用应用程序缓存
        webView1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageFinish++;
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 在这里处理链接的打开操作，可以使用 WebView 来加载链接
                view.loadUrl(url);
                return true; // 返回 true 表示已经处理了链接的打开操作
            }
        });
        WebSettings webSetting1=webView1.getSettings();
        webSetting1.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        // 启用链接预览模式
        webSetting1.setSupportMultipleWindows(true);
        webSetting1.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSetting1.setMediaPlaybackRequiresUserGesture(false);
        }
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

        webView2.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        //webView2.loadUrl("file:///android_asset/GIF/加载_1920-1080.gif");
        webView1.setBackgroundColor(0);// 设置背景色
        webView2.setBackgroundColor(0);

        //webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView2.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageFinish++;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                LogHelper.Debug("调用shouldInterceptRequest"+isReload);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (VideoUrlParser.isVideoResource(request.getUrl().toString()) || VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                        ResSize resSize = new ResSize();
                        // 创建HTTP连接以发送HEAD请求
                        String urlStr = request.getUrl().toString();
                        URL url = null;
                        try {
                            url = new URL(urlStr);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("HEAD");
                            // 获取Content-Length
                            int contentLength = connection.getContentLength();
                            resSize.setSize(contentLength);
                            connection.disconnect();
                            int indexStart=urlStr.lastIndexOf("/");
                            String filename = urlStr.substring(indexStart); // 生成唯一的文件名
                            LogHelper.Debug("调用资源"+filename);
                            LogHelper.Debug("1111111111111111111111111"+isReload);
                            isReload = false;
                            LogHelper.Debug("2222222222222222222222222"+isReload);
                            String fn=Paras.appContext.getExternalFilesDir("/nf").getPath();
                            File cacheFile = new File(fn, filename); // 缓存目录为应用的内部缓存目录

                            //判断本地文件是否存在且完整
                            if(cacheFile.isFile() && cacheFile.length() >= contentLength) {
                                boolean hasItem = false;
                                for(int i=0;i<resList.size();i++) {
                                    //分段请求的情况
                                    if(resList.get(i).getName().equals(filename)) {
                                        ResSize segRes = resList.get(i);
                                        int size = contentLength + segRes.getSize();
                                        segRes.setSize(size);
                                        resList.set(i,segRes);
                                        hasItem = true;
                                    }
                                }
                                if(!hasItem) {
                                    resSize.setName(filename);
                                    resList.add(resSize);
                                }
                                // 如果文件已存在且有效，则直接从文件读取内容返回给WebView
                                //InputStream fileInputStream = new FileInputStream(cacheFile);
                                Uri contentUri = FileProvider.getUriForFile(Paras.appContext, "com.example.multimediav2.fileProvider", cacheFile);
                                ContentResolver resolver = Paras.appContext.getContentResolver();
                                //InputStream fileInputStream = Paras.appContext.getContentResolver().openInputStream(contentUri);
                                //InputStream fileInputStream = resolver.openInputStream(contentUri);
                                fileInputStream = null;
                                fileInputStream = resolver.openInputStream(contentUri);
                                // 检查是否存在Content-Range请求头
                                /*String rangeHeader = request.getRequestHeaders().get("Range");
                                if (rangeHeader != null && rangeHeader.startsWith("bytes=") && rangeHeader.equals("bytes=0-")) {
                                    // 解析Range值
                                    String[] rangeParts = rangeHeader.substring("bytes=".length()).split("-");
                                    long startByte = Long.parseLong(rangeParts[0]);
                                    long endByte;
                                    if (rangeParts.length > 1) {
                                        endByte = Long.parseLong(rangeParts[1]);
                                    } else {
                                        // 如果只有开始位置，则结束位置为文件长度 - 1
                                        endByte = cacheFile.length() - 1;
                                    }
                                    // 创建一个只读取指定范围的InputStream
                                    fileInputStream.skip(startByte);
                                    return new WebResourceResponse("video/mp4", null, new LimitedInputStream(fileInputStream, endByte - startByte + 1));
                                } else */if (VideoUrlParser.isVideoResource(request.getUrl().toString())) {
                                    return new WebResourceResponse("video/mp4", "UTF-8", fileInputStream);
                                } else if(VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                                    String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                                    return new WebResourceResponse("image/"+pre, "UTF-8", fileInputStream);
                                }

                                /*try (InputStream inputStream = new FileInputStream(cacheFile);
                                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                                    // 这里假设视频文件位于assets目录下，如果不是，请替换为适当的文件读取方式
                                    byte[] buffer = new byte[1024];
                                    int read;
                                    while ((read = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, read);
                                    }

                                    if (VideoUrlParser.isVideoResource(request.getUrl().toString())) {
                                        return new WebResourceResponse("video/mp4", "UTF-8", new ByteArrayInputStream(outputStream.toByteArray()));
                                    } else if(VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                                        String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                                        return new WebResourceResponse("image/"+pre, "UTF-8", new ByteArrayInputStream(outputStream.toByteArray()));
                                    }
                                } catch (Exception e) {
                                    throw new Exception(e.toString());
                                }*/

                            } else {
                                /*// 创建一个临时的输出流用于存储下载的部分数据
                                FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
                                // 创建一个临时的输出流用于存储下载的部分数据
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int len;
                                // 创建一个缓冲输入流
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                                while ((len = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, len);
                                    // 将已下载的部分数据作为WebResourceResponse返回给WebView
                                    if (outputStream.size() > 0) {
                                        InputStream partialStream = new ByteArrayInputStream(outputStream.toByteArray());
                                        if (VideoUrlParser.isVideoResource(request.getUrl().toString())) {
                                            return new WebResourceResponse("video/mp4", "UTF-8", partialStream);
                                        } else if(VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                                            String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                                            return new WebResourceResponse("image/"+pre, "UTF-8", partialStream);
                                        }
                                        // 同时将数据写入到本地文件
                                        fileOutputStream.write(buffer, 0, len);
                                    }
                                }*/
                                VideoUrlParser.downloadVideo(request.getUrl().toString(),resSize.getSize());
                                /*InputStream cachedVideoInputStream = VideoUrlParser.getCachedVideo(request.getUrl().toString());
                                if (cachedVideoInputStream != null) {
                                    String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                                    return new WebResourceResponse("image/"+pre, "UTF-8", cachedVideoInputStream);
                                }*/

                                // 下载完成后流
                                /*inputStream.close();
                                fileOutputStream.close(); // 关闭FileOutputStream
                                connection.disconnect();//关闭连接*/
                            }


                            //只从本地加载
                        /*if (VideoUrlParser.isVideoResource(request.getUrl().toString())) {
                            // 如果是视频资源，尝试从缓存加载
                            String videoName = VideoUrlParser.downloadVideo(request.getUrl().toString(),resSize.getSize());
                            boolean hasItem = false;
                            for(int i=0;i<resList.size();i++) {
                                if(resList.get(i).getName().equals(videoName)) {
                                    hasItem = true;
                                }
                            }
                            if(!hasItem) {
                                resSize.setName(videoName);
                                resList.add(resSize);
                            }

                            InputStream cachedVideoInputStream = VideoUrlParser.getCachedVideo(request.getUrl().toString());
                            if (cachedVideoInputStream != null) {
                                return new WebResourceResponse("video/mp4", "UTF-8", cachedVideoInputStream);
                            }
                        } else if(VideoUrlParser.isPictureResource(request.getUrl().toString())) {
                            String photoName = VideoUrlParser.downloadVideo(request.getUrl().toString(),resSize.getSize());
                            boolean hasItem = false;
                            for(int i=0;i<resList.size();i++) {
                                if(resList.get(i).getName().equals(photoName)) {
                                    hasItem = true;
                                }
                            }
                            if(!hasItem) {
                                resSize.setName(photoName);
                                resList.add(resSize);
                            }
                            InputStream cachedVideoInputStream = VideoUrlParser.getCachedVideo(request.getUrl().toString());
                            if (cachedVideoInputStream != null) {
                                String pre=VideoUrlParser.getPhotoPre(request.getUrl().toString());
                                return new WebResourceResponse("image/"+pre, "UTF-8", cachedVideoInputStream);
                            }
                        }*/
                        } catch (Exception e) {
                            LogHelper.Error("获取资源异常"+e.toString());
                        }
                    }
                }

                return null;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return true;
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
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // 通过title获取判断
                LogHelper.Debug("网页标题："+title);
                if (title.contains("ERR_CONNECTION_REFUSED") || title.contains("找不到网页") || title.contains("网页无法加载")||title.contains("网页无法打开")) {
                    Paras.updateProgram=true;
                }
            }

        });

        WebSettings webSetting2=webView2.getSettings();
        webSetting2.setJavaScriptEnabled(true);
        //webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //webSetting2.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 启用链接预览模式
        /*webSetting2.setSupportMultipleWindows(true);
        webSetting2.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting2.setLoadsImagesAutomatically(true);
        //webSetting2.setDomStorageEnabled(true);
        // 设置布局算法，允许页面内容适应屏幕尺寸
        webSetting2.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        // 设置支持viewport属性，使得网页可以自适应屏幕宽度
        webSetting2.setUseWideViewPort(true);
        webSetting2.setLoadWithOverviewMode(true); // 缩放至屏幕大小
        *//*if(Paras.devType.equals(Paras.HAI_KANG)) {
            webSetting2.setDomStorageEnabled(false);
            *//**//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSetting2.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }*//**//*
        }*//*

        //缓存
        webSetting2.setAppCacheEnabled(false);
        webSetting2.setDomStorageEnabled(false);
        webSetting2.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting2.setDatabaseEnabled(false);
        webSetting2.setAllowContentAccess(true);
        webSetting2.setAllowFileAccess(true);
        webSetting2.setDefaultTextEncodingName("utf-8");
        // 设置支持缩放，与系统浏览器一致
        webSetting2.setSupportZoom(true);
        webSetting2.setBuiltInZoomControls(true); // 显示缩放控件（对于API 11+）
        webSetting2.setDisplayZoomControls(false); // 隐藏缩放控件（如果使用了自定义手势缩放则可添加）*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSetting2.setMediaPlaybackRequiresUserGesture(false);
        }
        //webView2.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentClickTime = System.currentTimeMillis();
                if (currentClickTime - lastClickTime <= 1000) {
                    clickCount++;
                    if (clickCount == 10) {
                        // 执行操作
                        LogHelper.Debug("跳转配置页");
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
                        clickCount = 0; // 重置计数器
                    } else if(clickCount==5){
                        //int residue=10-clickCount;
                        Paras.msgManager.SendMsg("连续点击将进入配置页");
                        //Toast.makeText(getApplicationContext(),"连续点击将进入配置页，剩余"+residue+"次",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    clickCount = 1; // 重新计数
                }
                lastClickTime = currentClickTime;
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
        // 注册网络状态监听器
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver(webView1,webView2);
        registerReceiver(receiver, filter);
        Paras.executor.scheduleAtFixedRate(FreshThread,12,5,TimeUnit.SECONDS);
        Paras.executor.scheduleAtFixedRate(KeepFocusThread,15000,1000,TimeUnit.MILLISECONDS);
        //Paras.executor.scheduleAtFixedRate(programTask,30,5, TimeUnit.SECONDS);
        Paras.executor.scheduleAtFixedRate(programTask,10,5, TimeUnit.SECONDS);
        cmdManager = new CmdManager();
        cmdManager.Init(Paras.appContext, null);
        try {
            Paras.powerManager.StatusBar();
        } catch (Exception e) {
            LogHelper.Error("隐藏导航栏失败："+e.getMessage());
        }
        webView2.addJavascriptInterface(new MyJavaScriptInterface(), "Android");
        //Paras.executor.scheduleAtFixedRate(reloadTask2,2,2, TimeUnit.MINUTES);
        Paras.executor.scheduleAtFixedRate(reloadTask2,10,1, TimeUnit.SECONDS);
        //Paras.executor.scheduleAtFixedRate(reloadTask,1,1, TimeUnit.SECONDS);
    }
    @Override
    public void onBackPressed() {
        VerifyUtil.showExitConfirmationDialog2(this,this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Paras.updateProgram=true;
        webView2.onResume();
        webView1.onResume();
        LogHelper.Debug("刷新");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView1.onPause();
        webView2.onPause();
        // 在 Activity 不可见时移除刷新任务，避免内存泄漏
        Paras.underUrl="";
        Paras.programUrl="";
        Paras.updateProgram=true;
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
        if(null != webView2) {
            webView2.setWebChromeClient(null);
            webView2.setWebViewClient(null);
            webView2.stopLoading(); // 停止加载任何内容
            webView2.removeAllViews(); // 从父容器移除WebView
            webView2.onPause(); // 如果适用，调用pause相关方法
            webView2.destroy(); // 关键步骤，销毁WebView并释放其所有资源
        }
        if(null!=webView1) {
            webView1.setWebChromeClient(null);
            webView1.setWebViewClient(null);
            webView1.stopLoading(); // 停止加载任何内容
            webView1.removeAllViews(); // 从父容器移除WebView
            webView1.onPause(); // 如果适用，调用pause相关方法
            webView1.destroy(); // 关键步骤，销毁WebView并释放其所有资源

        }

        Paras.executor.shutdownNow();
        resList=null;
        cmdManager = null;
        // 注销BroadcastReceiver
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null; // 将引用置为null，帮助垃圾回收
        }
        if(programTask!=null) {
            programTask = null;
        }
        if(KeepFocusThread!=null) {
            KeepFocusThread=null;
        }
        if(fileInputStream!=null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {

            }
        }
        LogHelper.Debug("节目跳转onDestroy");
        super.onDestroy();
    }

    public void GetProgramData(String sn) {
        try {
            String jsonStr="";
            try {
                if(NetWorkUtils.isNetworkAvailable(Paras.appContext)) {
                    jsonStr = HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getProgramData?sn=" + sn);
                } else {
                    Paras.msgManager.SendMsg("网络异常");
                    throw new Exception("NetWorkUtils网络异常");
                }

            } catch (Exception e) {
                Paras.msgManager.SendMsg("网络异常");
                throw new Exception("获取节目异常："+e);
            }
            if(!Objects.equals(jsonStr, "")) {
                JSONObject object = new JSONObject(jsonStr);
                StringBuilder url = new StringBuilder(Paras.mulHtmlAddr);
                StringBuilder wvUrl=new StringBuilder("");
                JSONArray itemArray = object.getJSONArray("data");
                if(object.getBoolean("success")) {
                    Paras.programUrl="";
                    Paras.underUrl="";
                    if(Paras.devType.equals(Paras.HAI_KANG)) {
                        webView2.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap screenshot = NetWorkUtils.captureWebView(webView2);
                                boolean isWhite = NetWorkUtils.isWhiteScreen(screenshot);
                                LogHelper.Debug("当前是否白屏："+isWhite);
                                if(!isWhite) {
                                    //最后停止刷新之前
                                    Paras.updateProgram=false;
                                }
                            }
                        });

                    } else {
                        if(pageFinish>0) {
                            //最后停止刷新之前
                            Paras.updateProgram=false;
                        }
                    }

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
                    //Paras.updateProgram=false;
                    for (int i = 0; i < itemArray.length(); i++) {
                        JSONObject object1 = itemArray.getJSONObject(i);
                        String repeatDay = object1.getString("repet_day");
                        Long programId = object1.getLong("program_id");
                        String underUrl=object1.getString("under_url");
                        int material_count = object1.optInt("material_count", 0);
                        Paras.material_count=material_count;

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
                                if (now.Between(begin, end)) {
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
                                    ShowActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                webView2.loadUrl(url.toString());
                                                webView1.loadUrl(wvUrl.toString());
                                                LogHelper.Debug("url："+url.toString());
                                                LogHelper.Debug("wvUrl："+wvUrl.toString());
                                                Paras.programUrl = url.toString();
                                                Paras.underUrl = wvUrl.toString();
                                                webView2.setVisibility(View.VISIBLE);
                                                webView1.setVisibility(View.VISIBLE);
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
    public Runnable programTask=new Runnable() {
        @Override
        public void run() {
            if(!Objects.equals(Paras.deviceData.getSn(), "")) {
                try {
                    Date nowTime=new Date();
                    programCount++;
                    if(resList != null) {
                        LogHelper.Debug("节目素材数"+Paras.material_count+"加载数量"+resList.size());
                        //判断节目素材是否加载完成
                        String fn=Paras.appContext.getExternalFilesDir("/nf/cache").getPath();

                        if(Paras.material_count > 0 && resList.size() == Paras.material_count) {
                            boolean finish=false;
                            for(int i=0;i<resList.size();i++) {
                                File file=new File(fn,resList.get(i).getName());
                                if(file.length()!=resList.get(i).getSize()) {
                                    finish=false;
                                } else {
                                    finish=true;
                                }
                            }
                            if(finish) {
                                Paras.material_finish = true;
                                LogHelper.Debug("节目加载完成");
                            }
                        }
                    }


                    //et_input.setVisibility(View.INVISIBLE);//影响输入数据
                    if(programCount>2) {
                        try{
                            //内存使用情况
                            ActivityManager activityManager = (ActivityManager) Paras.appContext.getSystemService(Context.ACTIVITY_SERVICE);
                            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                            activityManager.getMemoryInfo(memoryInfo);
                            long totalMemory = memoryInfo.totalMem/1024/1024;
                            long availableMemory = memoryInfo.availMem/1024/1024;
                            boolean lowMemory=memoryInfo.lowMemory;
                            long threshold=memoryInfo.threshold/1024/1024;
                            long availableThreshold = threshold+50;
                            //低于阈值的时候重新启动
                            if(availableMemory<availableThreshold) {
                                LogHelper.Debug("可用内存:"+availableMemory+"M阈值："+threshold+"M设备可用运行空间低于阈值，即将重启");
                                ShowActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView1.reload();
                                        webView2.reload();
                                    }
                                });
                                /*if(Paras.devType.equals(Paras.DEVA40_XiPin)) {
                                    Paras.powerManager.Open();
                                }
                                Paras.powerManager.Reboot();*/
                            }
                            LogHelper.Debug("总内存:"+totalMemory+"M可用内存:"+availableMemory+"M是否低内存："+lowMemory+"阈值："+threshold+"M");
                            //LogHelper.Debug("加载资源："+resList);
                            //半夜23点重启
                            EDate now = EDate.Now();
                            EDate rebootDate = new EDate(now.Year(), now.Month(), now.Day(), 1, 0, 0);
                            EDate rebootEndDate = new EDate(now.Year(), now.Month(), now.Day(), 1, 0, 20);;
                            if(now.Between(rebootDate,rebootEndDate)) {
                                Paras.powerManager.Reboot();
                            }
                            //LogHelper.Debug("堆内存"+android.os.Debug.getNativeHeapAllocatedSize());
                        } catch (Exception e) {

                        }
                        LogHelper.Debug("programTask!!!", "programTask");
                        programCount=0;
                    }
                    if(Paras.updateProgram) {
                        GetProgramData(Paras.deviceData.getSn());
                    }
                    if(Paras.programEndDate!=null&&nowTime.getTime()>Paras.programEndDate.getTime()) {
                        LogHelper.Debug("节目结束时间："+Paras.programEndDate);
                        ShowActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    webView2.setVisibility(View.GONE);
                                    webView1.setVisibility(View.GONE);

                                } catch (Exception e) {
                                    LogHelper.Error("更新url"+e.toString());
                                }

                            }
                        });
                        Paras.material_finish=false;//素材是否加载完成
                        Paras.underUrl="";
                        Paras.programUrl="";
                        Paras.updateProgram=true;
                        resList = null;
                    }
                } catch (Exception e) {
                    LogHelper.Error("节目获取任务异常："+e.toString());
                }

            }
        }
    };
    public Runnable KeepFocusThread =new Runnable() {
        @Override
        public void run() {
            try {
                et_input.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        et_input.setFocusable(true);
                        et_input.setFocusableInTouchMode(true);
                        et_input.requestFocus();
                        focusCount++;
                        //et_input.setVisibility(View.INVISIBLE);//影响输入数据
                        if(focusCount>40) {
                            LogHelper.Debug("KeepFocusThread", "requestFocus!!!");
                            focusCount=0;
                        }
                    }
                }, 100);
            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
        }
    };
    private void Checkin(String cardNo) {
        webView1.post(new Runnable() {
            @Override
            public void run() {
                try{
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
                } catch (Exception e) {
                    LogHelper.Error("checkin调用"+e.toString());
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

    private Runnable FreshThread =new Runnable() {
        @Override
        public void run() {
            Fresh();
        }
    };

    // JavaScript 接口类
    public class MyJavaScriptInterface {
        @JavascriptInterface
        public void reload() {
            // 在这里执行你的点击操作
            LogHelper.Debug("开始reload");
            isReload=true;
            //Paras.executor.schedule(reloadTask,10,TimeUnit.SECONDS);
            /*ShowActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.Debug("调用reload");
                    webView2.reload();
                    webView2.clearHistory();
                    if(Paras.devType.equals(Paras.DEVA40_XiPin) || Paras.devType.equals(Paras.DEVA20_XiPinBox)) {
                        webView2.clearCache(true);
                        webView2.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                    }
                    sum++;
                    webView2.loadUrl(Paras.programUrl+"&i="+sum);
                    //webView2.loadUrl("https://www.baidu.com/");
                }
            });
            */
        }
    }
    public Runnable reloadTask = new Runnable() {
        @Override
        public void run() {
            ShowActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.Debug("调用reload"+isReload);
                    if(isReload) {
                        LogHelper.Debug("reloading");
                        webView2.reload();
                    }
                    //webView2.loadUrl(Paras.programUrl+"&i="+sum);
                }
            });
        }
    };
    public Runnable reloadTask2 = new Runnable() {
        @Override
        public void run() {
            ShowActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.Debug("定时reload"+isReload+System.currentTimeMillis());
//                    webView2.reload();
                    if(isReload) {
                        webView2.reload();
                        Paras.executor.schedule(reloadTask,5,TimeUnit.SECONDS);
                        LogHelper.Debug("完成reload"+System.currentTimeMillis());
                    }
                    //webView2.loadUrl(Paras.programUrl+"&i="+sum);
                }
            });
        }
    };
}
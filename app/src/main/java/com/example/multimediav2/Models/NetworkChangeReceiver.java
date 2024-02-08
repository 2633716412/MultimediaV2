package com.example.multimediav2.Models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebView;

import Modules.LogHelper;
import Modules.Paras;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private WebView mWebView1; // 假设你有一个全局的WebView实例
    private WebView mWebView2;
    public NetworkChangeReceiver() {
        // 默认构造函数
    }
    public NetworkChangeReceiver(WebView webView1,WebView webView2) {
        this.mWebView1 = webView1;
        this.mWebView2 = webView2;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) { // 当网络连接恢复时
                LogHelper.Debug("网络恢复连接");
                // 重新加载WebView内容
                if (mWebView1 != null) {
                    mWebView1.loadUrl(mWebView1.getUrl()); // 加载当前URL
                }
                if (mWebView2 != null) {
                    mWebView2.loadUrl(mWebView2.getUrl()); // 加载当前URL
                }
            } else { // 网络断开，可以在此处做相应的错误提示或者缓存策略
                Paras.msgManager.SendMsg("网络断开");
                LogHelper.Debug("网络断开");
            }
        }
    }
}

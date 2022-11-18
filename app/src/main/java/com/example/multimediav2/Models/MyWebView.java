package com.example.multimediav2.Models;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;


public class MyWebView extends WebView {
    public MyWebView(Context context) {
        super(context);
    }
    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }
    //重写onScrollChanged 方法
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //禁止左右滚动：scrollTo(0,y),禁止上下滚动：scrollTo(x,0)
        scrollTo(0,0);
    }
}

package com.example.multimediav2.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ShutUtil {

    public static byte[] screenshotWebView2(WebView webView) {
        Bitmap bmp = webView.getDrawingCache();
        byte[] drawByte = getBitmapByte(bmp);
        return drawByte;
    }


    //webview截屏
    public static byte[] screenshotWebView(WebView webView) {
        // webView.setDrawingCacheEnabled(true); // 设置缓存
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        webView.destroyDrawingCache();
        byte[] drawByte = getBitmapByte(bitmap);
        // webView.setDrawingCacheEnabled(false); // 清空缓存
        return drawByte;
    }


    // 位图转 Base64 String
    private static String getBitmapString(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream out = null;
        try {
            if (bitmap != null) {
                out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.flush();
                out.close();

                byte[] bitmapBytes = out.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 位图转 Byte
    public static byte[] getBitmapByte(Bitmap bitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 参数1转换类型，参数2压缩质量，参数3字节流资源
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


}

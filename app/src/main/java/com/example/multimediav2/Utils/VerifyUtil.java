package com.example.multimediav2.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.multimediav2.BaseActivity;
import com.example.multimediav2.MainActivity;
import com.example.multimediav2.ShowActivity;
import com.example.multimediav2.VerifyActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import Modules.LogHelper;
import Modules.Paras;

public class VerifyUtil {

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
            LogHelper.Error("getBitmapString1异常："+e.toString());
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                LogHelper.Error("getBitmapString2异常："+e.toString());
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
            LogHelper.Error("getBitmapByte异常："+e.toString());
        }
        return out.toByteArray();
    }
    //跳新activity实现
    public static void showExitConfirmationDialog(BaseActivity activity, Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("密码验证");
        builder.setMessage("请输入密码以退出应用");

        final EditText passwordEditText = new EditText(context);
        builder.setView(passwordEditText);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = passwordEditText.getText().toString();

                if (enteredPassword.equals("klb2017")) {
                    // 输入的密码正确，允许退出应用或跳到配置页
                    if(!Paras.first) {
                        VerifyActivity.openMain();
                    } else {
                        if(activity!=null) {
                            activity.finish();
                        };
                    }

                } else {
                    Toast.makeText(context, "密码错误，请重试", Toast.LENGTH_SHORT).show();
                    showExitConfirmationDialog(activity,context); // 继续显示密码验证对话框
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户取消退出
                if(!Paras.first) {
                    Paras.first=true;
                }
                VerifyActivity.openMain();
            }
        });

        builder.setCancelable(false); // 防止用户点击外部区域取消对话框

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //停留在当前所在activity
    public static void showExitConfirmationDialog2(BaseActivity activity, Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("密码验证");
        builder.setMessage("请输入密码以退出应用");

        final EditText passwordEditText = new EditText(context);
        builder.setView(passwordEditText);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = passwordEditText.getText().toString();

                if (enteredPassword.equals("klb2017")) {
                    // 输入的密码正确，允许退出应用或跳到配置页
                    if(!Paras.first) {
                        ShowActivity.SkipTo(MainActivity.class);
                    } else {
                        if(activity!=null) {
                            activity.finish();
                        };
                    }

                } else {
                    Toast.makeText(context, "密码错误，请重试", Toast.LENGTH_SHORT).show();
                    showExitConfirmationDialog2(activity,context); // 继续显示密码验证对话框
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户取消退出
                if(!Paras.first) {
                    Paras.first=true;
                }
                dialog.dismiss();
            }
        });

        builder.setCancelable(false); // 防止用户点击外部区域取消对话框

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

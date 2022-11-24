package com.example.multimediav2.FileUnit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import Modules.LogHelper;

public class FileUnitDef {

    public void Save(String dir, String fn, byte[] bytes) throws Exception {
        Save(dir, fn, new ByteArrayInputStream(bytes));
    }

    public String ReadToString(String filePathName) throws Exception {

        StringBuffer stringBuffer = new StringBuffer();

        // 打开文件输入流
        FileInputStream fileInputStream = new FileInputStream(filePathName);

        byte[] buffer = new byte[1024];
        int len = fileInputStream.read(buffer);
        // 读取文件内容
        while (len > 0) {
            stringBuffer.append(new String(buffer, 0, len));

            // 继续把数据存放在buffer中
            len = fileInputStream.read(buffer);
        }

        // 关闭输入流
        fileInputStream.close();

        return stringBuffer.toString();
    }

    public void Save(String dir, String fn, ByteArrayInputStream byteArrayInputStream) throws Exception {

        //路径
        {
            File f_dir = new File(dir);
            if (!f_dir.exists()) {
                boolean re = f_dir.mkdirs();

                if (re == false) {
                    LogHelper.Debug("创建目录失败 " + dir);
                    return;
                }
            }
        }

        //写入
        {
            File file = new File(dir, fn);
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);

            byteArrayInputStream.reset();

            byte[] ba = new byte[4096];

            while (byteArrayInputStream.available() > 0) {
                int len = byteArrayInputStream.read(ba);
                fileOutputStream.write(ba, 0, len);
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            byteArrayInputStream.reset();
        }

    }

    public File Read(String filePath) throws Exception {
        File file = new File(filePath);
        return file;
    }

    public void InstallApk(File file, Context context, String fileProvider) throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, fileProvider, file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }

        context.startActivity(intent);
    }

    public void InstallApk(String filePath, Context context, String fileProvider) throws Exception {
        File apkFile = new File(filePath);
        InstallApk(apkFile, context, fileProvider);
    }

    public void Copy(String o, String t) throws Exception {
        File of = new File(o);
        File tf = new File(t);

        if (!of.exists()) return;

        if (tf.exists()) tf.delete();

        FileInputStream from = new FileInputStream(of);
        FileOutputStream to = new FileOutputStream(tf);

        byte ba[] = new byte[1024];
        int len = 0;
        while ((len = from.read(ba)) > 0) {
            to.write(ba, 0, len);
        }

        from.close();
        to.close();
    }
}

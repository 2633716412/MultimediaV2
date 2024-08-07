package com.example.multimediav2.Utils;

import android.os.AsyncTask;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import Modules.LogHelper;
import Modules.Paras;

public class VideoUrlParser {
    public static List<String> extractVideoUrls(String htmlContent) {
        List<String> videoUrls = new ArrayList<>();

        // 使用 Jsoup 解析 HTML 内容
        Document doc = Jsoup.parse(htmlContent);

        // 选择 video 标签，如果有其他标签用于嵌套视频，请相应选择
        Elements videoElements = doc.select("video");

        for (Element videoElement : videoElements) {
            // 获取视频链接
            String videoUrl = videoElement.attr("src");
            if (!videoUrl.isEmpty()) {
                videoUrls.add(videoUrl);
            }
        }

        // 返回提取的视频链接列表
        return videoUrls;
    }
    public static boolean isVideoResource(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".webm") || lowerUrl.endsWith(".avi")
                || lowerUrl.contains("video/");
    }

    public static InputStream getCachedVideo(String videoUrl) {
        int indexStart=videoUrl.lastIndexOf("/");
        String filename = videoUrl.substring(indexStart); // 生成唯一的文件名
        String fn=Paras.appContext.getExternalFilesDir("/nf/cache").getPath();
        File cacheFile = new File(fn, filename); // 缓存目录为应用的内部缓存目录

        try {
            if (cacheFile.isFile()) {
                return new FileInputStream(cacheFile);
            }
        } catch (IOException e) {
            LogHelper.Error("获取视频缓存地址异常："+e);
        }

        return null;
    }

    public static String getPhotoPre(String videoUrl) {
        String pre="";

        int indexEnd=videoUrl.lastIndexOf(".");
        pre=videoUrl.substring(indexEnd+1);
        return pre;
    }

    public static String downloadVideo(String videoUrl,int size) {
        int indexStart=videoUrl.lastIndexOf("/");
        String filename = videoUrl.substring(indexStart); // 生成唯一的文件名
        String fn=Paras.appContext.getExternalFilesDir("/nf").getPath();
        File f_dir = new File(fn);
        if (!f_dir.exists()) {
            boolean re = f_dir.mkdirs();

            if (re == false) {
                LogHelper.Debug("创建目录失败 " + fn);
            }
        }
        File file=new File(fn,filename);
        //LogHelper.Debug("文件缓存地址："+file.getPath());
        if(!file.exists() || file.length() < size) {
            try {
               // LogHelper.Debug("开始缓存");
                //HttpUnitFactory.Get().DownLoad(videoUrl, fn, filename, null,null);
                // 使用示例
                new VideoDownloadTask(videoUrl, fn, filename).execute();
                LogHelper.Debug("文件下载完成");
            } catch (Exception e) {
                LogHelper.Error("文件缓存异常："+e);
            }
        }
        return filename;
    }

    public static boolean isPictureResource(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".png") || lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")||lowerUrl.endsWith(".bmp")||lowerUrl.endsWith(".gif");
    }

    //删除缓存文件
    public static void deleteCacheFile() {
        File file = new File(Paras.appContext.getExternalFilesDir("/nf").getPath());
        // 获取当前目录下的目录和文件
        File[] listFiles = file.listFiles();
        for (File f:listFiles) {
            //判断是否是目录
            if (f.isFile()) {
                LogHelper.Debug("删除文件：" + f.getAbsolutePath());
                f.delete();
            }
        }
    }

    public static class VideoDownloadTask extends AsyncTask<Void, Integer, Boolean> {

        private String videoUrl;
        private String fn;
        private String filename;

        public VideoDownloadTask(String videoUrl, String fn, String filename) {
            this.videoUrl = videoUrl;
            this.fn = fn;
            this.filename = filename;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // 假设 DownLoad 方法内部已经处理了异步下载和进度更新
            try {
                HttpUnitFactory.Get().DownLoad(videoUrl, fn, filename, null,null);
            } catch (Exception e) {
                LogHelper.Error("资源下载异常："+e.toString());
            }

            return true;
        }


    }

}

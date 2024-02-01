package com.example.multimediav2.HttpUnit;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import Modules.Action;
import Modules.LogHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 */
public class HttpUnit_Okhttp implements IHttpUnit {

    static public String proxyip;

    static public int port;

    static public int TIMEOUT = 30;

    // 用于保存已下载文件及大小
    private File tempFile;
    private long downloadedBytes;
    private OkHttpClient client;

    public HttpUnit_Okhttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 添加限流拦截器
        builder.addInterceptor(rateLimitInterceptor);
        //短连接
        builder.connectionPool(connectionPool);

        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS);

        /*if (!StringUnit.isEmpty(proxyip) && port > 0 && usePeoxy) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyip, port));
            builder.proxy(proxy);
        }*/

        client = builder.build();
    }
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    ConnectionPool connectionPool=new ConnectionPool(2, 5, TimeUnit.SECONDS);

    public String Get(String url, boolean usePeoxy) throws Exception {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response=null;
        String res="";
        try {
            response = client.newCall(request).execute();
            res=response.body().string();
        } catch (IOException e) {
            LogHelper.Error(e);
        } finally {
            // Close the response body to release resources
            if (response != null) {
                response.close();
                //connectionPool.evictAll();
            }
        }

        return res;
    }

    public String Get(String url) throws Exception {
        return Get(url, true);
    }

    public String Post(String url, String json, boolean usePeoxy) throws IOException {

        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response=null;
        String res="";
        try {
            response = client.newCall(request).execute();
            res=response.body().string();
        } catch (IOException e) {
            LogHelper.Error(e);
        } finally {
            // Close the response body to release resources
            if (response != null) {
                response.close();
            }
        }

        return res;
    }

    public String Post(String url, String json) throws IOException {
        return Post(url, json, true);
    }

    public void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded) throws Exception {
        DownLoad(url, dir, fn, OnDwonloading, Downloaded, false);
    }

    /*public void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded, boolean usePeoxy) throws Exception {
        // 检查本地文件是否存在，并获取其大小作为已下载的起始位置
        File file = new File(dir, fn);
        downloadedBytes = 0;
        if (file.exists()) {
            downloadedBytes = file.length();
        }
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                // 添加Range头信息以实现断点续传
                .header("Range", "bytes=" + downloadedBytes + "-")
                .build();
        Callback callback = new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                LogHelper.Error(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //路径
                {
                    File f_dir = new File(dir);
                    if (!f_dir.exists())
                        f_dir.mkdirs();
                }

                //写入
                try {
                    File file = new File(dir, fn);
                    FileOutputStream fileOutputStream = new FileOutputStream(file, false);

                    byte[] ba = new byte[4096];

                    Long tempDownloaded = 0L;
                    Long totalDownloaded = 0L;


                    int len = 0;

                    while ((len = response.body().byteStream().read(ba)) > 0) {

                        fileOutputStream.write(ba, 0, len);

                        //下载提示
                        {
                            tempDownloaded += len;

                            if (tempDownloaded > 1024 * 1024) {
                                totalDownloaded += tempDownloaded;
                                tempDownloaded = 0L;

                                if (OnDwonloading != null) {
                                    try {
                                        OnDwonloading.Excute(totalDownloaded);
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }
                    }

                    if (Downloaded != null) {
                        Downloaded.Excute(null);
                    }

                    fileOutputStream.flush();
                    fileOutputStream.close();

                } catch (Exception ex) {
                    LogHelper.Error(ex);
                }
            }
        };

        client.newCall(request).enqueue(callback);
    }*/

    public void Upload(String url, HashMap<String, Object> paras, boolean usePeoxy) throws Exception {

        try {

            MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (String key : paras.keySet()) {
                Object object = paras.get(key);
                if (!(object instanceof File)) {
                    mb.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    mb.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"),file));
                }
            }

            RequestBody requestBody = mb.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response res=null;
            try {
                res = client.newCall(request).execute();
            } catch (IOException e) {
                LogHelper.Error(e);
            } finally {
                // Close the response body to release resources
                if (res != null) {
                    res.close();
                }
            }

            if (!res.isSuccessful()) {
                LogHelper.Error(res.message());
            }

        } catch (Exception ex) {
            LogHelper.Error(ex);
            throw ex;
        }

    }

    // 创建一个限流拦截器
    Interceptor rateLimitInterceptor = new Interceptor() {
        //5Mb/s
        private final Semaphore semaphore = new Semaphore(5000);

        @Override
        public Response intercept(Chain chain) throws IOException {
            try {
                // 尝试获取信号量许可
                semaphore.acquire();
                // 执行请求
                Response response = chain.proceed(chain.request());
                return response;
            } catch (Exception e) {
                //LogHelper.Error("限流拦截器异常："+e.toString());
                throw new IOException("限流拦截器异常："+e.toString());
            } finally {
                // 释放信号量许可
                semaphore.release();
            }
        }
    };

    public void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded, boolean usePeoxy) throws IOException {
        // 检查本地是否有临时文件，如果有则获取已下载的字节数
        tempFile = new File(dir+fn);
        if (tempFile.exists()) {
            downloadedBytes = tempFile.length();
        } else {
            downloadedBytes = 0L;
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close");

        // 如果之前有下载过，则添加Range头以请求剩余部分
        if (downloadedBytes > 0) {
            requestBuilder.header("Range", "bytes=" + downloadedBytes + "-");
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理下载失败的情况
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 非200系列的成功响应码，可能是服务器不支持断点续传
                    throw new IOException("Unexpected code " + response);
                }

                // 获取服务器返回的实际Content-Length或Content-Range来判断是否支持断点续传
                String contentRange = response.header("Content-Range");
                long contentLength = getContentLength(response);

                try (RandomAccessFile out = new RandomAccessFile(tempFile, "rw")) {
                    out.seek(downloadedBytes); // 移动到已下载数据末尾

                    InputStream in = response.body().byteStream();
                    byte[] buffer = new byte[8192];
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloadedBytes += read;

                        // 更新下载进度
                        //updateDownloadProgress(downloadedBytes, contentLength);
                    }

                    // 下载完成，检查文件完整性并重命名
                    if (downloadedBytes == contentLength || isContentRangeComplete(contentRange, contentLength)) {
                        LogHelper.Debug("文件下载完成");
                        //renameTempFileToTarget(targetFilePath);
                    }
                }
            }
        });
    }

    // 根据Content-Range判断是否完整下载
    private boolean isContentRangeComplete(String contentRange, long contentLength) {
        // 解析Content-Range，验证已下载范围是否覆盖整个文件
        if (contentRange == null) return false;

        String[] parts = contentRange.split("/");
        // Content-Range: bytes 0-1023/2048 或者 bytes 500-*
        if (parts.length > 1 && !"bytes".equals(parts[0].trim())) return false;

        long startByte = 0, endByte = -1;
        try {
            startByte = Long.parseLong(parts[1].split("-")[0]);
            if (parts[1].contains("-")) {
                endByte = Long.parseLong(parts[1].split("-")[1]);
            }
        } catch (NumberFormatException e) {
            return false; // 解析错误
        }

        if (endByte >= 0) {
            // 如果Content-Range包含结束字节，则检查是否覆盖了整个文件
            return endByte + 1 == contentLength;
        } else {
            // 若服务器返回的是"bytes 500-"表示从第500个字节开始到文件结束
            // 在这种情况下，我们无法直接得知文件总大小，需要依赖其他方式（如之前已知的总大小）来判断
            return true; // 假设在接收到这个响应时已经确认是完整的
        }
    }

    // 获取实际内容长度
    private long getContentLength(Response response) {
        long cl = response.body().contentLength();
        if (cl != -1) return cl;

        String contentRange = response.header("Content-Range");
        if (contentRange != null) {
            String[] parts = contentRange.split("/");
            if (parts.length > 1) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    // 解析错误，返回默认值
                }
            }
        }

        // 如果所有尝试都失败，则只能返回默认值或抛出异常，具体取决于你的应用逻辑
        return 0;
    }

    // 将临时文件重命名为目标文件
    private void renameTempFileToTarget(String targetFilePath) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.move(tempFile.toPath(), Paths.get(targetFilePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

package com.example.multimediav2.HttpUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import Modules.Action;
import Modules.LogHelper;
import Modules.StringUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
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

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public String Get(String url, boolean usePeoxy) throws Exception {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //短连接
        builder.connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));

        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS);

        if (!StringUnit.isEmpty(proxyip) && port > 0 && usePeoxy) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyip, port));
            builder.proxy(proxy);
        }

        OkHttpClient client = builder.build();

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
            }
        }

        return res;
    }

    public String Get(String url) throws Exception {
        return Get(url, true);
    }

    public String Post(String url, String json, boolean usePeoxy) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //短连接
        builder.connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS);

        if (!StringUnit.isEmpty(proxyip) && port > 0 && usePeoxy) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyip, port));
            builder.proxy(proxy);
        }

        OkHttpClient client = builder.build();

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

    public void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded, boolean usePeoxy) throws Exception {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //短连接
        builder.connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
        builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);

        if (!StringUnit.isEmpty(proxyip) && port > 0 && usePeoxy) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyip, port));
            builder.proxy(proxy);
        }

        OkHttpClient client = builder.build();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
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
    }

    public void Upload(String url, HashMap<String, Object> paras, boolean usePeoxy) throws Exception {

        try {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            //短连接
            builder.connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
            builder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);

            if (!StringUnit.isEmpty(proxyip) && port > 0 && usePeoxy) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyip, port));
                builder.proxy(proxy);
            }

            OkHttpClient client = builder.build();

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

}

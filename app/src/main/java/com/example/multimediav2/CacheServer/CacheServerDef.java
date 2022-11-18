package com.example.multimediav2.CacheServer;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

public class CacheServerDef implements ICacheServer {

    HttpProxyCacheServer proxy;

    public CacheServerDef(Context context) {
        proxy = new HttpProxyCacheServer.Builder(context).maxCacheSize(512 * 1024 * 1024).build();
    }

    public String GetCachedPath(String needCachePath) {
        return proxy.getProxyUrl(needCachePath);
    }

}



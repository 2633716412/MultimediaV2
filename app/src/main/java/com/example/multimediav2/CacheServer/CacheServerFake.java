package com.example.multimediav2.CacheServer;

public class CacheServerFake implements ICacheServer {
    

    public CacheServerFake() {

    }

    public String GetCachedPath(String needCachePath) {
        return needCachePath;
    }

}
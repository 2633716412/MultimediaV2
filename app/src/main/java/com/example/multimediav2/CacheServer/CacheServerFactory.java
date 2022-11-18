package com.example.multimediav2.CacheServer;

import android.content.Context;


public class CacheServerFactory {

    public static ICacheServer Get(Context context) {

        return new CacheServerDef(context);

    }
}
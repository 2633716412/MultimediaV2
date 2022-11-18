package com.example.multimediav2.HttpUnit;

import java.io.IOException;
import java.util.HashMap;

import Modules.Action;

public interface IHttpUnit {

    public  String Get(String url, boolean usePeoxy) throws Exception;

    public  String Get(String url) throws Exception;

    public String Post(String url, String json, boolean usePeoxy) throws IOException;

    public  String Post(String url, String json) throws IOException;

    public  void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded) throws Exception;

    public  void DownLoad(String url, final String dir, final String fn, final Action<Long> OnDwonloading, final Action Downloaded, boolean usePeoxy) throws Exception;

    public  void Upload(String url, HashMap<String, Object> paras, boolean usePeoxy) throws Exception;
}

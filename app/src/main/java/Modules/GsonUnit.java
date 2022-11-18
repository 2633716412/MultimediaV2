package Modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUnit {

    public static <T> T fromJson(String json, Class<T> classOfT) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, classOfT);



    }

    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(obj);
    }

}

package Modules;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUnit {

    SharedPreferences sp;

    public SPUnit(Context context) {
        sp = context.getSharedPreferences("DeviceData", 0);
    }

    public <T> T Get(String name, T def, Class<T> classOfT) {
        String str = sp.getString(name, GsonUnit.toJson(def));
        return GsonUnit.fromJson(str, classOfT);
    }

    public <T> T Get(String name, Class<T> classOfT) {
        String str = sp.getString(name, "{}");
        return GsonUnit.fromJson(str, classOfT);
    }

    public <T> void Set(String name, T data) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name, GsonUnit.toJson(data));
        editor.commit();
    }

    public void SetInt(String name, int data) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(name, data);
        editor.commit();
    }
}


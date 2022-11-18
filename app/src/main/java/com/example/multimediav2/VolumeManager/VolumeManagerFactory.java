package com.example.multimediav2.VolumeManager;

public class VolumeManagerFactory {

    public static IVolumeManager Get() {
        return new VolumeManagerDef();
    }
}


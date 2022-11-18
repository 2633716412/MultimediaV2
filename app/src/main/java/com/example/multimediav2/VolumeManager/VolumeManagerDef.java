package com.example.multimediav2.VolumeManager;

import android.content.Context;
import android.media.AudioManager;

public class VolumeManagerDef implements IVolumeManager {

    @Override
    public void SetVolumn(Context context, int newValue) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (0.01f * newValue * maxVolume), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int) (0.01f * newValue * maxVolume), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, (int) (0.01f * newValue * maxVolume), 0);
    }
}
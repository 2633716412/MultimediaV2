package Modules;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public class TextSpeaker2 implements TextToSpeech.OnInitListener {

    TextToSpeech toSpeech;
    AudioAttributes audioAttributes;
    public static boolean speechOver = true;
    SPUnit spUnit = new SPUnit(Paras.appContext);
    DeviceData userData = spUnit.Get("DeviceData", DeviceData.class);
    public TextSpeaker2(Context context) {
        toSpeech = new TextToSpeech(context, TextSpeaker2.this);
        toSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            int i=0;
            @Override
            public void onStart(String s) {//开始播放
                LogHelper.Debug("onStart开始播放");
                speechOver = false;
            }
            @Override
            public void onDone(String s) {//完成之后
                LogHelper.Debug("onDone完成播放");
                speechOver = true;
            }
            @Override
            public void onError(String s) {//播放错误的处理
                speechOver = true;

                LogHelper.Error("语音播放错误"+s);
            }
        });
    }

    public void read(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Thread.sleep(3000);
                    float v = Paras.volume / 100f;
                    if (v < 0 || v > 1)
                        v = 1f;
                    HashMap params = new HashMap();
                    String strV = String.valueOf(v);
                    params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, strV);
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "param");

                    try {

                        if(speechOver) {
                            int res=toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                            LogHelper.Debug("res"+res);
                            if (res == TextToSpeech.ERROR) {
                                stopTTS();
                                toSpeech=new TextToSpeech(Paras.appContext,TextSpeaker2.this);
                                Thread.sleep(3000);
                                read(text);
                            }
                        } else {
                            Thread.sleep(2000);
                            read(text);
                        }

                    } catch (Exception e) {
                        Paras.msgManager.SendMsg("播报失败："+e);
                        LogHelper.Error("语音返回错误"+e);
                    }

                } catch (Exception e) {
                    LogHelper.Error("语音播报失败："+e);
                }

            }
        }).start();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = toSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Paras.msgManager.SendMsg("语音模块初始化失败！");
                LogHelper.Debug("语音模块初始化失败！");
            }
            if (toSpeech != null) {
                // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                toSpeech.setPitch(2.0f);
                setStreamType(userData.stream_type);
                LogHelper.Debug("语音模块初始化成功！");
            }
        }
    }
    /**
     * 销毁播报方法，直接调用
     */
    public void stopTTS() {
        toSpeech.stop();
        //toSpeech.shutdown();
        toSpeech = null;
    }

    public void setSpeed(Long speed)
    {
        float v = speed / 100f;
        toSpeech.setSpeechRate(v);
    }

    //叫号音频设置
    public void setStreamType(int stream_type) {
        if(stream_type==0) {//闹钟音频
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                toSpeech.setAudioAttributes(audioAttributes);
            }
        } else if(stream_type==1) {//通知音频
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                toSpeech.setAudioAttributes(audioAttributes);
            }
        } else if(stream_type==2) {//通话音频
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                toSpeech.setAudioAttributes(audioAttributes);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes=new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                toSpeech.setAudioAttributes(audioAttributes);
            }
        }

    }
}

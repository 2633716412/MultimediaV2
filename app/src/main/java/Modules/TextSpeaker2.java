package Modules;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public class TextSpeaker2 implements TextToSpeech.OnInitListener {

    TextToSpeech toSpeech;
    private boolean speechOver = true;
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
                //可循环播放
                        /*if (i<3){
                            toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                            i++;
                        }*/
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
                    Thread.sleep(3000);
                    float v = Paras.volume / 100f;
                    if (v < 0 || v > 1)
                        v = 1f;
                    HashMap params = new HashMap();
                    String strV = String.valueOf(v);
                    params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, strV);
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "param");

                    try {
                        int res=toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                        LogHelper.Debug("res"+res);
                        if (res == TextToSpeech.ERROR) {
                            stopTTS();
                            toSpeech=new TextToSpeech(Paras.appContext,TextSpeaker2.this);
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
                toSpeech.setPitch(1.0f);
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
}

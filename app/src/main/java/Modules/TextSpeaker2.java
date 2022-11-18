package Modules;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class TextSpeaker2 implements TextToSpeech.OnInitListener {

    TextToSpeech toSpeech;

    public TextSpeaker2(Context context) {
        toSpeech = new TextToSpeech(context, this);
    }

    public void read(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                float v = Paras.volume / 100f;
                if (v < 0 || v > 1)
                    v = 1f;

                HashMap params = new HashMap();
                String strV = String.valueOf(v);

                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, strV);
                toSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
            }
        }).start();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = toSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Paras.msgManager.SendMsg("语音模块初始化失败！");
            }
            if (toSpeech != null) {
                // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                toSpeech.setPitch(1.0f);
                LogHelper.Debug("语音模块初始化成功！");
            }
        }
    }
}

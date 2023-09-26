package Modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.multimediav2.MainActivity;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.Debug("BootCompleteReceiver.onReceive");
        if(intent!=null) {
            LogHelper.Debug("接收到："+intent.getAction());
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            LogHelper.Debug("接收到开机广播");
            Intent thisIntent = new Intent(context, MainActivity.class);
            thisIntent.setAction("android.intent.action.MAIN");
            thisIntent.addCategory("android.intent.category.LAUNCHER");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(thisIntent);
        }
    }
}

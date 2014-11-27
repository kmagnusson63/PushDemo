package com.example.ken.pushdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class PushDemoBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),PushDemoIntentService.class.getName());

        startWakefulService(context,(intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}

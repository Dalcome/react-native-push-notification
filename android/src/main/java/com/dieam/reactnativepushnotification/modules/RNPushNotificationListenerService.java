package com.dieam.reactnativepushnotification.modules;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.util.List;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

public class RNPushNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        JSONObject data = getPushData(bundle.getString("data"));
        if (data != null) {
            if (!bundle.containsKey("message")) {
                bundle.putString("message", data.optString("alert", "Notification received"));
            }
            if (!bundle.containsKey("title")) {
                bundle.putString("title", data.optString("title", null));
            }
        }

        sendNotification(bundle);
    }

    private JSONObject getPushData(String dataString) {
        try {
            return new JSONObject(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendNotification(Bundle bundle) {

        Boolean isRunning = isApplicationRunning();

        Intent intent = new Intent("RNPushNotificationReceiveNotification");
        bundle.putBoolean("foreground", isRunning);
        intent.putExtra("notification", bundle);

        if (!isRunning || isKeyguardInputMode()) {
            new RNPushNotificationHelper(getApplication(), this).sendNotification(bundle);
        }
        else {
            sendBroadcast(intent);
        }
    }

    private boolean isApplicationRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(getApplication().getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String d: processInfo.pkgList) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isKeyguardInputMode() {
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }
}
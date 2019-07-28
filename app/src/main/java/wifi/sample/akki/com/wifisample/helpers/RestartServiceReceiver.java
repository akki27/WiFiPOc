package wifi.sample.akki.com.wifisample.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import wifi.sample.akki.com.wifisample.services.ForegroundService;

public class RestartServiceReceiver extends BroadcastReceiver
{

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        context.startService(new Intent(context.getApplicationContext(), ForegroundService.class));

    }
}

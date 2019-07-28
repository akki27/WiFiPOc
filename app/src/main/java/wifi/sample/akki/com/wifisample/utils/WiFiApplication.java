package wifi.sample.akki.com.wifisample.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class WiFiApplication extends Application {
    private static final String LOG_TAG = "WiFiApplication";
    public static WiFiApplication instance = null;

    public static Context getInstance() {
        if (null == instance) {
            instance = new WiFiApplication();
        }
        return instance;
    }
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate() {
        Log.d(LOG_TAG, "onTerminate");
        super.onTerminate();
    }
}

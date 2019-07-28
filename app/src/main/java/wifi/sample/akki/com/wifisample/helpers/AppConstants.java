package wifi.sample.akki.com.wifisample.helpers;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.util.Arrays;
import java.util.List;

import wifi.sample.akki.com.wifisample.BuildConfig;
import wifi.sample.akki.com.wifisample.utils.WiFiApplication;

public class AppConstants {

    public static final String CONNECTIVITY_TYPE_WIFI = "Wifi";
    public static final String CONNECTIVITY_TYPE_MOBILE = "mobile";
    public static final String CONNECTIVITY_TYPE_UNKNOWN = "unknown";
    public static final String CONNECTIVITY_TYPE_NOT_CONNECTED = "not_connected";
    public static final String CURRENT_WIFI_SSID = "current_ssid";
    public static final String CURRENT_WIFI_BSSID = "current_bssid";
    public static final String WIFI_RSSI_STRENGTH_EXCELLENT = "Excellent";
    public static final String WIFI_RSSI_STRENGTH_GOOD = "Good";
    public static final String WIFI_RSSI_STRENGTH_FAIR = "Fair";
    public static final String WIFI_RSSI_STRENGTH_WEAK = "Weak";
    public static final String WIFI_RSSI_STRENGTH_NO_SIGNAL = "No Signal";

    public static final String TRANSPORT_CELLULAR = "Cellular";
    public static final String TRANSPORT_WIFI = "WiFi";
    public static final String TRANSPORT_BLUETOOTH = "Bluetooth";
    public static final String TRANSPORT_ETHERNET = "Ethernet";
    public static final String TRANSPORT_VPN = "VPN";
    public static final String TRANSPORT_WIFI_AWARE = "WiFi-Aware";
    public static final String TRANSPORT_LOWPAN = "LowPAN";

    public static final String NETWORK_AVAILABLE_ACTION = "wifi.sample.akki.com.wifisample.NetworkAvailable";
    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";

    public interface ACTION {
        public static String MAIN_ACTION = "wifi.sample.akki.com.wifisample.action.main";
        public static String INIT_ACTION = "wifi.sample.akki.com.wifisample.action.init";
        public static String PREV_ACTION = "wifi.sample.akki.com.wifisample.action.prev";
        public static String PLAY_ACTION = "wifi.sample.akki.com.wifisample.action.play";
        public static String NEXT_ACTION = "wifi.sample.akki.com.wifisample.action.next";
        public static String STARTFOREGROUND_ACTION = "wifi.sample.akki.com.wifisample.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "wifi.sample.akki.com.wifisample.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static final String SF_PROTECTED_APP_SETTINGS= "ProtectedApps";
    public static final String SF_KEY_APP_PROTECTION_CHECK = "skipProtectedAppCheck";
    public static final String BRAND_HUAWEI = "huawei";

    /***
     * Xiaomi (MIUI): Manual Procedure : Go to Settings-> Battery-> Manage app's battery usage-> Click on Off or Choose your app
     */
    public static final String PACKAGE_NAME_MI = "com.miui.securitycenter";
    public static final String CLASS_NAME_MI_AUTOSTART = "com.miui.permcenter.autostart.AutoStartManagementActivity";
    public static final String PACKAGE_NAME_MI_BATTERY = "com.miui.powerkeeper";
    public static final String CLASS_NAME_MI_BATTERY = "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity";

    /***
     * Honor(HUAWEI)
     */
    public static final String PACKAGE_NAME_HUAWEI = "com.huawei.systemmanager";
    public static final String CLASS_NAME_HUAWEI_OPTIMIZE = "com.huawei.systemmanager.optimize.process.ProtectActivity";
    public static final String CLASS_NAME_HUAWEI_APPCONTROL = "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity";

    /***
     * Oppo: Manual Procedure:
      1. Settings-> Battery -> Your App-> Disallow both options.
      2. Security-> Privacy Permission-> StartUp manager-> Allow Your Appcom.miui.permcenter.autostart
     .AutoStartManagementActivity
           ==> enable auto start in settings for app to keep service running.
      3. Lock app in recent app's tab, by dragging it downwards
     Oppo Settings: https://oppo-au.custhelp.com/app/answers/detail/a_id/139/~/manage-app-permissions-on-your-oppo-smartphone
    */
    public static final String OPPO_COLOROS_POWER_PACKAGE  = "com.coloros.oppoguardelf";
    public static final String OPPO_COLOROS_POWER_ACTIVITY   = "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity";
    public static final String PACKAGE_NAME_OPPO = "com.oppo.safe";
    public static final String CLASS_NAME_OPPO_STARTUP_ACTIVITY = "com.oppo.safe.permission.startup.StartupAppListActivity";
    public static final String OPPO_COLOROS_SAFECENTER_PACKAGE  = "com.coloros.safecenter";
    public static final String OPPO_COLOROS_STARTUP_ACTIVITY = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    public static final String OPPO_COLOROS_STARTUP_ACTIVITY_V2 = "com.coloros.safecenter.startupapp.StartupAppListActivity";
    //permissions manage page does not work [https://github.com/jokermonn/permissions4m/blob/master/permissions4m-api/src/main/java/com/joker/api/support/manufacturer/OPPO.java]
    public static final String OPPO_COLOROS_PERMISSION_MANAGER_ACTIVITY = "com.coloros.safecenter.permission.singlepage.PermissionSinglePageActivity";

    /**
     * Vivo
     */
    public static final String PACKAGE_NAME_VIVO = "com.vivo.permissionmanager";
    public static final String CLASS_NAME_VIVO_STARTUP = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    public static final String PACKAGE_NAME_VIVO_SECURE = "com.iqoo.secure";
    public static final String CLASS_NAME_VIVO_SECURE_WHITELIST = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    public static final String CLASS_NAME_VIVO_SECURE_STARTUP = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";
    //public static final String CLASS_NAME_VIVO_SECURE_STARTUP = "com.iqoo.secure.MainGuideActivity.";

    /***
     * Asus
     * */
    public static final String PACKAGE_NAME_ASUS= "com.asus.mobilemanager";
    //public static final String CLASS_NAME_ASUS_STARTUP = "com.asus.mobilemanager.MainActivity";
    public static final String CLASS_NAME_ASUS_STARTUP = "com.asus.mobilemanager.entry.FunctionActivity";
    public static final String CLASS_NAME_ASUS_AUTOSTART = "com.asus.mobilemanager.autostart.AutoStartActivity";

    /***
     * Samsung
     * */
    public static final String PACKAGE_NAME_SAMSUNG = "com.samsung.android.lool";
    public static final String CLASS_NAME_SAMSUNG_STARTUP = "com.samsung.android.sm.ui.battery.BatteryActivity";

    /***
     * HTC
     * */
    public static final String PACKAGE_NAME_HTC = "com.htc.pitroad";
    public static final String CLASS_NAME_HTC_STARTUP = "com.htc.pitroad.landingpage.activity.LandingPageActivity";

    /***
     * QMobile
     * */
    public static final String PACKAGE_NAME_QMOBILE = "com.dewav.dwappmanager";
    public static final String CLASS_NAME_QMOBILE_STARTUP = "com.dewav.dwappmanager.memory.SmartClearupWhiteList";

    /***
     * Letv(LeEco)
     */
    public static final String PACKAGE_NAME_LeEco = "com.letv.android.letvsafe";
    public static final String CLASS_NAME_LeEco_STARTUP_ACTIVITY = "com.letv.android.letvsafe.AutobootManageActivity";

    /***
     * Meizu flymes: https://github.com/zhaozepeng/FloatWindowPermission/issues/29
     * */
    public static final String PACKAGE_NAME_MEIZU_FLYMES = "com.meizu.safe";
    public static final String MEIZU_APPLICATION_DETAIL_SETTINGS_ACTIVITY = "com.meizu.safe.security.SHOW_APPSEC";

    //updated the POWERMANAGER_INTENTS 28/02/2019
    public static final List<Intent> POWERMANAGER_INTENTS = Arrays.asList(

            new Intent().setComponent(new ComponentName(PACKAGE_NAME_MI, CLASS_NAME_MI_AUTOSTART)),
            //new Intent().setComponent(new ComponentName(PACKAGE_NAME_MI_BATTERY, CLASS_NAME_MI_BATTERY)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_HUAWEI, CLASS_NAME_HUAWEI_OPTIMIZE)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_HUAWEI, CLASS_NAME_HUAWEI_APPCONTROL)),
            new Intent().setComponent(new ComponentName(OPPO_COLOROS_POWER_PACKAGE, OPPO_COLOROS_POWER_ACTIVITY)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_OPPO, CLASS_NAME_OPPO_STARTUP_ACTIVITY)),
            new Intent().setComponent(new ComponentName(OPPO_COLOROS_SAFECENTER_PACKAGE, OPPO_COLOROS_STARTUP_ACTIVITY)),
            new Intent().setComponent(new ComponentName(OPPO_COLOROS_SAFECENTER_PACKAGE, OPPO_COLOROS_STARTUP_ACTIVITY_V2))
                    .setData(Uri.fromParts("package", WiFiApplication.getInstance().getPackageName(), null)),
            new Intent().setComponent(new ComponentName(OPPO_COLOROS_SAFECENTER_PACKAGE, OPPO_COLOROS_PERMISSION_MANAGER_ACTIVITY)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_VIVO, CLASS_NAME_VIVO_STARTUP)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_VIVO_SECURE, CLASS_NAME_VIVO_SECURE_WHITELIST)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_VIVO_SECURE, CLASS_NAME_VIVO_SECURE_STARTUP)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_ASUS, CLASS_NAME_ASUS_STARTUP)),
            //new Intent().setComponent(new ComponentName(PACKAGE_NAME_ASUS, CLASS_NAME_ASUS_STARTUP)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_ASUS, CLASS_NAME_ASUS_AUTOSTART)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_SAMSUNG, CLASS_NAME_SAMSUNG_STARTUP)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_HTC, CLASS_NAME_HTC_STARTUP)),
            new Intent().setComponent(new ComponentName( PACKAGE_NAME_QMOBILE, CLASS_NAME_QMOBILE_STARTUP)), //For Qmobile
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_LeEco, CLASS_NAME_LeEco_STARTUP_ACTIVITY)),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_LeEco, CLASS_NAME_LeEco_STARTUP_ACTIVITY))
                    .setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart")),
            new Intent().setComponent(new ComponentName(PACKAGE_NAME_MEIZU_FLYMES, MEIZU_APPLICATION_DETAIL_SETTINGS_ACTIVITY)).addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID)

    );

}

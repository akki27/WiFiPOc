package wifi.sample.akki.com.wifisample.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import wifi.sample.akki.com.wifisample.helpers.AppConstants;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class WiFiUtils {
    public static final String TAG = WiFiUtils.class.getSimpleName();
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public static boolean addPermission(Context context, List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            //return !shouldShowRequestPermissionRationale(permission);

            // Check for Rationale Option
            /*if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, permission))
                return false;*/
            return true;
        }
        return false;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<ScanResult> getUniqueSSID(List<ScanResult> inputList) {
        Set<ScanResult> set = new HashSet<ScanResult>(inputList);
        inputList.clear();
        inputList.addAll(set);
        return inputList;
    }

    public static boolean getGpsStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean CheckInternetConnection(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(CONNECTIVITY_SERVICE);
        return (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
                || (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnected());
    }

    private boolean netCheckin(Context context) {
        try {
            ConnectivityManager nInfo = (ConnectivityManager) context.getApplicationContext().getSystemService(
                    CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            Log.d(TAG, "Net avail:"
                    + nInfo.getActiveNetworkInfo().isConnectedOrConnecting());
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(
                    CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                Log.d(TAG, "Network available:true");
                return true;
            } else {
                Log.d(TAG, "Network available:false");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String ConnectionType(Context context) {
        String connectivityType = AppConstants.CONNECTIVITY_TYPE_UNKNOWN;
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    // connected to wifi
                    connectivityType = AppConstants.CONNECTIVITY_TYPE_WIFI;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    connectivityType = AppConstants.CONNECTIVITY_TYPE_MOBILE;
                    break;
                default:
                    break;
            }
        } else {
            // not connected to the internet
            connectivityType = AppConstants.CONNECTIVITY_TYPE_NOT_CONNECTED;
        }
        return connectivityType;
    }

    public static boolean isNullOrEmpty(String ptext) {
        return ptext == null || ptext.trim().length() == 0;
    }

    public static String getSignalStrength(int signalLevel) {
        String signalStrength = "Unknown";

        if (signalLevel <= 0 && signalLevel >= -50) {
            //Best signal
            signalStrength = AppConstants.WIFI_RSSI_STRENGTH_EXCELLENT;
        } else if (signalLevel < -50 && signalLevel >= -70) {
            //Good signal
            signalStrength = AppConstants.WIFI_RSSI_STRENGTH_GOOD;
        } else if (signalLevel < -70 && signalLevel >= -80) {
            //Low signal
            signalStrength = AppConstants.WIFI_RSSI_STRENGTH_FAIR;
        } else if (signalLevel < -80 && signalLevel >= -100) {
            //Very weak signal
            signalStrength = AppConstants.WIFI_RSSI_STRENGTH_WEAK;
        } else {
            // no signals
            signalStrength = AppConstants.WIFI_RSSI_STRENGTH_EXCELLENT;
        }

        return signalStrength;
    }

    /* NOTE: From android 8.0 onwards we wont be getting SSID of the connected network unless GPS is turned on. */
    public static int getRSSILevel(Context context, int rssiLevel) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return WifiManager.calculateSignalLevel(rssiLevel, 5);
    }

    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    public static String getCurrentBssid(Context context) {
        String bssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getBSSID())) {
                bssid = connectionInfo.getBSSID();
            }
        }
        return bssid;
    }

    /**
     * Get the value for the given key.
     * @return an empty string if the key isn't found
     */
    public static String getRmsi(Context context, String key) {
        String ret = "";

        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;

            Method get = SystemProperties.getMethod("get", paramTypes);

            //Parameters
            Object[] params = new Object[1];
            params[0] = new String(key);

            ret = (String) get.invoke(SystemProperties, params);
        } catch (Exception e) {
            ret = "Error While fetching RMSI";
            //TODO : Error handling
        }

        return ret;
    }

    public static String getImsiValue(Context context) {
        TelephonyManager telMngr = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching RMSI");
            return "";
        }
        return telMngr.getSubscriberId();
    }

    public static String getImeiValue(Context context) {
        TelephonyManager telMngr = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching IMEI");
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telMngr.getImei();
        } else {
            return telMngr.getDeviceId();
        }
    }

    /* Android doesn’t have any guaranteed solution to get user's mobile number programmatically.
     * If you want to verify user’s mobile number then ask to user to provide his number, using otp you can can verify that.
     * If you want to identify the user’s device, for this you can easily get device IMEI number.
     * */
    public static String getMobNum(Context context) {
        TelephonyManager telMngr = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching Mob Num");
            return "";
        }

        return telMngr.getLine1Number();
    }

    public static String getMobNumNew(Context context) {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching Mob Num");
            return "";
        }
        String mobNum = "";
        String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri", "mimetype"};
        Object object = context.getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract.Profile.CONTENT_URI, "data"),
                main_data, "mimetype=?",
                new String[]{"vnd.android.cursor.item/phone_v2"},
                "is_primary DESC");
        if (object != null) {
            do {
                if (!((Cursor) (object)).moveToNext())
                    break;
                // This is the phoneNumber
                mobNum = ((Cursor) (object)).getString(4);
            } while (true);
            ((Cursor) (object)).close();
        }

        return mobNum;
    }

    public static String getClientPhoneNumber(Context context) {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching Mob Num");
            return "";
        }

        StringBuilder mobNum = new StringBuilder();

        boolean isMultiSimEnabled = false;
        List<SubscriptionInfo> subInfoList = null;
        ArrayList<String> numbers;
        SubscriptionManager subscriptionManager;
        Integer PHONESTATS = 0x1;

        numbers = new ArrayList<String>();
        subscriptionManager = SubscriptionManager.from(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            }
            if (subInfoList.size() > 1) {
                isMultiSimEnabled = true;
            }
            for (SubscriptionInfo subscriptionInfo : subInfoList) {
                numbers.add(subscriptionInfo.getNumber());
            }

            for (String car : numbers) {
                if (mobNum.toString().length() > 0) {
                    mobNum.append("Sim2: ").append(numbers.get(1));
                } else {
                    mobNum.append(" Sim1: ").append(numbers.get(0));
                }
            }

            /*if(!numbers.get(0).isEmpty() && numbers.get(0).length() >0) {
                mobNum.append("Sim1: ").append(numbers.get(0));
            }
            if(!numbers.get(1).isEmpty() && numbers.get(1).length() >0) {
                mobNum.append("Sim2: ").append(numbers.get(1));
            }*/

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return mobNum.toString();
    }

    public static String getCarrier(Context context) {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching Mob Num");
            return "";
        }

        StringBuilder carrierName = new StringBuilder();

        boolean isMultiSimEnabled = false;
        List<SubscriptionInfo> subInfoList = null;
        ArrayList<String> carriers;
        SubscriptionManager subscriptionManager;
        Integer PHONESTATS = 0x1;

        carriers = new ArrayList<String>();
        subscriptionManager = SubscriptionManager.from(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            }
            if (subInfoList.size() > 1) {
                isMultiSimEnabled = true;
            }
            for (SubscriptionInfo subscriptionInfo : subInfoList) {
                carriers.add(subscriptionInfo.getCarrierName().toString());
            }

            for (String car : carriers) {
                if (carrierName.toString().length() > 0) {
                    carrierName.append(" Sim2: ").append(carriers.get(1));
                } else {
                    carrierName.append("Sim1: ").append(carriers.get(0));
                }
            }

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return carrierName.toString();
    }

    public static String getNetworkSubscriberHni(Context context) {
        StringBuilder subscriberHni = new StringBuilder();
        TelephonyManager tel = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();

        if (!TextUtils.isEmpty(networkOperator)) {
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            int mnc = Integer.parseInt(networkOperator.substring(3));
            subscriberHni.append(mcc).append(mnc);
        }
        return subscriberHni.toString();
    }

    /*
    1. In O, Android ID (Settings.Secure.ANDROID_ID or SSAID) has a different value for each app and each user on the device
    2. The ANDROID_ID value won't change on package uninstall/reinstall, as long as the package name and signing key are the same. Apps can rely on this value to maintain state across reinstalls.
    3. If an app was installed on a device running an earlier version of Android, the Android ID remains the same when the device is updated to Android O, unless the app is uninstalled and reinstalled.
    4. The Android ID value only changes if the device is factory reset or if the signing key rotates between uninstall and reinstall events.
    */
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public synchronized static String getUuid(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    public static int getInstalledAppCount(Context context) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        return pkgAppsList.size();
    }

    public static int getRunningAppCount(Context context) {
        ActivityManager actvityManager = (ActivityManager)
                context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
        return procInfos.size();
    }

    public static String getBatteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        float p = batteryPct * 100;
        return String.valueOf(Math.round(p));
    }

    public static String getBatteryChargingStatus(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
            return "Charging(AC)";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
            return "Charging(USB)";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS)
            return "Charging(Wireless)";
        return "Not Charging";
    }

    public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public static boolean isScreenON(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            Log.d(TAG, "AWAKE_STATUS: " + powerManager.isInteractive());
            return powerManager.isInteractive();
        } else {
            Log.d(TAG, "AWAKE_STATUS1: " + powerManager.isScreenOn());
            return powerManager.isScreenOn();
        }
    }

    /* NOTE: Getting the MAC address through WifiInfo.getMacAddress() won't work on Marshmallow and above,
    it has been disabled and will return the constant value of 02:00:00:00:00:00.*/
    public static String getMacAddress(Context context) {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Error: Permission Error while fetching Mac Address");
            return "";
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            return info.getMacAddress();
        } else {
            try {
                String interfaceName = "wlan0";
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                        continue;
                    }

                    byte[] mac = intf.getHardwareAddress();
                    if (mac == null) {
                        return "";
                    }

                    StringBuilder buf = new StringBuilder();
                    for (byte aMac : mac) {
                        buf.append(String.format("%02X:", aMac));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    return buf.toString();
                }
            } catch (Exception ex) {
                Log.d(TAG, "Exception occured while fetching Mac Address");
                return "";
            }
        }

        return "";
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }


    /**
     * Convert byte array to hex string
     * @param bytes toConvert
     * @return hexValue
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     * @param str which to be converted
     * @return array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     * @param filename which to be converted to string
     * @return String value of File
     * @throws java.io.IOException if error occurs
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
        }
    }

    /* Do this on a BG thread */
    public static void ping() {
        String result = null;
        int pingCount = 5;
        String[] rttPacket = new String[3];
        String[] packetLoss = new String[1];
        try {
            String url = "8.8.8.8";
            String pingCmd = "ping -c " + pingCount + " " + url;
            String pingResult = "";
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                pingResult += inputLine;
            }
            in.close();
            ProcessSyncResponse(pingResult, url);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ProcessSyncResponse(String pingResult, String url) {
        StringBuilder pingDataResult = new StringBuilder();
        String[] packetLoss = new String[0];
        String[] rttPacket = new String[3];

        try {
            int index2 = pingResult.indexOf("% packet loss");
            int index1 = pingResult.indexOf("rtt min/avg/max/mdev =");
            if (index1 > 0) {
                String parsedTime = (pingResult.substring(index1 + 23));
                rttPacket = parsedTime.split("/");
            } else {
                rttPacket[0] = "0";
                rttPacket[1] = "0";
                rttPacket[2] = "0";
            }
            if (index2 > 0) {
                String parsedPacket = "";
                int finalIndex = index2 + -3;
                String parsedintitalIndex = pingResult.substring(finalIndex,
                        index2);
                parsedintitalIndex = parsedintitalIndex.replaceAll(
                        "(,)|(,,)|( )", "");

                parsedPacket += parsedintitalIndex;
                parsedPacket += (pingResult.substring(index2));
                packetLoss = parsedPacket.split("%");

            } else {
                packetLoss[0] = "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (packetLoss.length > 0 && rttPacket.length > 0) {
            pingDataResult.append("\nPacket Loss in %: ").append(packetLoss[0])
                    .append("\nPacket Loss AVG: ").append(rttPacket[0])
                    .append("\nPacket Loss Min: ").append(rttPacket[1])
                    .append("\nPacket Loss Max: ").append(rttPacket[2]);
        }
        Log.d(TAG, "PingDataResult: " + pingDataResult.toString());
    }

    public static String getPacketLossAndDelay() {
        StringBuilder pingDataResult = new StringBuilder();
        String lost;
        String delay;
        Process p = null;
        String url = "8.8.8.8";
        try {
            p = Runtime.getRuntime().exec("ping -c 4 " + url);
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str;
            while ((str = buf.readLine()) != null) {
                if (str.contains("packet loss")) {
                    int i = str.indexOf("received");
                    int j = str.indexOf("%");
                    lost = str.substring(i + 10, j);
                    pingDataResult.append(lost);
                }
                if (str.contains("avg")) {
                    int i = str.indexOf("/", 20);
                    int j = str.indexOf(".", i);
                    System.out.println(":" + str.substring(i + 1, j));
                    delay = str.substring(i + 1, j);
                    pingDataResult.append(":").append(delay);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "PingDataResult: " + pingDataResult.toString());
        return pingDataResult.toString();
    }

    /* to check if you have Wifi connection or Cellular */
    private boolean isConnectedViaWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return mWifi.isConnected();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static long getWifiConsumedData(Context context) {
        long totalConsumedData = 0L;
        NetworkStats.Bucket bucket = null;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "",
                    getTimeFrom(), System.currentTimeMillis());

            if (bucket == null) {
                Log.d("Info", "Error");
            } else {
                Log.d("Info", "Data Received: " + (bucket.getRxBytes()) / (1024 * 1024)
                        + "\nDataTransmitted" + (bucket.getTxBytes()) / (1024 * 1024)
                        + "\nTotal Data: " + (bucket.getRxBytes() + bucket.getTxBytes()) / (1024 * 1024));
                totalConsumedData = (bucket.getRxBytes() + bucket.getTxBytes()) / (1024 * 1024);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return totalConsumedData;
    }

    /*private static int getConnectionType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if(capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))) {
                return NetworkCapabilities.TRANSPORT_WIFI;
            } else if(capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkCapabilities.TRANSPORT_CELLULAR;
            }
        } else {

        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static long getWifiReceived(Context context) {
        long totalReceivedData = 0L;
        NetworkStats.Bucket bucket = null;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "",
                    getTimeFrom(), System.currentTimeMillis());

            if (bucket == null) {
                Log.d("Info", "Error");
            } else {
                totalReceivedData = (bucket.getRxBytes()) / (1024 * 1024);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return totalReceivedData;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static long getWifiDataTransmitted(Context context) {
        long totalTransmitted = 0L;
        NetworkStats.Bucket bucket = null;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "",
                    getTimeFrom(), System.currentTimeMillis());

            if (bucket == null) {
                Log.d("Info", "Error");
            } else {
                totalTransmitted = (bucket.getTxBytes()) / (1024 * 1024);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return totalTransmitted;
    }

    private static long getTimeFrom() {
        long fromTime = 0L;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Date startOfToday = Date.from(ZonedDateTime.now().with(LocalTime.MIDNIGHT).toInstant());
            Log.d(TAG, "AKKi1: " + startOfToday.toString());
            fromTime = startOfToday.getTime();
        } else {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            fromTime = c.getTimeInMillis();
            Log.d(TAG, "AKKi2: " + c.toString());
        }

        Log.d(TAG, "AKKi3: " + fromTime);
        return fromTime;
    }

    public static boolean isUsagesStatePermissionAllowed(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getApplicationContext().getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        return false;
    }

    /////////////////////////////

    /* to switch off mobile network,
    including its corresponding subscription service via the SubscriptionManager class introduced in API 22 */
    public static void setMobileNetworkfromLollipop(Context context) throws Exception {
        String command = null;
        int state = 0;
        try {
            // Get the current state of the mobile network.
            state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }

    /*To check if the mobile network is enabled or not */
    private static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

    /* To get the value of the TRANSACTION_setDataEnabled field */
    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    /* To execute command via su */
    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }

    public static String getCurNetworkInUse(Context context) {
        String curNetworkType = "";
        ConnectivityManager connMgr = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        /*if(connMgr.getActiveNetworkInfo().isConnected()
                && connMgr.getActiveNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            curNetworkType = connMgr.getActiveNetworkInfo().getTypeName();
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connMgr.getAllNetworks();
            for (Network network : networks) {
                NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(network);
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    curNetworkType = AppConstants.TRANSPORT_WIFI;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    curNetworkType = AppConstants.TRANSPORT_CELLULAR;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
                    curNetworkType = AppConstants.TRANSPORT_BLUETOOTH;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    curNetworkType = AppConstants.TRANSPORT_ETHERNET;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    curNetworkType = AppConstants.TRANSPORT_VPN;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE))
                    curNetworkType = AppConstants.TRANSPORT_WIFI_AWARE;
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN))
                    curNetworkType = AppConstants.TRANSPORT_LOWPAN;
            }
        } else {
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.isConnectedOrConnecting ()) {
                curNetworkType = "WiFi";
            } else if (mobile.isConnectedOrConnecting ()) {
                curNetworkType = "Cellular";
            } else {
                curNetworkType = "No Network";
            }
        }

        return curNetworkType;
    }

    public static int getConnectionSpeed(Context context) {
        int linkSpeed = 0;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            linkSpeed = wifiInfo.getLinkSpeed(); //measured using WifiInfo.LINK_SPEED_UNITS
        }

        return linkSpeed;
    }

    public static int getDownloadBandwidth(Context context) {
        int downloadBandwidth = 0;
        ConnectivityManager connMgr = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connMgr.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(network);
            downloadBandwidth = capabilities.getLinkDownstreamBandwidthKbps()/1000;
        }

        return downloadBandwidth;
    }

    public static int getUploadBandwidth(Context context) {
        int uploadBandwidth = 0;
        ConnectivityManager connMgr = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connMgr.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(network);
            uploadBandwidth = capabilities.getLinkUpstreamBandwidthKbps()/1000;
        }

        return uploadBandwidth;
    }

    /* Turn Mobile data ON/OFF: In Android L 5.xx the hidden API setMobileDataEnabled method is removed and it can no longer be used.
     * Now the setMobileDataEnabled method no longer exists in ConnectivityManager and
     * this functionality was moved to TelephonyManager with two methods getDataEnabled and setDataEnabled.
     * Required: MODIFY_PHONE_STATE, But this permission is only for sstem apps which either
     * Pre-installed into a system folder on the ROM, OR
     * Compiled by a manufacturer using their security certificate*/
    public static void setMobileDataEnabled(Context context, boolean enabled) {
        final ConnectivityManager conman =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(
                    iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setMobileDataState(Context context, boolean mobileDataEnabled)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) context.getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error setting mobile data state", ex);
        }
    }

    public boolean getMobileDataState(Context context)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) context.getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error getting mobile data state", ex);
        }

        return false;
    }

    public static List<String> getRunningApps(Context context) {
        List<String> runningAppsList = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

        for (int i = 0; i < runningAppProcessInfo.size(); i++) {
            runningAppsList.add(runningAppProcessInfo.get(i).processName);
        }

        return runningAppsList;
    }

    public static boolean isAppInforground(Context context, String myPackage){
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(100);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }

    public static List<String> getRunningTask(Context context) {
        List<String> runningTaskList = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
            runningTaskList.add(pid.processName);
        }

        return runningTaskList;
    }

    public static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static String getUserSerial(Context context) {
        //noinspection ResourceType
        Object userManager = context.getApplicationContext().getSystemService("user");
        if (null == userManager) return "";

        try {
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            Long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            if (userSerial != null) {
                return String.valueOf(userSerial);
            } else {
                return "";
            }
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ignored) {
        }
        return "";
    }

    public static boolean isCurAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static boolean isAppIsInBackground(Context context, String inputPackageName) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(inputPackageName)) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(inputPackageName)) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}

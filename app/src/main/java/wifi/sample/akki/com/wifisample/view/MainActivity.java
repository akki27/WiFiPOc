    package wifi.sample.akki.com.wifisample.view;

    import android.Manifest;
    import android.annotation.TargetApi;
    import android.app.Activity;
    import android.app.AppOpsManager;
    import android.arch.lifecycle.Observer;
    import android.content.BroadcastReceiver;
    import android.content.ComponentName;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.content.pm.ResolveInfo;
    import android.content.res.Resources;
    import android.net.ConnectivityManager;
    import android.net.Uri;
    import android.net.wifi.SupplicantState;
    import android.net.wifi.WifiInfo;
    import android.net.wifi.WifiManager;
    import android.os.AsyncTask;
    import android.os.BatteryManager;
    import android.os.Build;
    import android.os.Handler;
    import android.provider.Settings;
    import android.support.annotation.NonNull;
    import android.support.annotation.Nullable;
    import android.support.annotation.RequiresApi;
    import android.support.v7.app.AlertDialog;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.AppCompatCheckBox;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;
    import android.widget.CompoundButton;
    import android.widget.ProgressBar;
    import android.widget.Switch;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.judemanutd.autostarter.AutoStartPermissionHelper;

    import java.io.IOException;
    import java.lang.reflect.Field;
    import java.lang.reflect.InvocationTargetException;
    import java.lang.reflect.Method;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.TimeUnit;

    import androidx.work.ExistingPeriodicWorkPolicy;
    import androidx.work.OneTimeWorkRequest;
    import androidx.work.PeriodicWorkRequest;
    import androidx.work.WorkInfo;
    import androidx.work.WorkManager;
    import wifi.sample.akki.com.wifisample.R;
    import wifi.sample.akki.com.wifisample.services.ForegroundService;
    import wifi.sample.akki.com.wifisample.services.MyWorker;
    import wifi.sample.akki.com.wifisample.utils.BatteryOptimizationUtil;
    import wifi.sample.akki.com.wifisample.utils.WiFiUtils;
    import wifi.sample.akki.com.wifisample.helpers.AppConstants;

    import static java.security.AccessController.getContext;

    public class MainActivity extends AppCompatActivity {
        private static final String TAG = MainActivity.class.getSimpleName();

        private Switch wifiSwitchBtn;
        private WifiManager wifiManager;
        private String currentWifiSsid = null;
        private String currentWifiBssid = null;
        private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;
        private static final int REQUEST_CODE_ASK_PHONE_STATE_PERMISSIONS = 1;
        private int batteryValue;
        private int batteryChargingStatus;
        private BroadcastReceiver batChargingReceiver, batInfoReceiver, wifiStateReceiver, supplicantStateReceiver, rssiChangeReceiver;
        private TextView wifiLinkSpeed, wifiRssiLevel, supplicantState;
        private ProgressBar pingProgress;
        private Button pingBtn;
        private MenuItem foregroundServiceStatus, workManager;
        private Intent serviceIntent;
        private boolean settingsUpdate = false;
        private boolean appAutoStartSettings = false;

        public class BatteryLevelReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_BATTERY_LOW, -1);
                Log.d(TAG, "LowBattery: " +status);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            initViews();
        }

        @Override
        protected void onResume() {
            super.onResume();
            updateWifiStatus();
            //updateNetworkInfo();

            //
            if(settingsUpdate) {
                settingsUpdate = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getApplicationContext())) {
                        startPowerSaverIntent();
                    }
                    else {
                        //TODO: Show toast that setting management was cancelled
                        Toast.makeText(MainActivity.this, "Setting management not enabled!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startPowerSaverIntent();
                }
            }
            if(appAutoStartSettings) {
                appAutoStartSettings = false;
                updateForegroundServiceStatus();
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            this.registerReceiver(this.batChargingReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            this.registerReceiver(this.batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            this.registerReceiver(this.wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            this.registerReceiver(this.supplicantStateReceiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
            this.registerReceiver(this.rssiChangeReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        }

        @Override
        protected void onStop() {
            Log.d(TAG, "onStopCalled");
            super.onStop();
            unregisterReceiver(batChargingReceiver);
            unregisterReceiver(batInfoReceiver);
            unregisterReceiver(wifiStateReceiver);
            unregisterReceiver(supplicantStateReceiver);
            unregisterReceiver(rssiChangeReceiver);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
            foregroundServiceStatus = menu.findItem(R.id.action_foreground_service);
            workManager = menu.findItem(R.id.action_work_manager);
            serviceIntent = new Intent(MainActivity.this, ForegroundService.class);
            updateForeGroundServiceMenuStatus();
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_network_stats:
                    Intent networkStatsIntent = new Intent(MainActivity.this, PackageListActivity.class);
                    startActivity(networkStatsIntent);
                    break;
                case R.id.action_runningapps:
                    //TODO: Check why it showing only the current forground app but not all the running apps?
                    Intent runningAppsIntent = new Intent(MainActivity.this, RunningAppsActivity.class);
                    startActivity(runningAppsIntent);
                    break;
                case R.id.action_foreground_service:
                    AddAppToWhiteList();
                    break;
                case R.id.action_work_manager:
                    startWorkManager();
                    break;
                case R.id.action_rssi_monitor:
                    Intent rssiMonitorIntent = new Intent(MainActivity.this, RssiMonitorActivity.class);
                    startActivity(rssiMonitorIntent);
                    break;
                default:
                    //break;
                    return super.onOptionsItemSelected(item);
            }
            return true;
        }

        private void initViews() {
            wifiSwitchBtn = (Switch) findViewById(R.id.switch_wifi_status);
            wifiSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        //Toast.makeText(MainActivity.this, "Wifi ON", Toast.LENGTH_SHORT).show();
                        wifiManager.setWifiEnabled(true);
                        wifiSwitchBtn.setText("Wifi ON");
                        //wifiLinkSpeed.setText(String.valueOf(WiFiUtils.getConnectionSpeed(getApplicationContext())));
                    } else {
                        //Toast.makeText(MainActivity.this, "Wifi OFF", Toast.LENGTH_SHORT).show();
                        wifiManager.setWifiEnabled(false);
                        wifiSwitchBtn.setText("Wifi OFF");
                    }

                    //TODO: Handle this
                    //updateNetworkInfo();
                    //updateWifiStatus();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateNetworkInfo();
                            updateWifiStatus();
                        }
                    }, 6000);
                }
            });

            wifiStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    SupplicantState supState;
                    wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    supState = wifiInfo.getSupplicantState();
                    Log.d(TAG, "WifiState: " +wifiState +":\nSuplicantState: " +supState);
                    Toast.makeText(MainActivity.this, "Wait...Stats updating", Toast.LENGTH_LONG).show();

                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLING:
                            //Toast.makeText(MainActivity.this, "Disabling", Toast.LENGTH_SHORT).show();
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            //Toast.makeText(MainActivity.this, "Disabled", Toast.LENGTH_SHORT).show();
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            //Toast.makeText(MainActivity.this, "Enabling", Toast.LENGTH_SHORT).show();
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            //Toast.makeText(MainActivity.this, "Enabled", Toast.LENGTH_SHORT).show();
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            //Toast.makeText(MainActivity.this, "Unknown", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            TextView appRunningStatus = (TextView) findViewById(R.id.app_running_status);
            if(WiFiUtils.isAppInforground(getApplicationContext(), "com.carnegietechnologies.ncp.cm")) { //"com.jio.jioplay.tv"
                appRunningStatus.setText("Running");
            } else {
                appRunningStatus.setText("Not Running");
            }

            TextView appUiStatus = (TextView) findViewById(R.id.app_ui_status);
            if(WiFiUtils.isAppIsInBackground(getApplicationContext(), "com.carnegietechnologies.ncp.cm")) { //"com.jio.jioplay.tv"
                appUiStatus.setText("Foreground");
                Log.d(TAG, "CarnegieClientUIStatus: Running in Foreground");
            } else {
                appUiStatus.setText("Background");
                Log.d(TAG, "CarnegieClientUIStatus: Running in Background");
            }

            supplicantState = (TextView) findViewById(R.id.supplicant_state);
            supplicantStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SupplicantState supState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    Log.d(TAG, "SupState: " +supState);
                    supplicantState.setText(supState.toString());
                    if(supState.equals(SupplicantState.COMPLETED)) {
                        wifiLinkSpeed.setText(String.valueOf(WiFiUtils.getConnectionSpeed(getApplicationContext())));
                    }

                }
            };

            rssiChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int numberOfLevels=5;
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int level=WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
                    System.out.println("Bars =" +level);
                    wifiRssiLevel.setText(wifiInfo.getRssi()+ " (Level: "  +String.valueOf(level) +")");
                }
            };

            Button showWifiListBtn = (Button) findViewById(R.id.button_show_wifi_list);
            showWifiListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WiFiScannerActivity.class);
                    intent.putExtra(AppConstants.CURRENT_WIFI_SSID, currentWifiSsid);
                    intent.putExtra(AppConstants.CURRENT_WIFI_BSSID, currentWifiBssid);
                    startActivity(intent);
                }
            });

            pingProgress = (ProgressBar)findViewById(R.id.ping_progressbar);
            pingBtn = (Button) findViewById(R.id.button_ping);
            pingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PingAsyncTask pingAsyncTask = new PingAsyncTask();
                    pingAsyncTask.execute();

                }
            });

            //Register for battery charging level and status updates
            batInfoReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context ctxt, Intent intent) {
                    batteryValue = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    TextView batteryLevel = (TextView) findViewById(R.id.device_battery);
                    batteryLevel.setText(String.valueOf(batteryValue)+"%");

                }
            };

            batChargingReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Are we charging / charged?
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    // How are we charging?
                    int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    Log.d(TAG, "chargePlugVal: " +chargePlug);
                    String chargingStatus = "Not Charging";
                    if(chargePlug  == BatteryManager.BATTERY_PLUGGED_AC)
                        chargingStatus = "Charging(AC)";
                    if(chargePlug  == BatteryManager.BATTERY_PLUGGED_USB)
                        chargingStatus = "Charging(USB)";
                    if(chargePlug  == BatteryManager.BATTERY_PLUGGED_WIRELESS)
                        chargingStatus = "Charging(Wireless)";

                    TextView chargingState = (TextView) findViewById(R.id.device_charging_state);
                    chargingState.setText(chargingStatus);
                }
            };

            wifiLinkSpeed = (TextView) findViewById(R.id.wifi_link_speed);
            wifiRssiLevel = (TextView) findViewById(R.id.wifi_signal_level);

            updateWifiStatus();
            setDeviceInfo();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkAndAskPermissions();
            } else {
                updateNetworkInfo();
            }
        }

        private void updateWifiStatus() {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if(wifiManager.isWifiEnabled()) {
                wifiSwitchBtn.setChecked(true);
                wifiSwitchBtn.setText("Wifi ON");
                //wifiLinkSpeed.setText(String.valueOf(WiFiUtils.getConnectionSpeed(getApplicationContext())));
                if(connectionInfo != null && !WiFiUtils.isNullOrEmpty(connectionInfo.getSSID())) {
                    currentWifiSsid = connectionInfo.getSSID();
                    currentWifiBssid = connectionInfo.getBSSID();
                }
            } else {
                wifiSwitchBtn.setChecked(false);
                wifiSwitchBtn.setText("Wifi OFF");
            }

        }

        private void setDeviceInfo() {
            TextView deviceManufacturer = (TextView) findViewById(R.id.device_manufacturer);
            deviceManufacturer.setText(Build.MANUFACTURER);

            TextView deviceModel = (TextView) findViewById(R.id.device_model);
            deviceModel.setText(Build.MODEL);

            TextView deviceOs = (TextView) findViewById(R.id.device_os);
            deviceOs.setText(Build.VERSION.RELEASE + "(API: " +String.valueOf(Build.VERSION.SDK_INT) +")");

            TextView hwSerial = (TextView) findViewById(R.id. device_hw_serial);
            hwSerial.setText(Build.HARDWARE);

            /* ANDROID_ID is a 64-bit number (as a hex string) that is randomly generated on the device’s first boot
            and should remain constant for the lifetime of the device.
            ANDROID_ID is used to absolutely identify a particular device physically: Not 100% reliable but better than other solutions.
            * */
            TextView androidId = (TextView) findViewById(R.id.device_android_id);
            androidId.setText(WiFiUtils.getAndroidId(getApplicationContext()));

            TextView uuid = (TextView) findViewById(R.id.device_uuid);
            uuid.setText(WiFiUtils.getUuid(getApplicationContext()));

            TextView installedAppsCount = (TextView) findViewById(R.id.device_apps_installed);
            installedAppsCount.setText(String.valueOf(WiFiUtils.getInstalledAppCount(getApplicationContext())));

            TextView runningAppsCount = (TextView) findViewById(R.id.device_apps_running);
            runningAppsCount.setText(String.valueOf(WiFiUtils.getRunningAppCount(getApplicationContext())));

            /*TextView batteryLevel = (TextView) findViewById(R.id.device_battery);
            batteryLevel.setText(String.valueOf(WiFiUtils.getBatteryLevel(getApplicationContext())));*/

            /*TextView chargingState = (TextView) findViewById(R.id.device_charging_state);
            chargingState.setText(String.valueOf(WiFiUtils.getBatteryChargingStatus(getApplicationContext())));*/

            TextView awakeState = (TextView) findViewById(R.id.device_awake_state);
            awakeState.setText(String.valueOf(WiFiUtils.isScreenON(getApplicationContext())));


        }

        private void checkAndAskPermissions() {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();

            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.READ_PHONE_STATE))
                permissionsNeeded.add("Phone State");
            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.READ_SMS))
                permissionsNeeded.add("Read SMS");
            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.READ_CONTACTS))
                permissionsNeeded.add("Read Contact");
            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.ACCESS_WIFI_STATE))
                permissionsNeeded.add("WiFi State");
            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.ACCESS_NETWORK_STATE))
                permissionsNeeded.add("Access Network State");
            if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.CHANGE_NETWORK_STATE))
                permissionsNeeded.add("Access Network State");

            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                }
            } else {
                updateNetworkInfo();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                    Map<String, Integer> perms = new HashMap<String, Integer>();
                    perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_SMS, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.CHANGE_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);

                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                        updateNetworkInfo();
                    } else {
                        // Permission Denied
                        Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                case REQUEST_CODE_ASK_PHONE_STATE_PERMISSIONS:
                    Map<String, Integer> perms = new HashMap<String, Integer>();
                    perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        //setWifiDataUses();
                    } else {
                        // Permission Denied
                        Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                                .show();
                    }

            }
        }

        private void updateNetworkInfo() {
            TextView deviceMac = (TextView) findViewById(R.id.device_mac);
            //deviceMac.setText(WiFiUtils.getMacAddress(getApplicationContext()));
            deviceMac.setText(WiFiUtils.getMacAddr());
            //deviceMac.setText(WiFiUtils.getMACAddress("wlan0")); //"eth0"

            TextView deviceIpv4 = (TextView) findViewById(R.id.device_ipv4);
            deviceIpv4.setText(WiFiUtils.getIPAddress(true));

            TextView deviceIpv6 = (TextView) findViewById(R.id.device_ipv6);
            deviceIpv6.setText(WiFiUtils.getIPAddress(false));

            TextView imeiValue = (TextView) findViewById(R.id. device_imei);
            imeiValue.setText(WiFiUtils.getImeiValue(getApplicationContext()));

            TextView networkImsi = (TextView) findViewById(R.id.network_imsi);
            networkImsi.setText(WiFiUtils.getImsiValue(getApplicationContext()));

            TextView mobNum = (TextView) findViewById(R.id.network_mobnum);
            mobNum.setText(WiFiUtils.getClientPhoneNumber(getApplicationContext()));

            TextView carrierName = (TextView) findViewById(R.id.network_carrier);
            carrierName.setText(WiFiUtils.getCarrier(getApplicationContext()));

            /*TextView simSlots = (TextView) findViewById(R.id.network_simslots);
            simSlots.setText(TelephonyInfo.getInstance(this));*/

            Log.d(TAG, "AKHILESH: " +this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT));

            TextView simSlots = (TextView) findViewById(R.id.network_hni);
            simSlots.setText(WiFiUtils.getNetworkSubscriberHni(getApplicationContext()));

            TextView curNetworkInUse = (TextView) findViewById(R.id.network_type);
            curNetworkInUse.setText(WiFiUtils.getCurNetworkInUse(getApplicationContext()));

            TextView LinkDnBandwidth = (TextView) findViewById(R.id.bandwidth_download);
            LinkDnBandwidth.setText(String.valueOf(WiFiUtils.getDownloadBandwidth(getApplicationContext())));

            TextView LinkUpBandwidth = (TextView) findViewById(R.id.bandwidth_upload);
            LinkUpBandwidth.setText(String.valueOf(WiFiUtils.getUploadBandwidth(getApplicationContext())));

        }

        /* This would not work now */
        private void setMobileDataEnabled(boolean enabled) {
            final ConnectivityManager conman =
                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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

        private class PingAsyncTask extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pingProgress.setVisibility(View.VISIBLE);
                pingBtn.setClickable(false);
            }

            @Override
            protected String doInBackground(Void... voids) {
                return WiFiUtils.getPacketLossAndDelay();
            }

            @Override
            protected void onPostExecute(String pingData) {
                super.onPostExecute(pingData);
                pingProgress.setVisibility(View.INVISIBLE);
                pingBtn.setClickable(true);
                if(pingData.length() > 0) {
                    String[] pingDataWord = pingData.split(":");

                    TextView packetLossPercentage = (TextView) findViewById(R.id.packet_loss_val);
                    packetLossPercentage.setText(pingDataWord[0]);

                    TextView packetDelay = (TextView) findViewById(R.id.packet_delay_val);
                    packetDelay.setText(pingDataWord[1]);
                }
            }
        }

        public void updateForegroundServiceStatus() {
            if (!ForegroundService.IS_SERVICE_RUNNING) {
                serviceIntent.setAction(AppConstants.ACTION.STARTFOREGROUND_ACTION);
                ForegroundService.IS_SERVICE_RUNNING = true;
                foregroundServiceStatus.setTitle("Stop Service");
            } else {
                serviceIntent.setAction(AppConstants.ACTION.STOPFOREGROUND_ACTION);
                ForegroundService.IS_SERVICE_RUNNING = false;
                foregroundServiceStatus.setTitle("Start Service");
            }
            startService(serviceIntent);
        }

        private void updateForeGroundServiceMenuStatus() {
            if (ForegroundService.IS_SERVICE_RUNNING) {
                foregroundServiceStatus.setTitle("Stop Service");
            } else {
                foregroundServiceStatus.setTitle("Start Service");
            }
        }

        private void AddAppToWhiteList() {
            if(!ForegroundService.IS_SERVICE_RUNNING) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getApplicationContext())) {
                        startPowerSaverIntent();
                        //startPowerSaverIntentNew();
                    }
                    else {
                        settingsUpdate = true;
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + this.getApplicationContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else {
                    startPowerSaverIntent();
                }
            } else {
                updateForegroundServiceStatus();
            }

        }

        public void startPowerSaverIntent() {
            if (Build.MANUFACTURER.contains(AppConstants.BRAND_HUAWEI)) {
                showHuaweiAlert();
            } else {
                SharedPreferences settings = this.getSharedPreferences(AppConstants.SF_PROTECTED_APP_SETTINGS, Context.MODE_PRIVATE);
                boolean skipMessage = settings.getBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, false);
                if (!skipMessage) {
                    final SharedPreferences.Editor editor = settings.edit();
                    boolean foundCorrectIntent = false;
                    for (final Intent intent : AppConstants.POWERMANAGER_INTENTS) {
                        try{
                            Log.d(TAG, "PowerManagerIntent: " +intent);
                            if (WiFiUtils.isCallable(this, intent)) {
                                foundCorrectIntent = true;
                                final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(this);
                                dontShowAgain.setText("Do not show again");
                                dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        editor.putBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, isChecked);
                                        editor.apply();
                                    }
                                });

                                new AlertDialog.Builder(this)
                                        .setTitle(Build.MANUFACTURER + " Protected Apps")
                                        .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", this.getString(R.string.app_name)))
                                        .setView(dontShowAgain)
                                        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                appAutoStartSettings = true;
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                                break;
                            }

                            if (!foundCorrectIntent) {
                                editor.putBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, true);
                                editor.apply();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    updateForegroundServiceStatus();
                }
            }
        }

        /** There isn't a setting in the manifest, and Huawei has enabled Tinder because it's a popular app.
        * There isn't a way to know if apps are protected.
        **/
        private void showHuaweiAlert() {
            final SharedPreferences settings = getSharedPreferences(AppConstants.SF_PROTECTED_APP_SETTINGS, MODE_PRIVATE);
            boolean skipMessage = settings.getBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, false);
            if (!skipMessage) {
                final SharedPreferences.Editor editor = settings.edit();
                Intent intent = new Intent();
                intent.setClassName(AppConstants.PACKAGE_NAME_HUAWEI, AppConstants.CLASS_NAME_HUAWEI_OPTIMIZE);
                if (WiFiUtils.isCallable(this, intent)) {
                    final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(this);
                    dontShowAgain.setText("Do not show again");
                    dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            editor.putBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, isChecked);
                            editor.apply();
                        }
                    });

                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Huawei Protected Apps")
                            .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", getString(R.string.app_name)))
                            .setView(dontShowAgain)
                            .setPositiveButton("Protected Apps", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    huaweiProtectedApps();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                } else {
                    editor.putBoolean(AppConstants.SF_KEY_APP_PROTECTION_CHECK, true);
                    editor.apply();
                }
            }
        }

        private void huaweiProtectedApps() {
            try {
                String cmd = "am start -n com.huawei.systemmanager/.optimize.process.ProtectActivity";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    cmd += " --user " + WiFiUtils.getUserSerial(this);
                }
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ignored) {
            }
        }

        public void startPowerSaverIntentNew() {
            //AutoStartPermissionHelper.getInstance().getAutoStartPermission(this); //For SDK
            final AlertDialog dialog = BatteryOptimizationUtil.getBatteryOptimizationDialog(this,
                    new BatteryOptimizationUtil.OnBatteryOptimizationAccepted() {
                        @Override
                        public void onBatteryOptimizationAccepted() {
                            //TODO:
                            Toast.makeText(MainActivity.this, "Start Foreground service now!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new BatteryOptimizationUtil.OnBatteryOptimizationCanceled() {
                        @Override
                        public void onBatteryOptimizationCanceled() {
                            //TODO:
                            Toast.makeText(MainActivity.this, "Foreground service may be killed as battery optimization cancelled!", Toast.LENGTH_SHORT).show();
                        }
                    });
            if (dialog != null) dialog.show();
        }

        private void startWorkManager() {

            final OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .addTag("simple_work")
                    .build();

            final PeriodicWorkRequest periodicWorkRequest
                    = new PeriodicWorkRequest.Builder(MyWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.SECONDS)
                    .addTag("periodic_work")
                    .build();

            WorkManager.getInstance().enqueue(periodicWorkRequest);
            /*WorkManager.getInstance().enqueueUniquePeriodicWork("jobTag",
                    ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);*/

            WorkManager.getInstance().getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(@Nullable WorkInfo workInfo) {
                            Log.d(TAG, "This is workmanager: " +workInfo.getState().isFinished());
                        }
                    });
        }

    }

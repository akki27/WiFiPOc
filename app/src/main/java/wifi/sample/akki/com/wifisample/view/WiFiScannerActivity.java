package wifi.sample.akki.com.wifisample.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import wifi.sample.akki.com.wifisample.Model.WifiData;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wifi.sample.akki.com.wifisample.R;
import wifi.sample.akki.com.wifisample.utils.WiFiUtils;
import wifi.sample.akki.com.wifisample.adapter.WifiAdapter;
import wifi.sample.akki.com.wifisample.helpers.AppConstants;
import wifi.sample.akki.com.wifisample.helpers.RecyclerTouchListener;

public class WiFiScannerActivity extends AppCompatActivity{

    private static final String TAG = WiFiScannerActivity.class.getName();
    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private EditText wifiPass;
    private String currentWifiSsid = null;
    private String currentWifiBssid = null;
    private List<WifiData> wifiList = new ArrayList<WifiData>();
    private RecyclerView recyclerView;
    private WifiAdapter wifiAdapter;
    private ProgressBar scanProgress;
    private BroadcastReceiver supplicantStateReceiver, networkStateReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scanner);
        setActionBar("Available WiFi List");

        currentWifiSsid = getIntent().getStringExtra(AppConstants.CURRENT_WIFI_SSID);
        currentWifiBssid = getIntent().getStringExtra(AppConstants.CURRENT_WIFI_BSSID);

        buttonScan = (Button) findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        wifiScanInit();
    }

    private void wifiScanInit() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        wifiAdapter = new WifiAdapter(wifiList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(wifiAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                WifiData wifiInfo = wifiList.get(position);
                String ssid = wifiInfo.getSsid();
                connectToWifi(ssid);
                //Toast.makeText(WiFiScannerActivity.this,"Wifi SSID : "+ssid,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndAskPermissions();
        } else {
            scanWifi();
        }


        IntentFilter intentFilter = new IntentFilter(AppConstants.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(AppConstants.IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                Log.d(TAG, "NetworkStateChange: " +networkStatus);
            }
        }, intentFilter);

        supplicantStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        Log.d(TAG, "Supplicant_WiFiConnected: ");
                    } else {
                        Log.d(TAG, "Supplicant_WiFiNotConnectedYet");
                    }
                }

                /*SupplicantState supState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d(TAG, "SupplicantState: " +supState);
                if(supState.equals(SupplicantState.COMPLETED)) {
                    Log.d(TAG, "WiFiConnected_Supplicant");
                }*/
            }
        };

        networkStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION )) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(info.isConnected()) {
                        Log.d(TAG, "Network_WiFiConnected ");
                    } else {
                        Log.d(TAG, "Network_WiFiConnecting: " +info.getDetailedState().name());
                    }
                }
            }
        };

    }
    private void scanWifi() {
        scanProgress = (ProgressBar)findViewById(R.id.scan_progressbar);
        scanProgress.setVisibility(View.VISIBLE);
        //WiFiScannerActivity.ScanAsyncTask pingAsyncTask = new WiFiScannerActivity.ScanAsyncTask();
        //pingAsyncTask.execute();

        if(WiFiUtils.getGpsStatus(this)) {
            //arrayList.clear();
            registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
            //Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
        } else {
            //enableDeviceLocation();
            displayLocationSettingsRequest(this);
        }
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanProgress.setVisibility(View.GONE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        }
    };

    private void scanSuccess() {
        wifiList.clear();
        List<ScanResult> results = WiFiUtils.getUniqueSSID(wifiManager.getScanResults());
        results = wifiManager.getScanResults();
        unregisterReceiver(wifiScanReceiver);

        WifiData wifiData;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        for (ScanResult scanResult : results) {
            int signalLevel = scanResult.level;
            /*Log.d(TAG, "CHECK: " +scanResult.SSID + ":: " +WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) + "\n");
            if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "Connected",
                        "RSSI :" + WiFiUtils.getRSSILevel(getApplicationContext(), signalLevel));
            } else {
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "",
                        "RSSI :" + WiFiUtils.getRSSILevel(getApplicationContext(), signalLevel));
            }*/

            if(currentWifiSsid != null && currentWifiSsid.contains(scanResult.SSID)
                    && currentWifiBssid != null && currentWifiBssid.contains(scanResult.BSSID)) {
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "Connected",
                        "RSSI: " + WiFiUtils.getSignalStrength(signalLevel));
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "Connected",
                        "RSSI: " + WiFiUtils.getRSSILevel(getApplicationContext(), signalLevel) + "(" +signalLevel +")");
            } else {
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "",
                        "RSSI: " + WiFiUtils.getSignalStrength(signalLevel));
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "",
                        "RSSI: " + WiFiUtils.getRSSILevel(getApplicationContext(), signalLevel) + "(" +signalLevel +")");
            }
            wifiList.add(wifiData);
            wifiAdapter.notifyDataSetChanged();
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed. Consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        results = wifiManager.getScanResults();
        unregisterReceiver(wifiScanReceiver);

        for (ScanResult scanResult : results) {
            arrayList.add(scanResult.SSID + " - " + scanResult.BSSID);
            wifiAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    scanWifi();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private void enableDeviceLocation() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);

        if(WiFiUtils.getGpsStatus(this)) {
            scanWifi();
        } else {
            // Location service not enabled
            Toast.makeText(this, "Please enable location service", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void checkAndAskPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();

        if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Location");
        if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Location");
        if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.ACCESS_WIFI_STATE))
            permissionsNeeded.add("WiFi State Access");
        if (WiFiUtils.addPermission(this, permissionsList, Manifest.permission.CHANGE_WIFI_STATE))
            permissionsNeeded.add("WiFi State Change");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                }
            }
        } else {
            scanWifi();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        scanWifi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter supplicantIntentFilter = new IntentFilter();
        supplicantIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        this.registerReceiver(this.supplicantStateReceiver, supplicantIntentFilter);

        IntentFilter networkIntentFilter = new IntentFilter();
        networkIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(networkStateReceiver, networkIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(supplicantStateReceiver);
        unregisterReceiver(networkStateReceiver);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(WiFiScannerActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if(dialog.getWindow() != null)
            dialog.getWindow().setLayout((6 * width)/7, (2 *height)/7);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.text_ssid);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        wifiPass = (EditText) dialog.findViewById(R.id.textPassword);
        textSSID.setText("Enter PW for " +wifiSSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String checkPassword = wifiPass.getText().toString();
                //finallyConnect(checkPassword, wifiSSID);
                WiFiScannerActivity.WiFiConnectionAsyncTask wiConnectAsyncTask =
                        new WiFiScannerActivity.WiFiConnectionAsyncTask();
                wiConnectAsyncTask.execute(wifiSSID, checkPassword);


                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void finallyConnect(String networkPass, String networkSSID) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        // remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"\"" + networkSSID + "\"\"";
        conf.preSharedKey = "\"" + networkPass + "\"";
        wifiManager.addNetwork(conf);

        currentWifiSsid = WiFiUtils.getCurrentSsid(getApplicationContext());
        currentWifiBssid = WiFiUtils.getCurrentBssid(getApplicationContext());
        scanWifi();
    }

    private class WiFiConnectionAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            scanProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            String networkSSID = params[0];
            String networkPass = params[1];

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", networkSSID);
            wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

            // remember id
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"\"" + networkSSID + "\"\"";
            conf.preSharedKey = "\"" + networkPass + "\"";
            wifiManager.addNetwork(conf);

            currentWifiSsid = WiFiUtils.getCurrentSsid(getApplicationContext());
            currentWifiBssid = WiFiUtils.getCurrentBssid(getApplicationContext());
            scanWifi();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            scanProgress.setVisibility(View.INVISIBLE);
        }
    }

    public void setActionBar(String heading) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            actionBar.setTitle(heading);
            actionBar.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ScanAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            scanProgress.setVisibility(View.VISIBLE);
            buttonScan.setClickable(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return WiFiUtils.getPacketLossAndDelay();
        }

        @Override
        protected void onPostExecute(String pingData) {
            super.onPostExecute(pingData);
            scanProgress.setVisibility(View.INVISIBLE);
            buttonScan.setClickable(true);
            if(pingData.length() > 0) {
                String[] pingDataWord = pingData.split(":");

                TextView packetLossPercentage = (TextView) findViewById(R.id.packet_loss_val);
                packetLossPercentage.setText(pingDataWord[0]);

                TextView packetDelay = (TextView) findViewById(R.id.packet_delay_val);
                packetDelay.setText(pingDataWord[1]);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (wifiScanReceiver!=null) {
                this.unregisterReceiver(wifiScanReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}

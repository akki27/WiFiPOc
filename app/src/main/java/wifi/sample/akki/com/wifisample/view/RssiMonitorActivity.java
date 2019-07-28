package wifi.sample.akki.com.wifisample.view;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import wifi.sample.akki.com.wifisample.Model.WifiData;
import wifi.sample.akki.com.wifisample.R;
import wifi.sample.akki.com.wifisample.adapter.RssiAdapter;
import wifi.sample.akki.com.wifisample.adapter.WifiAdapter;
import wifi.sample.akki.com.wifisample.helpers.RecyclerTouchListener;
import wifi.sample.akki.com.wifisample.utils.WiFiUtils;

public class RssiMonitorActivity extends AppCompatActivity {

    private static final String TAG = RssiMonitorActivity.class.getName();
    private RecyclerView recyclerView;
    private RssiAdapter rssiAdapter;
    private List<WifiData> wifiList = new ArrayList<WifiData>();
    private WifiManager wifiManager;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private Spinner selectedAP;
    private TextView noBssidText;

    private int interval = 2000;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi_monitor);
        setActionBar("Monitor RSSI for the selected AP");

        wifiScanInit();
    }

    private void wifiScanInit() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        rssiAdapter = new RssiAdapter(wifiList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rssiAdapter);

        selectedAP = findViewById(R.id.spnr_ap);
        String[] items = new String[]{"HSC_MAC", "JioHomeNet", "JioPrivateNet", "HSC_LIVE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        selectedAP.setAdapter(adapter);

        //Selected AP
        selectedAP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG, "Selected_AP: " +adapterView.getItemAtPosition(position).toString());
                //doScanIfLocationGranted();
                startRepeatingTask();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        noBssidText = (TextView) findViewById(R.id.tv_no_bssid);

        handler = new Handler();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                doScanIfLocationGranted();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler.postDelayed(mStatusChecker, interval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }

    private void doScanIfLocationGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndAskPermissions();
        } else {
            scanWifi();
        }
    }

    private void scanWifi() {
        if(WiFiUtils.getGpsStatus(this)) {
            registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
        } else {
            displayLocationSettingsRequest(this);
        }
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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

        WifiData wifiData;
        Log.d(TAG, "WiFiListSize1: " +wifiList.size());
        for (ScanResult scanResult : results) {
            if(scanResult.SSID.contains(selectedAP.getSelectedItem().toString())) {
                noBssidText.setVisibility(View.INVISIBLE);
                int signalLevel = scanResult.level;
                wifiData = new WifiData(scanResult.SSID, scanResult.BSSID, "",
                        "RSSI: " + WiFiUtils.getRSSILevel(getApplicationContext(), signalLevel) + "(" + signalLevel + ")");
                wifiList.add(wifiData);
                rssiAdapter.notifyDataSetChanged();
                Log.d(TAG, "WiFiListSize2: " +wifiList.size());
            }
        }

        Log.d(TAG, "WiFiListSize3: " +wifiList.size());
        if(wifiList.size() == 0) {
            rssiAdapter.notifyDataSetChanged();
            noBssidText.setVisibility(View.VISIBLE);
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed. Consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        results = wifiManager.getScanResults();
        unregisterReceiver(wifiScanReceiver);

        for (ScanResult scanResult : results) {
            arrayList.add(scanResult.SSID + " - " + scanResult.BSSID);
            rssiAdapter.notifyDataSetChanged();
            noBssidText.setVisibility(View.VISIBLE);
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
                            status.startResolutionForResult(RssiMonitorActivity.this, REQUEST_CHECK_SETTINGS);
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
        }

        @Override
        protected String doInBackground(Void... voids) {
            return WiFiUtils.getPacketLossAndDelay();
        }

        @Override
        protected void onPostExecute(String pingData) {
            super.onPostExecute(pingData);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();

        try {
            if (wifiScanReceiver!=null) {
                this.unregisterReceiver(wifiScanReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}

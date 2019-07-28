package wifi.sample.akki.com.wifisample.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import wifi.sample.akki.com.wifisample.R;
import wifi.sample.akki.com.wifisample.helpers.NetworkStatsHelper;
import wifi.sample.akki.com.wifisample.helpers.PackageManagerHelper;
import wifi.sample.akki.com.wifisample.helpers.TrafficStatsHelper;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = StatsActivity.class.getSimpleName();
    private static final int READ_PHONE_STATE_REQUEST = 37;
    public static final String EXTRA_PACKAGE = "ExtraPackage";

    AppCompatImageView ivIcon;
    Toolbar toolbar;

    TextView trafficStatsAllRx;
    TextView trafficStatsAllTx;
    TextView trafficStatsPackageRx;
    TextView trafficStatsPackageTx;

    TextView networkStatsAllRx;
    TextView networkStatsAllTx;
    TextView networkStatsPackageRx;
    TextView networkStatsPackageTx;
    TextView timePickerStart, timeStartValue, timePickerEnd, timeEndValue;
    String selectedDate, selectedTime;
    long startTime, endTime;
    String curPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivIcon = (AppCompatImageView) findViewById(R.id.avatar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
        initTextViews();
        checkIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String packageName = extras.getString(EXTRA_PACKAGE);
        if (packageName == null) {
            return;
        }
        try {
            ivIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
            toolbar.setTitle(getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(
                            packageName, PackageManager.GET_META_DATA)));
            toolbar.setSubtitle(packageName + ":" + PackageManagerHelper.getPackageUid(this, packageName));
            setSupportActionBar(toolbar);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!PackageManagerHelper.isPackage(StatsActivity.this, packageName)) {
            return;
        }
        curPackageName = packageName;
        fillData(packageName);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private void initTextViews() {
        trafficStatsAllRx = (TextView) findViewById(R.id.traffic_stats_all_rx_value);
        trafficStatsAllTx = (TextView) findViewById(R.id.traffic_stats_all_tx_value);
        trafficStatsPackageRx = (TextView) findViewById(R.id.traffic_stats_package_rx_value);
        trafficStatsPackageTx = (TextView) findViewById(R.id.traffic_stats_package_tx_value);
        networkStatsAllRx = (TextView) findViewById(R.id.network_stats_all_rx_value);
        networkStatsAllTx = (TextView) findViewById(R.id.network_stats_all_tx_value);
        networkStatsPackageRx = (TextView) findViewById(R.id.network_stats_package_rx_value);
        networkStatsPackageTx = (TextView) findViewById(R.id.network_stats_package_tx_value);

        timePickerStart = (TextView) findViewById(R.id.tv_select_start_time);
        timeStartValue = (TextView)findViewById(R.id.time_startValue);
        timePickerEnd = (TextView) findViewById(R.id.tv_select_end_time);
        timeEndValue = (TextView) findViewById(R.id.time_endValue);

        timePickerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePickerDialog();
            }
        });

        timePickerEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickerDialog();
            }
        });
    }

    private void showStartDatePickerDialog() {
        // Create a new OnDateSetListener instance. This listener will be invoked when user click ok button in DatePickerDialog.
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(year);
                strBuf.append("/");
                strBuf.append(month+1);
                strBuf.append("/");
                strBuf.append(dayOfMonth);

                selectedDate = strBuf.toString();
                //timeStartValue.setText(strBuf.toString());
                showStartTimePickerDialog();
            }
        };

        // Get current year, month and day.
        Calendar now = Calendar.getInstance();
        int year = now.get(java.util.Calendar.YEAR);
        int month = now.get(java.util.Calendar.MONTH);
        int day = now.get(java.util.Calendar.DAY_OF_MONTH);

        // Create the new DatePickerDialog instance.
        DatePickerDialog datePickerDialog = new DatePickerDialog(StatsActivity.this, onDateSetListener, year, month, day);

        // Set dialog icon and title.
        datePickerDialog.setIcon(R.drawable.ic_access_time_black_24dp);
        datePickerDialog.setTitle("Please select date.");

        // Popup the dialog.
        datePickerDialog.show();
    }
    private void showStartTimePickerDialog() {
        // Create a new OnTimeSetListener instance. This listener will be invoked when user click ok button in TimePickerDialog.
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(hour);
                strBuf.append(":");
                strBuf.append(minute);
                strBuf.append(":");
                strBuf.append("00");

                selectedTime = strBuf.toString();
                timeStartValue.setText(selectedDate + " " + selectedTime);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = null;
                try {
                    date = sdf.parse(selectedDate + " " + selectedTime);
                    startTime = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Reset the date strings
                selectedDate = "";
                selectedTime = "";
            }
        };

        Calendar now = Calendar.getInstance();
        int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = now.get(java.util.Calendar.MINUTE);

        // Whether show time in 24 hour format or not.
        boolean is24Hour = true;

        TimePickerDialog timePickerDialog = new TimePickerDialog(StatsActivity.this, onTimeSetListener, hour, minute, is24Hour);

        timePickerDialog.setIcon(R.drawable.ic_access_time_black_24dp);
        timePickerDialog.setTitle("Please select time.");

        timePickerDialog.show();
    }

    private void showEndDatePickerDialog() {
        // Create a new OnDateSetListener instance. This listener will be invoked when user click ok button in DatePickerDialog.
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(year);
                strBuf.append("/");
                strBuf.append(month+1);
                strBuf.append("/");
                strBuf.append(dayOfMonth);

                selectedDate = strBuf.toString();
                showEndTimePickerDialog();
            }
        };

        // Get current year, month and day.
        Calendar now = Calendar.getInstance();
        int year = now.get(java.util.Calendar.YEAR);
        int month = now.get(java.util.Calendar.MONTH);
        int day = now.get(java.util.Calendar.DAY_OF_MONTH);

        // Create the new DatePickerDialog instance.
        DatePickerDialog datePickerDialog = new DatePickerDialog(StatsActivity.this, onDateSetListener, year, month, day);

        // Set dialog icon and title.
        datePickerDialog.setIcon(R.drawable.ic_access_time_black_24dp);
        datePickerDialog.setTitle("Please select date.");

        // Popup the dialog.
        datePickerDialog.show();
    }
    private void showEndTimePickerDialog() {
        // Create a new OnTimeSetListener instance. This listener will be invoked when user click ok button in TimePickerDialog.
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(hour);
                strBuf.append(":");
                strBuf.append(minute);
                strBuf.append(":");
                strBuf.append("00");

                selectedTime = strBuf.toString();
                timeEndValue.setText(selectedDate + " " + selectedTime);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = null;
                try {
                    date = sdf.parse(selectedDate + " " + selectedTime);
                    endTime = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Update the states as per the selected intervals
                if(startTime > 0L && endTime > 0L) {
                    Log.d(TAG, "StartInterVal: " +startTime + "::EndInterval: " +endTime);
                    fillData(curPackageName);
                }

                //Reset the date strings
                selectedDate = "";
                selectedTime = "";
            }
        };

        Calendar now = Calendar.getInstance();
        int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = now.get(java.util.Calendar.MINUTE);

        // Whether show time in 24 hour format or not.
        boolean is24Hour = true;

        TimePickerDialog timePickerDialog = new TimePickerDialog(StatsActivity.this, onTimeSetListener, hour, minute, is24Hour);

        timePickerDialog.setIcon(R.drawable.ic_access_time_black_24dp);
        timePickerDialog.setTitle("Please select time.");

        timePickerDialog.show();
    }

    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
        fillTrafficStatsAll();
        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
        if(startTime > 0L && endTime > 0L) {
            long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this, startTime, endTime)
                    + networkStatsHelper.getAllRxBytesWifi(startTime, endTime);
            networkStatsAllRx.setText(mobileWifiRx + " B");
            long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(this, startTime, endTime)
                    + networkStatsHelper.getAllTxBytesWifi(startTime, endTime);
            networkStatsAllTx.setText(mobileWifiTx + " B");
        } else {
            long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this, 0, System.currentTimeMillis())
                    + networkStatsHelper.getAllRxBytesWifi(0, System.currentTimeMillis());
            networkStatsAllRx.setText(mobileWifiRx + " B");
            long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(this, 0, System.currentTimeMillis())
                    + networkStatsHelper.getAllTxBytesWifi(0, System.currentTimeMillis());
            networkStatsAllTx.setText(mobileWifiTx + " B");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {
        if(startTime > 0L && endTime > 0L) {
            long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this, startTime, endTime)
                    + networkStatsHelper.getPackageRxBytesWifi(startTime, endTime);
            networkStatsPackageRx.setText(mobileWifiRx + " B");
            long mobileWifiTx = networkStatsHelper.getPackageTxBytesMobile(this, startTime, endTime)
                    + networkStatsHelper.getPackageTxBytesWifi(startTime, endTime);
            networkStatsPackageTx.setText(mobileWifiTx + " B");
        } else {
            long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this, 0, System.currentTimeMillis())
                    + networkStatsHelper.getPackageRxBytesWifi(0, System.currentTimeMillis());
            networkStatsPackageRx.setText(mobileWifiRx + " B");
            long mobileWifiTx = networkStatsHelper.getPackageTxBytesMobile(this, 0, System.currentTimeMillis())
                    + networkStatsHelper.getPackageTxBytesWifi(0, System.currentTimeMillis());
            networkStatsPackageTx.setText(mobileWifiTx + " B");
        }
    }

    private void fillTrafficStatsAll() {
        trafficStatsAllRx.setText(TrafficStatsHelper.getAllRxBytes() + " B");
        trafficStatsAllTx.setText(TrafficStatsHelper.getAllTxBytes() + " B");
    }

    private void fillTrafficStatsPackage(int uid) {
        trafficStatsPackageRx.setText(TrafficStatsHelper.getPackageRxBytes(uid) + " B");
        trafficStatsPackageTx.setText(TrafficStatsHelper.getPackageTxBytes(uid) + " B");
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(StatsActivity.this, StatsActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

}

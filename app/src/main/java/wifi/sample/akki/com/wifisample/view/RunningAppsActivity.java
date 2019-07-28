package wifi.sample.akki.com.wifisample.view;

import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import wifi.sample.akki.com.wifisample.Model.WifiData;
import wifi.sample.akki.com.wifisample.R;
import wifi.sample.akki.com.wifisample.adapter.RunningAppsAdapter;
import wifi.sample.akki.com.wifisample.adapter.WifiAdapter;
import wifi.sample.akki.com.wifisample.utils.WiFiUtils;

public class RunningAppsActivity extends AppCompatActivity {

    private List<String> runningAppsList = new ArrayList<String>();
    private RecyclerView recyclerView;
    private RunningAppsAdapter runningAppsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_apps);
        setActionBar("Running Apps");
        setRunningAppsList();
    }

    private void showRunningApps() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        runningAppsAdapter = new RunningAppsAdapter(runningAppsList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(runningAppsAdapter);
    }

    private void setRunningAppsList() {
        runningAppsList.clear();

        runningAppsList.addAll(WiFiUtils.getRunningTask(this.getApplicationContext()));
        showRunningApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRunningAppsList();
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
}

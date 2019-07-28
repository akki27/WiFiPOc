package wifi.sample.akki.com.wifisample.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import wifi.sample.akki.com.wifisample.R;

public class RunningAppsAdapter extends RecyclerView.Adapter<RunningAppsAdapter.MyViewHolder> {

    private List<String> runningAppList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView runningAppName;

        public MyViewHolder(View view) {
            super(view);
            runningAppName = (TextView) view.findViewById(R.id.app_name);
        }
    }


    public RunningAppsAdapter(List<String> runningAppList) {
        this.runningAppList = runningAppList;
    }

    @Override
    public RunningAppsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.runing_apps_row, parent, false);

        return new RunningAppsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.runningAppName.setText(runningAppList.get(position));
    }


    @Override
    public int getItemCount() {
        return runningAppList.size();
    }
}


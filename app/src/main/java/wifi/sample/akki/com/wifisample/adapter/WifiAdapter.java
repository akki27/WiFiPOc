package wifi.sample.akki.com.wifisample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import wifi.sample.akki.com.wifisample.Model.WifiData;
import wifi.sample.akki.com.wifisample.R;

public class WifiAdapter  extends RecyclerView.Adapter<WifiAdapter.MyViewHolder> {

    private List<WifiData> wifiList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView Ssid, Bssid, connectionState, Rssi;

        public MyViewHolder(View view) {
            super(view);
            Ssid = (TextView) view.findViewById(R.id.ssid);
            connectionState = (TextView) view.findViewById(R.id.connection_state);
            Bssid = (TextView) view.findViewById(R.id.bssid);
            Rssi = (TextView) view.findViewById(R.id.rssi_value);
        }
    }


    public WifiAdapter(List<WifiData> wifiList) {
        this.wifiList = wifiList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WifiData WifiInfo = wifiList.get(position);
        holder.Ssid.setText(WifiInfo.getSsid());
        holder.connectionState.setText(WifiInfo.getConnectionState());
        holder.Bssid.setText(WifiInfo.getBssid());
        holder.Rssi.setText(WifiInfo.getRssi());
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }
}

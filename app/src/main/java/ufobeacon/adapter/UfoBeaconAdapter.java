package ufobeacon.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.fplay.audioapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import ufobeacon.main.BeaconDetailActivity;
import ufobeacon.main.SharedUFODevice;
import ufobeacon.main.UFOBeaconActivity;
import ufobeaconsdk.main.EddystoneType;
import ufobeaconsdk.main.UFODevice;
import ufobeaconsdk.main.UFODeviceType;


/**
 * Created by KP Patel on 03-Feb-17.
 */

public class UfoBeaconAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private final ArrayList<UFODevice> mListValues = new ArrayList<UFODevice>();
    private Context context;
    private ListView list;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public UfoBeaconAdapter(Context context, ListView deviceList) {
        this.context = context;
        list = deviceList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(UFODevice device) {

        if (mListValues != null) {

            if (mListValues.contains(device)) {
                //updateDevice(device);

            } else {
                mListValues.add(device);
               // notifyDataSetChanged();
            }
        }
    }

    private void updateDevice(UFODevice device) {
        final int indexInBonded = mListValues.indexOf(device);
        UFODevice ufodevice = mListValues.get(indexInBonded);

//        ufodevice.setRssi(device.getRssi());
//        ufodevice.setDate(device.getDate());

        View view = null;
        view = list.getChildAt(indexInBonded - list.getFirstVisiblePosition());
        if (view != null) {
            TextView rssi = (TextView) view.findViewById(R.id.tvRssi);
            TextView updatedDate = (TextView) view.findViewById(R.id.tvlastUpdated);
            TextView tvRowdata = (TextView) view.findViewById(R.id.tvrawData);
            TextView txPower = (TextView) view.findViewById(R.id.tvTx);
            TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
            tvRowdata.setText(ufodevice.getScanRecord());
            rssi.setText(ufodevice.getRssi() + " dBm");
            txPower.setText(ufodevice.getRssiAt1meter() + " dBm");
            tvDistance.setText(ufodevice.getDistanceInString());

            updatedDate.setText(sdf.format(ufodevice.getDate()));
            if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {
                TextView tviBeacon = (TextView) view.findViewById(R.id.tvibeacon);
                TextView tvEddystoneUIDTitle = (TextView) view.findViewById(R.id.tvEddystoneId);
                TextView tvEddystoneURLTitle = (TextView) view.findViewById(R.id.tvEddystoneUrl);
                TextView tvEddystoneTLMTitle = (TextView) view.findViewById(R.id.tvEddystoneTLM);
                TextView tvNamespaceId = (TextView) view.findViewById(R.id.tvEddystone_UID_namespaceId);
                TextView tvInstanceId = (TextView) view.findViewById(R.id.tvEddystone_UID_instaceId);
                TextView tvURL = (TextView) view.findViewById(R.id.tvEddystone_URL_url);
                TextView tvBatteryVoltage = (TextView) view.findViewById(R.id.tvEddystone_TLM_BatteyVoltage);
                TextView tvTemperature = (TextView) view.findViewById(R.id.tvEddystone_TLM_Temperature);
                TextView tvBootTime = (TextView) view.findViewById(R.id.tvEddystone_TLM_boottime);
                TextView tvPduCount = (TextView) view.findViewById(R.id.tvEddystone_TLM_pducount);
                LinearLayout lnrEddystone = (LinearLayout) view.findViewById(R.id.lnr_ufoeddystone_main);
                LinearLayout lnrBeacon = (LinearLayout) view.findViewById(R.id.lnr_ufobeacon_main);
                LinearLayout lnrEddystoneUID = (LinearLayout) view.findViewById(R.id.linear_eddystone_uid);
                LinearLayout lnrEddystoneURL = (LinearLayout) view.findViewById(R.id.linear_eddystone_url);
                LinearLayout lnrEddystoneTLM = (LinearLayout) view.findViewById(R.id.linear_eddystone_tlm);

                lnrEddystone.setVisibility(View.VISIBLE);
                lnrBeacon.setVisibility(View.GONE);
                if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_URL_TLM) {
                    lnrEddystoneUID.setVisibility(View.VISIBLE);
                    tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                    tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                    tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL + TLM");
                    lnrEddystoneURL.setVisibility(View.VISIBLE);
                    tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                    tvURL.setText(ufodevice.getEddystoneURL() + "");
                    lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                }
//               /* else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
//                    tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL");
//                    lnrEddystoneUID.setVisibility(View.VISIBLE);
//                    tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
//                    tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
//                    tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
//                    lnrEddystoneURL.setVisibility(View.VISIBLE);
//                    tvEddystoneURLTitle.setVisibility(View.VISIBLE);
//                    tvURL.setText(ufodevice.getEddystoneURL() + "");
//                    lnrEddystoneTLM.setVisibility(View.GONE);
//                } */
                else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_TLM) {
                    tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + TLM");
                    lnrEddystoneUID.setVisibility(View.VISIBLE);
                    tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                    tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                    lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                    lnrEddystoneURL.setVisibility(View.GONE);
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL_TLM) {
                    tviBeacon.setText(context.getString(R.string.eddystoneurl) + " + TLM");
                    lnrEddystoneURL.setVisibility(View.VISIBLE);
                    tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                    tvURL.setText(ufodevice.getEddystoneURL() + "");
                    lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                    lnrEddystoneUID.setVisibility(View.GONE);
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID) {
                    tviBeacon.setText(context.getString(R.string.eddystoneuid));
                    tvEddystoneUIDTitle.setVisibility(View.GONE);
                    lnrEddystoneTLM.setVisibility(View.GONE);
                    lnrEddystoneURL.setVisibility(View.GONE);
                    lnrEddystoneUID.setVisibility(View.VISIBLE);
                    tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
                    tviBeacon.setText(context.getString(R.string.eddystoneurl));
                    tvEddystoneURLTitle.setVisibility(View.GONE);
                    lnrEddystoneTLM.setVisibility(View.GONE);
                    lnrEddystoneUID.setVisibility(View.GONE);
                    lnrEddystoneURL.setVisibility(View.VISIBLE);
                    tvURL.setText(ufodevice.getEddystoneURL() + "");
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_TLM) {
                    tviBeacon.setText(context.getString(R.string.eddystonetlm));
                    tvEddystoneTLMTitle.setVisibility(View.GONE);
                    lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    lnrEddystoneUID.setVisibility(View.GONE);
                    lnrEddystoneURL.setVisibility(View.GONE);
                    tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                }
            }
        }
    }

    public void updateSortList() {
        sortListByRSSI();
        notifyDataSetChanged();
    }

    public void clearDevices() {
        if (mListValues != null && mListValues.size() > 0) {
            mListValues.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mListValues.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.item_ufobeacon, null);
            holder = new ViewHolder();

            holder.tvUnknown = (TextView) view.findViewById(R.id.tvunknown);
            holder.tviBeacon = (TextView) view.findViewById(R.id.tvibeacon);
            holder.tvDistance = (TextView) view.findViewById(R.id.tvDistance);
            holder.tvMacid = (TextView) view.findViewById(R.id.tvmacId);
            holder.tvMajor = (TextView) view.findViewById(R.id.tvmajor);
            holder.tvMinor = (TextView) view.findViewById(R.id.tvminor);
            holder.tvTxpower = (TextView) view.findViewById(R.id.tvTx);
            holder.tvRssi = (TextView) view.findViewById(R.id.tvRssi);
            holder.tvUUID = (TextView) view.findViewById(R.id.tvUUID);
            holder.tvLastUpdate = (TextView) view.findViewById(R.id.tvlastUpdated);
            holder.tvRowdata = (TextView) view.findViewById(R.id.tvrawData);
            holder.tvEddystoneUIDTitle = (TextView) view.findViewById(R.id.tvEddystoneId);
            holder.tvEddystoneURLTitle = (TextView) view.findViewById(R.id.tvEddystoneUrl);
            holder.tvEddystoneTLMTitle = (TextView) view.findViewById(R.id.tvEddystoneTLM);
            holder.tvNamespaceId = (TextView) view.findViewById(R.id.tvEddystone_UID_namespaceId);
            holder.tvInstanceId = (TextView) view.findViewById(R.id.tvEddystone_UID_instaceId);
            holder.tvURL = (TextView) view.findViewById(R.id.tvEddystone_URL_url);
            holder.tvBatteryVoltage = (TextView) view.findViewById(R.id.tvEddystone_TLM_BatteyVoltage);
            holder.tvTemperature = (TextView) view.findViewById(R.id.tvEddystone_TLM_Temperature);
            holder.tvBootTime = (TextView) view.findViewById(R.id.tvEddystone_TLM_boottime);
            holder.tvPduCount = (TextView) view.findViewById(R.id.tvEddystone_TLM_pducount);
            holder.lnrEddystone = (LinearLayout) view.findViewById(R.id.lnr_ufoeddystone_main);
            holder.lnrBeacon = (LinearLayout) view.findViewById(R.id.lnr_ufobeacon_main);
            holder.lnrEddystoneUID = (LinearLayout) view.findViewById(R.id.linear_eddystone_uid);
            holder.lnrEddystoneURL = (LinearLayout) view.findViewById(R.id.linear_eddystone_url);
            holder.lnrEddystoneTLM = (LinearLayout) view.findViewById(R.id.linear_eddystone_tlm);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (mListValues != null && mListValues.size() > 0) {

            final UFODevice ufodevice = mListValues
                    .get(position);
            final String name = ufodevice.getBtdevice().getName();
            holder.tvUnknown.setText(name != null ? name : "N/A");
            holder.tvMacid.setText(ufodevice.getBtdevice().getAddress());
            holder.tvRssi.setText(ufodevice.getRssi() + " dBm");
            holder.tvTxpower.setText(ufodevice.getRssiAt1meter() + " dBM");
            holder.tvDistance.setText(ufodevice.getDistanceInString());

            if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {
                holder.lnrEddystone.setVisibility(View.GONE);
                holder.lnrBeacon.setVisibility(View.VISIBLE);
                holder.tviBeacon.setText("iBeacon");
                holder.tvMajor.setText(ufodevice.getMajor() + "");
                holder.tvMinor.setText(ufodevice.getMinor() + "");
                holder.tvUUID.setText(ufodevice.getProximityUUID() + "");
                holder.tvRowdata.setText(ufodevice.getScanRecord());
                holder.tvLastUpdate.setText(sdf.format(ufodevice.getDate()));
            } else if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {
                holder.lnrEddystone.setVisibility(View.VISIBLE);
                holder.lnrBeacon.setVisibility(View.GONE);
                if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_URL_TLM) {
                    holder.lnrEddystoneUID.setVisibility(View.VISIBLE);
                    holder.tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                    holder.tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    holder.tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL + TLM");
                    holder.lnrEddystoneURL.setVisibility(View.VISIBLE);
                    holder.tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                    holder.tvURL.setText(ufodevice.getEddystoneURL() + "");
                    holder.lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    holder.tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    holder.tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    holder.tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    holder.tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    holder.tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                }
               /* else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_R && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL");
                    holder.lnrEddystoneUID.setVisibility(View.VISIBLE);
                    holder.tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                    holder.tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    holder.tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                    holder.lnrEddystoneURL.setVisibility(View.VISIBLE);
                    holder.tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                    holder.tvURL.setText(ufodevice.getEddystoneURL() + "");
                    holder.lnrEddystoneTLM.setVisibility(View.GONE);
                }*/
                else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_TLM) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + TLM");
                    holder.lnrEddystoneUID.setVisibility(View.VISIBLE);
                    holder.tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                    holder.tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    holder.tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                    holder.lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    holder.tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    holder.tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    holder.tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    holder.tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    holder.tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                    holder.lnrEddystoneURL.setVisibility(View.GONE);
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL_TLM) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneurl) + " + TLM");
                    holder.lnrEddystoneURL.setVisibility(View.VISIBLE);
                    holder.tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                    holder.tvURL.setText(ufodevice.getEddystoneURL() + "");
                    holder.lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    holder.tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                    holder.tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    holder.tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    holder.tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    holder.tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                    holder.lnrEddystoneUID.setVisibility(View.GONE);
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneuid));
                    holder.tvEddystoneUIDTitle.setVisibility(View.GONE);
                    holder.lnrEddystoneTLM.setVisibility(View.GONE);
                    holder.lnrEddystoneURL.setVisibility(View.GONE);
                    holder.lnrEddystoneUID.setVisibility(View.VISIBLE);
                    holder.tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                    holder.tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystoneurl));
                    holder.tvEddystoneURLTitle.setVisibility(View.GONE);
                    holder.lnrEddystoneTLM.setVisibility(View.GONE);
                    holder.lnrEddystoneUID.setVisibility(View.GONE);
                    holder.lnrEddystoneURL.setVisibility(View.VISIBLE);
                    holder.tvURL.setText(ufodevice.getEddystoneURL() + "");
                } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_TLM) {
                    holder.tviBeacon.setText(context.getString(R.string.eddystonetlm));
                    holder.tvEddystoneTLMTitle.setVisibility(View.GONE);
                    holder.lnrEddystoneTLM.setVisibility(View.VISIBLE);
                    holder.lnrEddystoneUID.setVisibility(View.GONE);
                    holder.lnrEddystoneURL.setVisibility(View.GONE);
                    holder.tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                    holder.tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + context.getString(R.string.celsiusUnicode));
                    holder.tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                    holder.tvPduCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                }
            }
            holder.tvRowdata.setText(ufodevice.getScanRecord());
            holder.tvLastUpdate.setText(sdf.format(ufodevice.getDate()));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedUFODevice.INSTANCE.setUfodevice(mListValues.get(position));
                ((UFOBeaconActivity)context).restartScan();
                Intent intent = new Intent(context, BeaconDetailActivity.class);
                context.startActivity(intent);
            }
        });
        return view;
    }

    public void sortListByRSSI() {
        if (mListValues != null && mListValues.size() > 0)
            Collections.sort(mListValues);
    }

    class ViewHolder {
        TextView tvUnknown, tviBeacon, tvMacid, tvMajor, tvMinor, tvTxpower,
                tvRssi, tvUUID, tvLastUpdate, tvRowdata, tvDistance;
        TextView tvEddystoneUIDTitle, tvEddystoneURLTitle, tvEddystoneTLMTitle;
        TextView tvNamespaceId, tvInstanceId;
        TextView tvURL;
        TextView tvBatteryVoltage, tvTemperature, tvBootTime, tvPduCount;
        LinearLayout lnrEddystone, lnrBeacon;
        LinearLayout lnrEddystoneUID, lnrEddystoneURL, lnrEddystoneTLM;

    }
}

package com.zen.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foolchen.lib.tracker.Tracker;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.biz.velabt.BleCore;
import com.zen.ui.R;
import com.zen.ui.event.BleUiConnectEvent;
import com.zen.ui.event.BleUiDisconnectEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class BleDevicePopupWindow extends PopupWindow {

    private RecyclerView availableRecyclerView;
    private RecyclerView connectedRecyclerView;

    private AvailableAdapter availableAdapter;
    private ConnectedAdapter connectedAdapter;

    private final ImageView searchIcon;
    private final Animation animation;

    /** Connected devices (UI only, backend still single-device) */
    private final List<ConnectedItem> connectedList = new ArrayList<>();

    public interface onButtonClickListener {
        void onClick(Object mac);
    }

    private onButtonClickListener onClickListener;

    public void setOnClickListener(onButtonClickListener listener) {
        this.onClickListener = listener;
    }

    public BleDevicePopupWindow(Context context) {
        super(context);

        View root = LayoutInflater.from(context)
                .inflate(R.layout.ble_device_popup_layout, null);
        setContentView(root);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));

        // --------------------
        // Connected devices list
        // --------------------
        connectedRecyclerView = root.findViewById(R.id.rv_connected_devices);
        connectedRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        connectedAdapter = new ConnectedAdapter();
        connectedRecyclerView.setAdapter(connectedAdapter);

        // --------------------
        // Available devices list
        // --------------------
        ViewGroup container = root.findViewById(R.id.layout_devices);
        availableRecyclerView = new RecyclerView(context);
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        availableAdapter = new AvailableAdapter();
        availableRecyclerView.setAdapter(availableAdapter);

        if (container instanceof LinearLayout) {
            ((LinearLayout) container).setGravity(Gravity.TOP);
        }

        container.removeAllViews();
        container.addView(availableRecyclerView);

        // --------------------
        // Scanning animation
        // --------------------
        searchIcon = root.findViewById(R.id.iv_searching);
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(animLoopListener);
    }

    // ==============================
    // PUBLIC API
    // ==============================

    public void addDevice(String name, String mac) {
        if (TextUtils.isEmpty(mac)) return;
        if (TextUtils.isEmpty(name)) name = mac;
        availableAdapter.addOrUpdate(new DeviceItem(name, mac));
    }

    public void removeDevice(String mac) {
        availableAdapter.remove(mac);
    }

    public void resetScanResults() {
        availableAdapter.replaceAll(new ArrayList<>());
    }

    /** Call ONLY when BLE confirms connection */
    public void setLinkDevice(String name, String mac) {
        if (TextUtils.isEmpty(mac)) return;

        for (ConnectedItem c : connectedList) {
            if (c.mac.equals(mac)) return;
        }

        connectedList.add(new ConnectedItem(
                TextUtils.isEmpty(name) ? mac : name,
                mac
        ));

        connectedAdapter.notifyDataSetChanged();
        availableAdapter.notifyDataSetChanged();
    }

    /** Call ONLY when BLE confirms disconnection */
    public void clearLinkDevice(String name, String mac) {
        for (int i = 0; i < connectedList.size(); i++) {
            if (connectedList.get(i).mac.equals(mac)) {
                connectedList.remove(i);
                connectedAdapter.notifyItemRemoved(i);
                break;
            }
        }

        availableAdapter.notifyDataSetChanged();
    }

    // ==============================
    // POPUP SHOW / DISMISS
    // ==============================

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);

        searchIcon.clearAnimation();
        searchIcon.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        BleCore.getInstance().stopScan();
        searchIcon.clearAnimation();
    }

    // ==============================
    // SCAN LOOP
    // ==============================

    private final Animation.AnimationListener animLoopListener = new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!isShowing()) return;

            if (MyApi.getInstance().getBtApi() != null) {
                if (MyApi.getInstance().getBtApi().isConnected()) return;
                if (MyApi.getInstance().getBtApi().isConnecting()) return;
            }

            if (!BleCore.getInstance().isScanning()) {
                BleCore.getInstance().startScan();
            }

            searchIcon.startAnimation(animation);
        }
    };

    // ============================================================
    // CONNECTED DEVICES
    // ============================================================

    private static class ConnectedItem {
        final String name;
        final String mac;
        ConnectedItem(String n, String m) {
            name = n;
            mac = m;
        }
    }

    private class ConnectedAdapter extends RecyclerView.Adapter<ConnectedVH> {

        @Override
        public ConnectedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ble_connected_device_item, parent, false);
            return new ConnectedVH(v);
        }

        @Override
        public void onBindViewHolder(ConnectedVH h, int pos) {
            ConnectedItem item = connectedList.get(pos);
            h.name.setText(item.name);

            h.sw.setOnCheckedChangeListener(null);
            h.sw.setChecked(true);

            h.sw.setOnCheckedChangeListener((btn, checked) -> {
                if (!checked) {
                    EventBus.getDefault()
                            .post(new BleUiDisconnectEvent(item.name, item.mac));
                }
            });

            h.itemView.setOnClickListener(v -> {
                BleDevice device = new BleDevice(item.name, item.mac);
                EventBus.getDefault().post(new BleUiConnectEvent(device));
            });
        }

        @Override
        public int getItemCount() {
            return connectedList.size();
        }
    }

    private static class ConnectedVH extends RecyclerView.ViewHolder {
        TextView name;
        Switch sw;

        ConnectedVH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_associated_name);
            sw = v.findViewById(R.id.iv_associated_link);
        }
    }

    // ============================================================
    // AVAILABLE DEVICES
    // ============================================================

    private static class DeviceItem {
        final String name;
        final String mac;
        DeviceItem(String n, String m) { name = n; mac = m; }
    }

    private class AvailableAdapter extends RecyclerView.Adapter<DeviceVH> {

        private final List<DeviceItem> list = new ArrayList<>();

        @Override
        public DeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ble_device_item, parent, false);
            return new DeviceVH(v);
        }

        @Override
        public void onBindViewHolder(DeviceVH h, int pos) {
            DeviceItem item = list.get(pos);
            h.name.setText(item.name);

            boolean connected = isDeviceReallyConnected(item.mac);

            RecyclerView.LayoutParams lp =
                    (RecyclerView.LayoutParams) h.itemView.getLayoutParams();

            if (connected) {
                // Hide AND remove layout space
                h.itemView.setVisibility(View.GONE);
                lp.height = 0;
            } else {
                // Restore visibility AND height (IMPORTANT for recycling)
                h.itemView.setVisibility(View.VISIBLE);
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                h.itemView.setOnClickListener(v -> {
                    if (onClickListener != null) {
                        onClickListener.onClick(item.mac);
                    }
                });
            }

            h.itemView.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        void addOrUpdate(DeviceItem d) {
            // Always keep device in the list; just update its name if needed.
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).mac.equals(d.mac)) {
                    list.set(i, d);
                    notifyItemChanged(i);
                    return;
                }
            }
            list.add(0, d);
            notifyItemInserted(0);
        }

        void remove(String mac) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).mac.equals(mac)) {
                    list.remove(i);
                    notifyItemRemoved(i);
                    return;
                }
            }
        }

        void replaceAll(List<DeviceItem> n) {
            list.clear();
            list.addAll(n);
            notifyDataSetChanged();
        }
    }

    private static class DeviceVH extends RecyclerView.ViewHolder {
        TextView name;
        DeviceVH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
        }
    }

    private boolean isDeviceReallyConnected(String mac) {
        if (TextUtils.isEmpty(mac)) return false;

        // UI truth: if it's in connectedList, we treat it as connected
        for (ConnectedItem c : connectedList) {
            if (mac.equals(c.mac)) {
                return true;
            }
        }

        // fall back on BleCore as a backup:
        // BleCore.Session s = BleCore.getInstance().getSession(mac);
        // return s != null && s.connected;

        return false;
    }


    private boolean shouldShow(DeviceItem item) {
        return !isDeviceReallyConnected(item.mac);
    }
}
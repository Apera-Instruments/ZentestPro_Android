package com.zen.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class BleDevicePopupWindow extends PopupWindow {

    private static final String TAG = "BleDevicePopupWindow";

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;

    private final ImageView searchIcon;
    private final Animation animation;

    private final Switch linkedSwitch;
    private final View linkedView;
    private final TextView linkedNameView;
    private final View linkedDeviceView;

    private String linkedMac = null;
    private String linkedName = null;

    private onButtonClickListener onClickListener;

    public interface onButtonClickListener {
        void onClick(Object index);
    }

    public void setOnClickListener(onButtonClickListener listener) {
        this.onClickListener = listener;
    }

    public BleDevicePopupWindow(Context context) {
        super(context);

        View root = LayoutInflater.from(context).inflate(R.layout.ble_device_popup_layout, null);
        setContentView(root);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));

        // Linked device section
        linkedDeviceView = root.findViewById(R.id.lv_associated_devices);
        linkedView = root.findViewById(R.id.layout_associated_devices);
        linkedNameView = root.findViewById(R.id.tv_associated_name);
        linkedView.setOnClickListener(v -> {
            if (onClickListener != null && linkedMac != null)
                onClickListener.onClick(linkedMac);
        });

        linkedDeviceView.setVisibility(View.INVISIBLE);

        linkedSwitch = root.findViewById(R.id.iv_associated_link);
        linkedSwitch.setOnCheckedChangeListener(this::onLinkedSwitchChange);

        // Replace the old layout container with a RecyclerView at runtime.
        ViewGroup container = root.findViewById(R.id.layout_devices);

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DeviceAdapter();
        recyclerView.setAdapter(adapter);

        // 🔧 FIX 1: ensure the list expands normally
        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        recyclerView.setLayoutParams(params);

// 🔧 FIX 2: prevent content from being vertically centered
        if (container instanceof LinearLayout) {
            ((LinearLayout) container).setGravity(Gravity.TOP);
        }

        // Replace old ViewGroup children with RecyclerView
        container.removeAllViews();
        container.addView(recyclerView);

        // scanning animation
        searchIcon = root.findViewById(R.id.iv_searching);
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(animLoopListener);
    }

    // ------------------------------
    // SWITCH LOGIC
    // ------------------------------
    private void onLinkedSwitchChange(CompoundButton buttonView, boolean isChecked) {
        if (MyApi.getInstance().getBtApi() == null) return;

        if (isChecked) {
            int ret = MyApi.getInstance().getBtApi().reconnect();
            if (ret == 0) {
                if (onClickListener != null && MyApi.getInstance().getBtApi().getLastDevice() == null)
                    onClickListener.onClick(linkedMac);

                buttonView.post(() -> {
                    boolean connected = MyApi.getInstance().getBtApi().isConnected();
                    if (connected != linkedSwitch.isChecked())
                        linkedSwitch.setChecked(connected);
                });
            }
        } else {
            MyApi.getInstance().getBtApi().disconnect();
            Tracker.INSTANCE.trackEvent("ble_disconnect", null);

            SharedPreferences sp = MyApi.getInstance().getDataApi().getSetting();
            sp.edit().putString("connectedDev", "").apply();
        }
    }

    // ------------------------------
    // POPUP SHOW
    // ------------------------------
    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);

        searchIcon.clearAnimation();
        searchIcon.startAnimation(animation);

        if (MyApi.getInstance().getBtApi() == null) return;

        linkedSwitch.setChecked(MyApi.getInstance().getBtApi().isConnected());
        BleDevice d = MyApi.getInstance().getBtApi().getLastDevice();

        if (d != null && MyApi.getInstance().getBtApi().isConnected()) {
            setLinkDevice(d.getName(), d.getMac());
        } else {
            linkedDeviceView.setVisibility(View.INVISIBLE);
        }
    }

    // ------------------------------
    // LINKED DEVICE UI
    // ------------------------------
    public void clearLinkDevice() {
        linkedMac = null;
        linkedName = null;
        linkedNameView.setText("");
        linkedView.setTag(null);
        linkedDeviceView.setVisibility(View.INVISIBLE);

        // Refresh list so previously linked device becomes visible again
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setLinkDevice(String name, String mac) {
        linkedMac = mac;
        linkedName = name;
        linkedNameView.setText(name);
        linkedView.setTag(mac);
        linkedDeviceView.setVisibility(View.VISIBLE);

        // Refresh list so this device disappears from the available list
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // ------------------------------
    // DEVICE LIST CONTROL
    // ------------------------------

    public void addDevice(String name, String mac) {
        if (TextUtils.isEmpty(mac)) return;

        if (TextUtils.isEmpty(name)) name = mac;

        adapter.addOrUpdate(new DeviceItem(name, mac));
    }

    public void removeDevice(String mac) {
        adapter.remove(mac);
    }

    public void resetScanResults() {
        adapter.replaceAll(new ArrayList<>());
    }

    public void updateConnectedUI() {
        if (MyApi.getInstance().getBtApi() == null) return;

        boolean connected = MyApi.getInstance().getBtApi().isConnected();
        linkedSwitch.setChecked(connected);

        BleDevice dev = MyApi.getInstance().getBtApi().getLastDevice();

        if (connected && dev != null)
            setLinkDevice(dev.getName(), dev.getMac());
        else
            clearLinkDevice();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        BleCore.getInstance().stopScan();
        searchIcon.clearAnimation();
    }

    // ------------------------------
    // SCAN ANIMATION LOOP
    // ------------------------------
    private final Animation.AnimationListener animLoopListener = new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!isShowing()) return;

            if (MyApi.getInstance().getBtApi() != null) {
                boolean connected = MyApi.getInstance().getBtApi().isConnected();
                if (connected != linkedSwitch.isChecked())
                    linkedSwitch.setChecked(connected);
                if (connected) return;

                boolean connecting = MyApi.getInstance().getBtApi().isConnecting();
                if (connecting) return;
            }

            if (!BleCore.getInstance().isScanning()) {
                BleCore.getInstance().startScan();
            }

            searchIcon.startAnimation(animation);
        }
    };

    // ============================================================
    //                        RECYCLER ADAPTER
    // ============================================================
    private static class DeviceItem {
        final String name;
        final String mac;
        DeviceItem(String n, String m) { name = n; mac = m; }
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {
        private final List<DeviceItem> list = new ArrayList<>();

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ble_device_item, parent, false);

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
            if (lp != null) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                v.setLayoutParams(lp);
            }

            return new DeviceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            DeviceItem item = list.get(position);
            holder.nameView.setText(item.name);
            holder.itemView.setTag(item.mac);

            boolean isLinked = linkedMac != null && linkedMac.equals(item.mac);

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (lp == null) {
                lp = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }

            if (isLinked) {
                // Connected device → hide from available list
                holder.itemView.setVisibility(View.GONE);
                lp.height = 0;
                holder.itemView.setLayoutParams(lp);

                // (Optional) disable click
                holder.itemView.setOnClickListener(null);

            } else {
                // Normal available device
                holder.itemView.setVisibility(View.VISIBLE);
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(lp);

                holder.itemView.setOnClickListener(v -> {
                    if (onClickListener != null)
                        onClickListener.onClick(item.mac);
                });
            }
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        // --------- List operations ---------

        void addOrUpdate(DeviceItem item) {
            int idx = find(item.mac);
            if (idx >= 0) {
                list.set(idx, item);
                notifyItemChanged(idx);
            } else {
                list.add(0, item);
                notifyItemInserted(0);
            }
        }

        void remove(String mac) {
            int idx = find(mac);
            if (idx >= 0) {
                list.remove(idx);
                notifyItemRemoved(idx);
            }
        }

        void replaceAll(List<DeviceItem> newList) {
            list.clear();
            list.addAll(newList);
            notifyDataSetChanged();
        }

        private int find(String mac) {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).mac.equals(mac))
                    return i;
            return -1;
        }
    }

    private static class DeviceViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        DeviceViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.tv_name);
        }
    }
}
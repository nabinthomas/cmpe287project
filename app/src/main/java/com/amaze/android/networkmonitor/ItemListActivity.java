package com.amaze.android.networkmonitor;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements NetworkMonitorEventListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;


    /**
     * Instance of the networkMonitor that is tied to this activity.
     */
    private NetworkMonitor networkMonitor = null;

    /**
     * Cached reference to the textView which shows the Tx Speed
     */
    private TextView txSpeedText = null;

    /**
     * Cached reference to the textView which shows the Rx Speed
     */
    private TextView rxSpeedText = null;

    /**
     *
     * Instance of the AppCont which holds the details about the apps.
     */
    private AppContent appContent = null;

    /**
     *
     * Threshold above which Notifications are required.
     */
    private long rxThreshold = 50 * 1024;

    /**
     *
     * Threshold above which Notifications are required.
     */
    private long txThreshold = 50 * 1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        txSpeedText = (TextView) findViewById(R.id.id_current_tx_speed_value);
        rxSpeedText = (TextView) findViewById(R.id.id_current_rx_speed_value);

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        AppContent appContent =   AppContent.getInstance(this);
        setupRecyclerView((RecyclerView) recyclerView, appContent);

        // Request Required Permissions when the App is starting.
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume(){
        System.out.println("OnResume...");
        super.onResume();
        if (null == networkMonitor) {
            networkMonitor = NetworkMonitor.Instance(this.getApplicationContext());
            networkMonitor.addListener(this);

        }
    }

    @Override
    protected void onPause(){
        System.out.println("onPause...");
        super.onPause();
        if (null != networkMonitor) {
            // Keep monitoring on moving away from this screen too.
            // networkMonitor.removeListener(this);
            // networkMonitor = null;
        }
    }

    public void handleReportGlobalSpeed(long rxValue, NetworkMonitor.Unit rxUnit,
                                        long txValue, NetworkMonitor.Unit txUnit) {

        System.out.println("Rx Speed Received = " + rxValue + " " + NetworkMonitor.unitToString(rxUnit));
        System.out.println("Tx Speed Received = " + txValue + " " + NetworkMonitor.unitToString(txUnit));

        /// TODO: Move this update to UI Thread to be safe
        txSpeedText.setText(NetworkMonitor.getFormattedSpeed(txValue, txUnit));
        rxSpeedText.setText(NetworkMonitor.getFormattedSpeed(rxValue, rxUnit));


        if (rxValue > rxThreshold) {
            notificationDialog("System Download Speed", rxThreshold, NetworkMonitor.Unit.bytesPerSec);
        }

        if (txValue > txThreshold) {
            notificationDialog("System Upload Speed", txThreshold, NetworkMonitor.Unit.bytesPerSec);
        }
    }

    public void handleReportAppBytesTransferred(String packageName, long rxValue, long txValue, NetworkMonitor.Unit unit, int networkType) {
        // This screen is not tracking per App data. So ignore this callback
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, AppContent appContent) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, appContent.ITEMS, mTwoPane));
    }

    // @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationDialog(String app_name, long threshold, NetworkMonitor.Unit txUnit) {

        NotificationManager notifManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Network_monitor";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notifChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notifChannel.setDescription("Network Monitor Channel description");
            notifChannel.enableLights(true);
            notifChannel.setLightColor(Color.RED);
            notifChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notifChannel.enableVibration(true);
            notifManager.createNotificationChannel(notifChannel);
        }
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        notifBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("NetworkMonitor")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Network usage crossed threshold " + NetworkMonitor.getFormattedSpeed(threshold, txUnit))
                .setContentText("App: " + app_name)
                .setContentInfo("Information");
        notifManager.notify(1, notifBuilder.build());
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<AppContent.AppItem> mValues;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppContent.AppItem item = (AppContent.AppItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<AppContent.AppItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).appName);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }


}

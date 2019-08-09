package com.amaze.android.networkmonitor;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.amaze.android.networkmonitor.dummy.DummyContent;

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
     * Cached reference to the textview which shows the Tx Speed
     */
    private TextView txSpeedText = null;

    /**
     * Cached reference to the textview which shows the Rx Speed
     */
    private TextView rxSpeedText = null;

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
        AppContent appContent = new AppContent();
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
            networkMonitor.removeListener(this);
            // networkMonitor.cancel(true);
            // networkMonitor = null;
        }
    }

    public void handleReportGlobalSpeed(long rxValue, NetworkMonitor.Unit rxUnit,
                                        long txValue, NetworkMonitor.Unit txUnit) {

        System.out.println("Rx Speed Received = " + rxValue + " " + NetworkMonitor.unitToString(rxUnit));
        System.out.println("Tx Speed Received = " + txValue + " " + NetworkMonitor.unitToString(txUnit));

        /// TODO: Move this update to UI Thread to be safe
        txSpeedText.setText(String.valueOf(txValue) + " " + NetworkMonitor.unitToString(txUnit));
        rxSpeedText.setText(String.valueOf(rxValue) + " " + NetworkMonitor.unitToString(rxUnit));
    }

    public void handleReportAppBytesTransferred(String packageName, long rxValue, long txValue, NetworkMonitor.Unit unit, int networkType) {
        // This screen is not tracking per App data. So ignore this callback
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, AppContent appContent) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, appContent.ITEMS, mTwoPane));
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
            holder.mContentView.setText(mValues.get(position).content);

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

    public class AppContent {

        /**
         * An array of sample (App) items.
         */
        public final List<AppItem> ITEMS = new ArrayList<AppItem>();

        /**
         * A map of sample (App) items, by ID.
         */
        public final Map<String, AppItem> ITEM_MAP = new HashMap<String, AppItem>();

        private static final int COUNT = 10;

        public AppContent(){
            PackageManager pm = getApplicationContext().getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);

            // Add app data.
            int i = 1;
            for (PackageInfo packageInfo : packages) {
                addItem(createAppItem(i++, packageInfo.packageName, packageInfo.versionName));
                try {
                    System.out.println("Package  (" + i + ") = " + packageInfo.packageName + " Name = " +
                            pm.getApplicationLabel(pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA)).toString());
                }
                catch (Exception e){

                }
            }
        }

        private void addItem(AppItem item) {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }

        private AppItem createAppItem(int position, String packageName, String versionName) {
            return new AppItem(String.valueOf(position), packageName, versionName);
        }

        private String makeDetails(int position) {
            StringBuilder builder = new StringBuilder();
            builder.append("Sans Detailed Stat for App #: ").append(position);
            for (int i = 0; i < position; i++) {
                builder.append("\nSans More details information here.");
            }
            return builder.toString();
        }

        /**
         * A App item representing a piece of content.
         */
        public class AppItem {
            public final String id;
            public final String content;
            public final String details;

            public AppItem(String id, String content, String details) {
                this.id = id;
                this.content = content;
                this.details = details;
            }

            @Override
            public String toString() {
                return content;
            }
        }
    }
}

package com.amaze.android.networkmonitor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import android.view.MenuItem;

import android.content.Context;
import androidx.core.content.ContextCompat;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity { //implements NetworkMonitorEventListener {

    /**
     * Instance of the networkMonitor that is tied to this activity.
     */
    private NetworkMonitor networkMonitor = null;

    /**
     * Instance of the AppContent
     */

    private  AppContent appContent = null;
    static final int PERMISSION_TYPE_READ_PHONE_STATE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_TYPE_READ_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_TYPE_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("BINU onOptionsItemSelected " +item.toString());
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
            return true;
        }
        else {
            AppContent.AppItem mItem = appContent.ITEM_MAP.get(item.getItemId());
            System.out.println("BINU onOptionsItemSelected " + mItem.appPkg);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){

        Context context = this.getApplicationContext() ;
        System.out.println("OnResume Detail...");
        super.onResume();
        networkMonitor = NetworkMonitor.Instance(context);
        appContent = AppContent.getInstance(context);


        //System.out.println("BINU OnResume " + getArguments().getString(ARG_ITEM_ID));
        //mItem = appContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

        // TODO : Replace the name below with the package name for the app that is handled by this activity
        //
       // networkMonitor.addListener(this);

    }

    @Override
    protected void onPause(){
        System.out.println("onPause...");
        super.onPause();
        if (null != networkMonitor) {
            networkMonitor.setPackageToMonitor(null);
            //BINU networkMonitor.removeListener(this);
        }
    }
    /*

    public void handleReportGlobalSpeed(long rxValue, NetworkMonitor.Unit rxUnit,
                                        long txValue, NetworkMonitor.Unit txUnit) {
        // This screen does not need to monitor global speed. So ignore this

    }

    public void handleReportAppBytesTransferred(String packageName, long rxValue, long txValue, NetworkMonitor.Unit unit, int networkType) {
        // TODO: Show the data on this activity.
        System.out.print("App Package Name: " + packageName + " : ");
        switch(networkType) {
            case ConnectivityManager.TYPE_WIFI:
                System.out.println("Wifi usage (Rx,Tx) = (" + rxValue + NetworkMonitor.unitToString(unit) + "," + txValue + NetworkMonitor.unitToString(unit) + ")");
                break;
            case ConnectivityManager.TYPE_MOBILE:
                System.out.println("Mobile usage (Rx,Tx) = (" + rxValue + NetworkMonitor.unitToString(unit) + "," + txValue + NetworkMonitor.unitToString(unit) + ")");
                break;
        }
        System.out.println("");

        try {
            AppContent.AppItem tempItem = appContent.ITEM_MAP.get(packageName);
            tempItem.TrafficRx = rxValue;
            tempItem.TrafficTx = txValue;

            System.out.println("BINU   tempItem.TrafficRx  for package  "+ packageName+ " is "  +  tempItem.TrafficRx  );
        }
        catch (Exception e)
        {
            //Do some thing later

            System.out.println("BINU  Error No Package   " +  packageName);
        }



    }
    */

}

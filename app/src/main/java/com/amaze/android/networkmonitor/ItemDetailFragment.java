package com.amaze.android.networkmonitor;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements NetworkMonitorEventListener {

    /**
     * We will keep an instance of AppContent which holds the per aap details
     */

    private AppContent appContent = null;

    /**
     * Instance of the networkMonitor that is tied to this activity.
     */
    private NetworkMonitor networkMonitor = null;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private AppContent.AppItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments().containsKey(ARG_ITEM_ID)) {

            Activity activity = this.getActivity();
            networkMonitor = NetworkMonitor.Instance(activity);
            appContent = AppContent.getInstance(activity);

            mItem = appContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            networkMonitor.setPackageToMonitor(mItem.appPkg);
            System.out.println("BINU Setting monitor to  " + mItem.appPkg  );
            networkMonitor.addListener(this);

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                String fullName  =  mItem.appPkg;
                appBarLayout.setTitle(fullName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            String  details = mItem.appPkg + " \n " + mItem.TrafficTx + "  AN: " + mItem.appName ;

            ((TextView) rootView.findViewById(R.id.item_detail)).setText(details);
        }

        return rootView;
    }


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
            if (mItem != null) {

                mItem.TrafficRx = rxValue;
                mItem.TrafficTx = txValue;
            }

            System.out.println("BINU   tempItem.TrafficRx  for package  "+ packageName+ " is "  +  mItem.TrafficRx  );
        }
        catch (Exception e)
        {
            //Do some thing later

            System.out.println("BINU  Error No Package   " +  packageName + e );
        }



    }
}

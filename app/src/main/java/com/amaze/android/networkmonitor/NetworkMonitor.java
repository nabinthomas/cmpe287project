package com.amaze.android.networkmonitor;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

interface NetworkMonitorEventListener {

    public void handleReportGlobalSpeed(long rxValue, NetworkMonitor.Unit rxUnit,
                                        long txValue, NetworkMonitor.Unit txUnit);

    public void handleReportAppBytesTransferred(String packageName, long rxValue, long txValue, NetworkMonitor.Unit unit, int networkType);
}

public class NetworkMonitor extends AsyncTask<NetworkMonitorEventListener, Integer, Long> {

    public enum Unit {
        bytesPerSec,
        kiloBytesPerSec,
        megaBytesPerSec,
        teraBytesPerSec,
        bytes,
        kiloBytes,
        megaBytes,
        teraBytes
    };

    NetworkMonitorEventListener listener = null;

    Context appContext = null;

    String mPackageToMonitor = null;

    /**
     *  Sampling interval. Default is 5 sec.
     */
    long samplingIntervalMs = 5000;

    public static String unitToString(Unit unit) {
        String units = "Unknown";

        switch (unit) {
            case bytesPerSec:
                units = "B/s";
                break;
            case kiloBytesPerSec:
                units = "KB/s";
                break;
            case megaBytesPerSec:
                units = "MB/s";
                break;
            case teraBytesPerSec:
                units = "TB/s";
                break;
            default:
                units = "Unknown";
                break;
        }
        return units;
    }

    /**
     * Setup the listener to monitor network traffic from a specific App.
     * If this is not called, then the monitoring happens for all Apps.
     *
     * @return true if successful, false otherwise
     */
    public boolean setPackageToMonitor(String packageName) {


        mPackageToMonitor = packageName;

        return true;
    }

    public void setSamplingIntervalMs(long samplingIntervalMs) {
        this.samplingIntervalMs = samplingIntervalMs;
    }

    protected Long doInBackground(NetworkMonitorEventListener... listener) {

        this.listener = listener[0];

        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long lastRxBytes = 0, lastTxBytes = 0;
        long currentRxBytes = 0;
        long currentTxBytes = 0;
        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastTxBytes = TrafficStats.getTotalTxBytes();

        while (!isCancelled()) {
            try {
                Thread.sleep(samplingIntervalMs);
                endTime = System.currentTimeMillis();
                currentRxBytes = TrafficStats.getTotalRxBytes();
                currentTxBytes = TrafficStats.getTotalTxBytes();

                float currentRxSpeed = 1.0f * (currentRxBytes - lastRxBytes) / ((endTime - startTime)/1000);
                float currentTxSpeed = 1.0f * (currentTxBytes - lastTxBytes) / ((endTime - startTime)/1000);

                System.out.println("TrafficStats : RxTotal = " + currentRxBytes +
                        ", TxTotal = " + currentTxBytes + ", " +
                        " Last = (" + lastRxBytes + ", " + lastTxBytes + ")" +
                        " Diff = (" + (currentRxBytes - lastRxBytes) + ", " + (currentTxBytes - lastTxBytes) + ")" +
                        " Time Duration = " + (endTime - startTime));
                if (this.listener != null) {

                    this.listener.handleReportGlobalSpeed((long)currentRxSpeed, NetworkMonitor.Unit.bytesPerSec,
                            (long)currentTxSpeed, Unit.bytesPerSec);
                }
                if (isCancelled()) break;
            }
            catch (Exception e) {
                // Ignore
            }
            // For now get the total data that was used by this app.
            refreshPerAppDataUsageIfEnabled(0, endTime);

            lastRxBytes = currentRxBytes;
            lastTxBytes = currentTxBytes;
            startTime = endTime;
        }
        return Long.parseLong("0");
    }

    protected void refreshPerAppDataUsageIfEnabled(long startTimeInMillisec, long endTimeInMillisec) {
        System.out.println (" Time is from " + startTimeInMillisec + " to " + endTimeInMillisec);
        // If Not monitoring per App Data return
        if (mPackageToMonitor == null)
            return;

        PackageManager pkgMgr = appContext.getPackageManager();
        int uid = -1;
        try {
            PackageInfo packageInfo = pkgMgr.getPackageInfo(mPackageToMonitor, PackageManager.GET_META_DATA);
            uid = packageInfo.applicationInfo.uid;

        } catch (PackageManager.NameNotFoundException e) {
            System.out.println ("Name not found + " + mPackageToMonitor + " Exception e = " + e.toString());
            return;
        }
        NetworkStats networkStats = null;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) appContext.getSystemService(Context.NETWORK_STATS_SERVICE);

        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI,
                getSubscriberId(ConnectivityManager.TYPE_WIFI),
                startTimeInMillisec,
                endTimeInMillisec,
                uid);
        long rxBytesWifi = 0L;
        long txBytesWifi = 0L;
        NetworkStats.Bucket packageBucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(packageBucket);
            rxBytesWifi += packageBucket.getRxBytes();
            txBytesWifi += packageBucket.getTxBytes();
        }
        networkStats.close();

        // System.out.println("TrafficStats.getUidTxBytes(uid) " + TrafficStats.getUidTxBytes(uid));

        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_MOBILE,
                getSubscriberId(ConnectivityManager.TYPE_MOBILE),
                startTimeInMillisec,
                endTimeInMillisec,
                uid);
        long rxBytesMobile = 0L;
        long txBytesMobile = 0L;

        packageBucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(packageBucket);
            rxBytesMobile += packageBucket.getRxBytes();
            txBytesMobile += packageBucket.getTxBytes();
        }
        networkStats.close();

        this.listener.handleReportAppBytesTransferred(mPackageToMonitor, rxBytesMobile, txBytesMobile, Unit.bytes, ConnectivityManager.TYPE_MOBILE);
        this.listener.handleReportAppBytesTransferred(mPackageToMonitor, rxBytesWifi, txBytesWifi, Unit.bytes, ConnectivityManager.TYPE_WIFI);
        System.out.println("Package " + mPackageToMonitor +
                " : Wifi(rx, tx) = (" + rxBytesWifi + "," + txBytesWifi + ")" +
                " : Mobile (rx, tx) = ( " + rxBytesMobile + "," +  txBytesMobile + ")");

    }
    protected void onProgressUpdate(Integer... progress) {
        System.out.println("Progress = " + progress[0]);
    }

    protected void onPostExecute(Long result) {
        System.out.println("Result = " + result);
    }

    protected void init(Context appContext) {
        this.appContext = appContext;

    }

    private String getSubscriberId(int networkType) {
        try {
            if (ConnectivityManager.TYPE_MOBILE == networkType) {
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                return telephonyManager.getSubscriberId();
            }
        }
        catch (SecurityException e) {
            System.out.println("Not enough permission  : " + e.toString() );
        }

        return "";
    }

    public static String getFormattedSpeed(long value, Unit unit) {
        Unit newUnit = unit;
        // while (value >= 1000 &&  )
        return "";
    }
}

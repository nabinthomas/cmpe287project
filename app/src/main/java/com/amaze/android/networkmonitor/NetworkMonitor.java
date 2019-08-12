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

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.annotation.SuppressLint;
import android.graphics.Color;
//import android.support.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


interface NetworkMonitorEventListener {

    void handleReportGlobalSpeed(long rxValue, NetworkMonitor.Unit rxUnit,
                                        long txValue, NetworkMonitor.Unit txUnit);

    void handleReportAppBytesTransferred(String packageName, long rxValue, long txValue, NetworkMonitor.Unit unit, int networkType);
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
        teraBytes,
        UnknownUnit
    }

    Context appContext = null;

    String mPackageToMonitor = null;

    protected static NetworkMonitor thisInstance = null;

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
            case bytes:
                units = "B";
                break;
            case kiloBytes:
                units = "KB";
                break;
            case megaBytes:
                units = "MB";
                break;
            case teraBytes:
                units = "TB";
                break;
            default:
                units = "??";
                break;
        }
        return units;
    }

    // @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationDialog(String app_name, long threshold, NetworkMonitor.Unit txUnit) {

        NotificationManager notifManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
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
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID);
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

        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long lastRxBytes = 0, lastTxBytes = 0;
        long currentRxBytes = 0;
        long currentTxBytes = 0;
        long rxThreshold = 50 * 1024;
        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastTxBytes = TrafficStats.getTotalTxBytes();

        while (!isCancelled()) {
            try {
                Thread.sleep(samplingIntervalMs);
                endTime = System.currentTimeMillis();
                currentRxBytes = TrafficStats.getTotalRxBytes();
                currentTxBytes = TrafficStats.getTotalTxBytes();

                float currentRxSpeed = 1.0f * (currentRxBytes - lastRxBytes) / ((endTime - startTime) / 1000);
                float currentTxSpeed = 1.0f * (currentTxBytes - lastTxBytes) / ((endTime - startTime) / 1000);

                System.out.println("TrafficStats : RxTotal = " + currentRxBytes +
                        ", TxTotal = " + currentTxBytes + ", " +
                        " Last = (" + lastRxBytes + ", " + lastTxBytes + ")" +
                        " Diff = (" + (currentRxBytes - lastRxBytes) + ", " + (currentTxBytes - lastTxBytes) + ")" +
                        " Time Duration = " + (endTime - startTime));

                Iterator<NetworkMonitorEventListener> it = listenerSet.iterator();
                while (((Iterator) it).hasNext()) {

                    NetworkMonitorEventListener callbackListener = (NetworkMonitorEventListener) it.next();
                    callbackListener.handleReportGlobalSpeed((long) currentRxSpeed, NetworkMonitor.Unit.bytesPerSec,
                            (long) currentTxSpeed, Unit.bytesPerSec);
                }

                if (currentRxSpeed > rxThreshold) {
                    notificationDialog("youtube", rxThreshold, NetworkMonitor.Unit.bytesPerSec);
                }

                if (isCancelled()) break;
            } catch (Exception e) {
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

        Iterator<NetworkMonitorEventListener> it = listenerSet.iterator();
        while(((Iterator) it).hasNext()) {

            NetworkMonitorEventListener callbackListener = (NetworkMonitorEventListener) it.next();
            callbackListener.handleReportAppBytesTransferred(mPackageToMonitor, rxBytesMobile, txBytesMobile, Unit.bytes, ConnectivityManager.TYPE_MOBILE);
            callbackListener.handleReportAppBytesTransferred(mPackageToMonitor, rxBytesWifi, txBytesWifi, Unit.bytes, ConnectivityManager.TYPE_WIFI);
        }

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

    public static NetworkMonitor Instance(Context context) {
        if (thisInstance == null) {
            thisInstance = new NetworkMonitor();
            thisInstance.init(context);
            thisInstance.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
        return thisInstance;
    }

    final Set<NetworkMonitorEventListener> listenerSet;

    protected NetworkMonitor() {
        listenerSet = new HashSet<NetworkMonitorEventListener>();
    }

    public void addListener(NetworkMonitorEventListener listener) {
        listenerSet.add(listener);
    }

    public void removeListener(NetworkMonitorEventListener listener) {
        listenerSet.remove(listener);
    }

    public static Unit getHigherUnit(Unit unit) {
        Unit newUnit = unit;
        switch (unit) {
            case bytes:
                newUnit = Unit.kiloBytes;
                break;
            case kiloBytes:
                newUnit = Unit.megaBytes;
                break;
            case megaBytes:
                newUnit = Unit.teraBytes;
                break;
            case bytesPerSec:
                newUnit = Unit.kiloBytesPerSec;
                break;
            case kiloBytesPerSec:
                newUnit = Unit.megaBytesPerSec;
                break;
            case megaBytesPerSec:
                newUnit = Unit.teraBytesPerSec;
                break;
            default:
                newUnit = unit;
                break;
        }
        return newUnit;
    }
    public static String getFormattedSpeed(long value, Unit unit) {
        Unit newUnit = unit;
        double convertedValue = value;


        while (convertedValue >= 1024 && (getHigherUnit(newUnit) != newUnit)) {
            convertedValue = convertedValue / 1024;
            newUnit = getHigherUnit(newUnit);
        }

        DecimalFormat df = new DecimalFormat("#.#");

        return df.format(convertedValue) + " " + unitToString(newUnit);
    }
}

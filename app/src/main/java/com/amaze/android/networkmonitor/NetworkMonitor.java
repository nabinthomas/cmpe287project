package com.amaze.android.networkmonitor;

import android.app.Activity;
import android.os.AsyncTask;

import androidx.core.view.ViewCompat;

interface NetworkMonitorEventListener {


    public void handleReportSpeed(long value, NetworkMonitor.Unit unit);
}

public class NetworkMonitor extends AsyncTask<NetworkMonitorEventListener, Integer, Long> {

    public enum Unit {
        bytesPerSec,
        kiloBytesPerSec,
        megaBytesPerSec,
        teraBytesPerSec
    };

    NetworkMonitorEventListener listener = null;

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
    public boolean setAppFilter() {
        /// @todo Setup App filter
        return true;
    }

    public void setSamplingIntervalMs(long samplingIntervalMs) {
        this.samplingIntervalMs = samplingIntervalMs;
    }

    protected Long doInBackground(NetworkMonitorEventListener... listener) {

        this.listener = listener[0];

        int i;

        for (i = 0; i < 100; i++) {
            try {
                Thread.sleep(samplingIntervalMs);
            }
            catch (Exception e) {
                // Ignore
            }
            publishProgress(i);
            if (this.listener != null)
                this.listener.handleReportSpeed(i, NetworkMonitor.Unit.bytesPerSec);
            if (isCancelled()) break;
        }
        return Long.parseLong("0");
    }
    protected void onProgressUpdate(Integer... progress) {
        System.out.println("Progress = " + progress[0]);
    }

    protected void onPostExecute(Long result) {
        System.out.println("Result = " + result);
    }
}

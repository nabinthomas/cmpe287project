package com.amaze.android.networkmonitor;

import android.app.Activity;
import android.os.AsyncTask;

import androidx.core.view.ViewCompat;

class NetworkMonitorEventListener {


    public void reportSpeed(long value, NetworkMonitor.Unit unit) {

        System.out.println("Progress Received = " + value + " " + NetworkMonitor.unitToString(unit));
    }
}

public class NetworkMonitor extends AsyncTask<NetworkMonitorEventListener, Integer, Long> {

    public enum Unit {
        bytesPerSec,
        kiloBytesPerSec,
        megaBytesPerSec,
        teraBytesPerSec
    };

    NetworkMonitorEventListener listener = null;

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

    protected Long doInBackground(NetworkMonitorEventListener... listener) {

        this.listener = listener[0];

        int i;

        for (i = 0; i < 100; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // Ignore
            }
            publishProgress(i);
            if (this.listener != null)
                this.listener.reportSpeed(i, NetworkMonitor.Unit.bytesPerSec);
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
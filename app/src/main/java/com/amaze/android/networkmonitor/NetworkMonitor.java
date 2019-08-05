package com.amaze.android.networkmonitor;

import android.app.Activity;
import android.os.AsyncTask;

class NetworkMonitorEventListener {
    public void progressReport(int i) {
        System.out.println("Progress = " + i);
    }
}

public class NetworkMonitor extends AsyncTask<NetworkMonitorEventListener, Integer, Long> {
    protected Long doInBackground(NetworkMonitorEventListener... listener) {

        int i;

        for (i = 0; i < 100; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // Ignore
            }
            publishProgress(i);
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

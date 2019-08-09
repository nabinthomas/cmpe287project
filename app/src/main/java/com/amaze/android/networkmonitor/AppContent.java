package com.amaze.android.networkmonitor;
import android.app.Application;
import android.content.pm.PackageItemInfo;
import 	android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppContent  {

    public static AppContent instance = null;
    public static AppContent getInstance( Context context) {
        if (instance !=null)
            return instance;
        else {
            instance = new AppContent(context);
            return instance;
        }
    }

    /**
     * An array of sample (App) items.
     */
    public final List<AppItem> ITEMS = new ArrayList<AppItem>();

    /**
     * A map of sample (App) items, by ID.
     */
    public final Map<String, AppItem> ITEM_MAP = new HashMap<String, AppItem>();

    private static final int COUNT = 10;


    private AppContent( Context context)  {

      // Applicationinfo ai =  getApplicationInfo ();
      // PackageManager pm = getApplicationContext().getPackageManager();
        PackageManager pm =   context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        // Add app data.
        int i = 1;
        for (PackageInfo packageInfo : packages) {
            //addItem(createAppItem(i++, packageInfo.packageName, packageInfo.versionName));
            String name = "" ;
            try {
                name =  pm.getApplicationLabel(pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA)).toString();

            }
            catch (Exception e){
                System.out.println("Exception Package  (" + i + ") = " + packageInfo.packageName + " Name = " +name);

            }
            //BINU
            addItem(createAppItem(i++, name, packageInfo.versionName));
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
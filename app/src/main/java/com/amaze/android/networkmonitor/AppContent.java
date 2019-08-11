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

        PackageManager pm =   context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        // Add app data.
        int i = 1;
        for (PackageInfo packageInfo : packages) {
            //addItem(createAppItem(i++, packageInfo.packageName, packageInfo.versionName));
            String name = "" ;
            try {
                name =  pm.getApplicationLabel(pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA)).toString();
                AppItem tempAppItem = createAppItem(i++, name, packageInfo.packageName);
                addItem(tempAppItem);
            }
            catch (Exception e){
                System.out.println("Exception Package  (" + i + ") = " + packageInfo.packageName + " Name = " +name);

            }

        }
    }

    private void addItem(AppItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
        //ITEM_MAP.put(item.appPkg, item);
    }

    private AppItem createAppItem(int position, String  Name, String packageName) {
        System.out.println("BINU  AppaItem    "+ packageName   );
        return new AppItem(String.valueOf(position),  Name, packageName);
    }

    /**
     * A App item representing a piece of content.
     */
    public class AppItem {
        public final String id;
        //public final String content;
        //public final String details;

        public final String appName;
        public final String appPkg;


        long TrafficRx;
        long TrafficTx;
        long TrafficMobRx;
        long TrafficMobTx;


        public AppItem(String id, String name, String pkg) {
            this.id = id;
            this.appName = name;
            this.appPkg = pkg;

            this.TrafficMobRx = 0 ;
            this.TrafficMobTx = 0 ;
            this.TrafficRx = 0 ;
            this.TrafficTx = 0 ;

        }

        @Override
        public String toString() {
            return appName ;//+ "TrafficRx:"+ TrafficRx + "  TrafficTx" +TrafficTx;
        }
    }
}
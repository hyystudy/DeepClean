package com.abclauncher.deepclean;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;


import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sks on 2017/1/22.
 */

public class RankDao {
    private static final String TAG = "RankDao";
    private static RankDao sInstance;
    private final Context mContext;
    private int mTotalTime;
    private  List<AppInfo> mAllRunningApps = new ArrayList<>();
    private  List<AppInfo> mNonSystemAppList = new ArrayList<>();
    private HashMap<String, AppInfo> mAllRunningAppsMap = new HashMap<>();
    private AllAppsLoadedListener mAllAppsLoadedListener;

    public RankDao(Context context) {
        mContext = context;
    }

    public static RankDao getInstance(Context context){
        if (sInstance == null) {
            sInstance = new RankDao(context);
        }
        return sInstance;
    }

    public List<AppInfo> getAllRunningAppsByCache(){
        return mAllRunningApps;
    }

    public List<AppInfo> getNonSystemAppList() {
        return mNonSystemAppList;
    }


    public void cleanData(){
        mAllRunningApps.clear();
        mAllRunningAppsMap.clear();
        mNonSystemAppList.clear();
    }

    public List<AppInfo> initRunningAppList(Context context, boolean needSystemApps) {
        List<AppInfo> runningApps = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            runningApps = findRunningAppsLollipop(context);
        } else {
            runningApps = findRunningApps(context);
        }
        PackageManager packageManager = context.getPackageManager();

        for (AppInfo appInfo : runningApps) {
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appInfo.pkgName, PackageManager.GET_META_DATA);
                appInfo.icon = applicationInfo.loadIcon(packageManager);

                appInfo.appName = (String) applicationInfo.loadLabel(packageManager);
                if (TextUtils.isEmpty(appInfo.appName)) {
                    appInfo.appName = appInfo.pkgName;
                }

                appInfo.isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                appInfo.percent = format((appInfo.cpuTime * 100.0d) / mTotalTime);
                if (mAllRunningAppsMap.containsKey(appInfo.appName)) {
                    Log.d(TAG, "initRunningAppList: --->" + appInfo.appName);
                    AppInfo appInfo1 = mAllRunningAppsMap.get(appInfo.appName);
                    appInfo1.cpuTime += appInfo.cpuTime;
                    appInfo1.percent = format((appInfo1.cpuTime * 100.0d) / mTotalTime);
                    continue;
                }else {
                    mAllRunningAppsMap.put(appInfo.appName, appInfo);
                }

            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "initRunningAppList: " + e.toString());
                e.printStackTrace();
            }
        }
        //初始化 所有 app
        mAllRunningApps.addAll(mAllRunningAppsMap.values());
        Collections.sort(mAllRunningApps, new CustomComparator());
        //初始化 所有 非系统app
        for (AppInfo appInfo : mAllRunningApps) {
            if (!appInfo.isSystemApp){
                mNonSystemAppList.add(appInfo);
            }
        }
        Collections.sort(mNonSystemAppList, new CustomComparator());


        if (mAllAppsLoadedListener != null) {
            mAllAppsLoadedListener.onAllAppsInited();
        }
        if (needSystemApps) {
            return mAllRunningApps;
        } else {
            return mNonSystemAppList;
        }

    }


    private List<AppInfo> findRunningApps(Context context) {
        mTotalTime = 0;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        Log.d(TAG, "caculateAppBattery: size--->" + runningApps.size());
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        for (ActivityManager.RunningAppProcessInfo info : runningApps) {
            final long time = getAppProcessTime(info.pid);
            String[] pkgNames = info.pkgList;
            AppInfo appInfo = new AppInfo();
            appInfo.pkgName = pkgNames[0];
            if (context.getPackageName().equals(appInfo.pkgName)) continue;
            appInfo.cpuTime = time;
            mTotalTime += appInfo.cpuTime;
            appInfos.add(appInfo);
        }

        //Collections.sort(appInfos, new CustomComparator());
        return appInfos;
    }

    public List<AppInfo> findRunningAppsLollipop(Context context) {
        mTotalTime = 0;
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        final List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess androidAppProcess : processes) {
            AppInfo appInfo = new AppInfo();
            final long time = getAppProcessTime(androidAppProcess.pid);
            appInfo.cpuTime = time;

            appInfo.pkgName = androidAppProcess.getPackageName();
            if (context.getPackageName().equals(appInfo.pkgName)) continue;
            mTotalTime += time;
            appInfos.add(appInfo);
        }
        //Collections.sort(appInfos, new CustomComparator());
        return appInfos;
    }

    public static long getAppProcessTime(int pid) {
        FileInputStream in = null;
        String ret = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/stat");
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            ret = os.toString();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ret == null) {
            return 0;
        }

        String[] s = ret.split(" ");
        if (s == null || s.length < 17) {
            return 0;
        }

        final long utime = string2Long(s[13]);
        final long stime = string2Long(s[14]);
        final long cutime = string2Long(s[15]);
        final long cstime = string2Long(s[16]);

        return utime + stime + cutime + cstime;
    }

    private static long string2Long(String s) {
        if (s != null) {
            //Log.d(TAG, "string2Long: " + s);
            return Long.valueOf(s);
        }
        return 0;
    }

    /**
     * 使用NumberFormat,保留小数点后两位
     */
    public static String format(double value) {
        Log.d(TAG, "format: " + value);
        if (value < 0.01) value = 0.01;

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        /*
         * setMinimumFractionDigits设置成2
         *
         * 如果不这么做，那么当value的值是100.00的时候返回100
         *
         * 而不是100.00
         */
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        /*
         * 如果想输出的格式用逗号隔开，可以设置成true
         */
        nf.setGroupingUsed(false);
        return nf.format(value);
    }

    public interface AllAppsLoadedListener {
        public void onAllAppsInited();
    }

    public void setAppAppsLoadedListener(AllAppsLoadedListener appAppsInitedListener){
        mAllAppsLoadedListener = appAppsInitedListener;
    }
}

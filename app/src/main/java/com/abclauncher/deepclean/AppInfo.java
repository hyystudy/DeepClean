package com.abclauncher.deepclean;

import android.graphics.drawable.Drawable;

/**
 * Created by sks on 2016/12/22.
 */

public class AppInfo implements Comparable<AppInfo> {
    public Drawable icon;
    public String pkgName;
    public String appName;
    public long cpuTime;
    public boolean isSystemApp;
    public String percent;


    @Override
    public int compareTo(AppInfo o) {
        return (int) (this.cpuTime - o.cpuTime);
    }
}

package com.abclauncher.deepclean;


import java.util.Comparator;

/**
 * Created by sks on 2016/12/22.
 */

public class CustomComparator implements Comparator<AppInfo> {
    @Override
    public int compare(AppInfo o1, AppInfo o2) {
        if (o1 == o2) return 0;
        if (o1.cpuTime == o2.cpuTime) return 0;
        return o1.cpuTime > o2.cpuTime ? -1 : 1;
    }
}



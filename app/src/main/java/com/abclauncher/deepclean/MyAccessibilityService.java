package com.abclauncher.deepclean;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by sks on 2017/1/22.
 */

public class MyAccessibilityService  extends AccessibilityService{
    private static final String TAG = "MyAccessibilityService";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent: ");
        if(null == accessibilityEvent || null == accessibilityEvent.getSource()) { return; }
        if (accessibilityEvent.getSource() != null) {
            Log.d(TAG, "onAccessibilityEvent: " + accessibilityEvent.getPackageName());
            if (accessibilityEvent.getPackageName().equals("com.android.settings")) {
                List<AccessibilityNodeInfo> stop_nodes = accessibilityEvent.getSource().findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
                if (stop_nodes!=null){
                    Log.d(TAG, "onAccessibilityEvent: " + stop_nodes.size());
                }

                if (stop_nodes!=null && !stop_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<stop_nodes.size(); i++){
                        node = stop_nodes.get(i);
                        Log.d(TAG, "onAccessibilityEvent: " + node.getPackageName());
                        if (node.getClassName().equals("android.widget.Button")) {
                            if(node.isEnabled()){
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } else {
                                performGlobalAction(GLOBAL_ACTION_BACK);
                            }
                            node.recycle();
                        }
                    }
                }

                List<AccessibilityNodeInfo> ok_nodes = null;
                if(accessibilityEvent.getText() != null && accessibilityEvent.getText().size() == 4) {
                    ok_nodes = accessibilityEvent.getSource().findAccessibilityNodeInfosByText(accessibilityEvent.getText().get(3).toString());
                    Log.d(TAG, "click ok" + accessibilityEvent.getText().get(3));
                }
                if (ok_nodes!=null && !ok_nodes.isEmpty()) {
                    AccessibilityNodeInfo node;
                    for(int i=0; i<ok_nodes.size(); i++){
                        node = ok_nodes.get(i);
                        if (node.getClassName().equals("android.widget.Button")) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.d(TAG, "click ok");
                        }
                        node.recycle();
                    }
                }
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected: ");
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.packageNames = new String[]{"com.android.settings"};
        serviceInfo.notificationTimeout=100;
        setServiceInfo(serviceInfo);
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {

    }
}

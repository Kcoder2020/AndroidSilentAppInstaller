package com.example.SilentAppInstaller;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brian on 3/10/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.DONUT)
public class SilentInstaller extends AccessibilityService {

    @SuppressLint("UseSparseArrays")
    Map<Integer, Boolean> handledMap = new HashMap<>();


    @Override
    public void onServiceConnected() {
        Log.d("SilentInstaller", "Starting service");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo;

        nodeInfo = event.getSource();
        CharSequence list = event.getPackageName();
        String listString = list.toString();
        String textAccess = (String) event.getClassName();
        Log.d("Text Access:  ", textAccess);

        Log.d("Event.getsource(): ", "node info reached");
        if (nodeInfo != null) {
            int eventType = event.getEventType();
            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
              if (textAccess.contains("install") || textAccess.contains("application") ||
                      textAccess.contains("settings") || textAccess.contains("SubSettings"))
              // com.android.packageinstaller
              {
                if (handledMap.get(event.getWindowId()) == null) {
                    boolean handled = iterateNodesAndHandle(nodeInfo);
                    if (handled) {
                        handledMap.put(event.getWindowId(), true);
                    }
                }
        }
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            int childCount = nodeInfo.getChildCount();
            if ("android.widget.Button".contentEquals(nodeInfo.getClassName())) {
                String nodeContent = nodeInfo.getText().toString();
                Log.d("TAG", "content is " + nodeContent);
                if ("Install".equals(nodeContent)
                        || "Finish".equals(nodeContent)
                        || "Ok".equals(nodeContent) || "Done".equals(nodeContent)
                        || "Allow".equals(nodeContent) || "Settings".equals(nodeContent)) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            } else if ("android.widget.ScrollView".contentEquals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (iterateNodesAndHandle(childNodeInfo)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onInterrupt() {

    }


}

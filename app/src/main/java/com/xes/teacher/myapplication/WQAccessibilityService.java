package com.xes.teacher.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Android 辅助功能
 * <p>
 * Created by WangQing on 2016/10/25.
 */

public class WQAccessibilityService extends AccessibilityService {

    private static final String TAG = "WQAccessibilityService";

    private static final int FLAG_MESSAGE_CLICK_EVENT = 0;
    private static final int FLAG_MESSAGE_INPUT_EVENT = 1;
    private static final int FLAG_MESSAGE_SCROLL_EVENT = 2;
    private static final int FLAG_MESSAGE_REVIEW_EVENT = 3;


    private static final int SEND_DELAY_TIME = 1000;


    private static final String BUTTON = Button.class.getCanonicalName();
    private static final String TEXTVIEW = TextView.class.getCanonicalName();
    private static final String EDITTEXT = EditText.class.getCanonicalName();
    private static final String LISTVIEW = ListView.class.getCanonicalName();

    Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            AccessibilityNodeInfo node = (AccessibilityNodeInfo) msg.obj;

            switch (msg.what) {
                case FLAG_MESSAGE_REVIEW_EVENT:
                case FLAG_MESSAGE_CLICK_EVENT:
                    runPerformAction(node, AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                case FLAG_MESSAGE_INPUT_EVENT:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // TODO: 2016/10/26  可以再次输入内容
                        runPerformAction(node, AccessibilityNodeInfo.ACTION_SET_TEXT, "可以再次输入内容");
                    }
                    break;
                case FLAG_MESSAGE_SCROLL_EVENT:
                    runPerformAction(node, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                    break;
            }
        }

        /**
         * 执行对应操作 事件
         * @param node
         * @param type
         */
        private void runPerformAction(AccessibilityNodeInfo node, int type) {
            runPerformAction(node, type, "");
        }

        /**
         * 执行对应操作 事件
         * @param node
         * @param type
         */
        private void runPerformAction(AccessibilityNodeInfo node, int type, String text) {
            if (node != null) {
                if (AccessibilityNodeInfo.ACTION_SET_TEXT == type) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 大于等于 5.0 系统 可以给 设置  文本
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    }
                } else {
                    node.performAction(type);
                }
            }
        }
    };


    /**
     * 通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，过滤是在配置工作时设置的。
     * <p>
     * 这是异步通知
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "onAccessibilityEvent: " + event.getText().toString());
        nodeInfo(event);
    }

    private void nodeInfo(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo != null) {
            if (getRootInActiveWindow() == null)
                return;

            // TODO: 2016/10/26  这里处理相关事件

            checkName(TEXTVIEW, "搜索");

            checkName(EDITTEXT, "com.tencent.mm:id/fo");

            checkName(LISTVIEW, "com.tencent.mm:id/bfr");
        }
    }

    /**
     * 检测名字和数据
     * <p>
     * 务必 大于 等于 Android 4.3 （18） 版本
     * <p>
     * 版本 至少要大约 等于   Android 4.1 （16） 版本，才能使用文字查找。
     * 版本 至少要大约 等于   Android 4.3 （18） 版本，才能使用ID 查找。
     * <p>
     * 版本 至少要大约 等于   Android 5.0（21） 版本，才能使用 动态在 EditText 里面 输入文本内容。
     *
     * @param type
     * @param keyWorld
     */
    private void checkName(String type, String keyWorld) {
        //通过文字 找到当前的节点
        List<AccessibilityNodeInfo> nodes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //版本大于等于   Android 4.0 （14） 版本 可以使用 findAccessibilityNodeInfosByText
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { //版本大于等于   Android 4.1 （16） 版本 可以使用 getRootInActiveWindow
                nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(keyWorld);
            }
        }

        if (nodes != null && nodes.size() > 0) {
            matchData(type, nodes);
        } else {
            //版本大于等于   Android 4.3 版本 匹配 ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { //版本大于等于   Android 4.3 （18） 版本 可以使用 findAccessibilityNodeInfosByViewId
                nodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(keyWorld);
                if (nodes != null && nodes.size() > 0) {
                    matchData(type, nodes);
                }
            }
        }
    }


    /**
     * 匹配数据， 延迟 后发送消息
     *
     * @param type
     * @param nodes
     */
    private void matchData(String type, List<AccessibilityNodeInfo> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
            if (node.getClassName().equals(type) && node.isEnabled()) {
                int what = getWhatState(type);
                handler.removeMessages(what);

                Message msg = handler.obtainMessage();
                msg.what = what;
                msg.obj = node;
                handler.sendMessageDelayed(msg, SEND_DELAY_TIME);
            }
        }
    }

    /**
     * 获取状态类型
     *
     * @param type
     * @return
     */
    private int getWhatState(String type) {
        int what;

        if (EDITTEXT.equals(type)) {
            what = FLAG_MESSAGE_INPUT_EVENT;
        } else if (LISTVIEW.equals(type)) {
            what = FLAG_MESSAGE_SCROLL_EVENT;
        } else if (TEXTVIEW.equals(type)) {
            what = FLAG_MESSAGE_REVIEW_EVENT;
        } else {
            what = FLAG_MESSAGE_CLICK_EVENT;
        }

        return what;
    }

    /**
     * 这个在系统想要中断AccessibilityService返给的响应时会调用。在整个生命周期里会被调用多次。
     */
    @Override
    public void onInterrupt() {

    }

    /**
     * 在系统将要关闭这个AccessibilityService会被调用。在这个方法中进行一些释放资源的工作。
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}

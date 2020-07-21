package com.test.virtureclick.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.test.virtureclick.R
import com.test.virtureclick.tools.d
import com.test.virtureclick.tools.trimBrackets
import kotlinx.coroutines.*


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private var mJob : Job?=null

    //服务中断时的回调
    override fun onInterrupt() {
        "onInterrupt".d(TAG)
        mJob?.cancel()
    }


    //接收到系统发送AccessibilityEvent时的回调
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // "onAccessibilityEvent:event:$event".d(TAG)
        analyzeEvent(event, rootInActiveWindow)
        event.recycle()
    }

    private fun analyzeEvent(event: AccessibilityEvent, nodeInfo: AccessibilityNodeInfo) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                "analyzeEvent , event.text:${event.text}".d(TAG)
                when(event.text.toString().trimBrackets()){
                    resources.getString(R.string.Heartstone_surrender) -> {
                        mJob = GlobalScope.launch(Dispatchers.IO) {
                            performSurrender(nodeInfo)
                        }
                    }
                    resources.getString(R.string.Heartstone_cancel) -> {
                        mJob?.cancel()
                    }

                }

            }
        }
    }

    private suspend fun performSurrender(nodeInfo: AccessibilityNodeInfo) {
        "performSurrender 点击设置".d(TAG)
        click(2163, 30)

        delay(2000)
        "performSurrender 点击投降".d(TAG)
        click(1239, 240)


        delay(8000)
        "performSurrender 点击空白区域".d(TAG)
        click(560, 560)

        delay(1000)
        "performSurrender 点击空白区域".d(TAG)
        click(560, 560)

        delay(5000)
        "performSurrender 点击对战".d(TAG)
        click(1750, 934)
    }


    private fun click(x: Int, y: Int) {
        val clickPath = Path()
        val builder = GestureDescription.Builder()
        clickPath.moveTo(x.toFloat(), y.toFloat())
        val gestureDescription =
            builder.addStroke(GestureDescription.StrokeDescription(clickPath, 100, 50)).build()
        dispatchGesture(gestureDescription, null, null)
    }


}
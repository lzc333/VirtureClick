package com.test.virtureclick.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.graphics.Path
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.test.virtureclick.R
import com.test.virtureclick.bean.Coordinate
import com.test.virtureclick.tools.d
import com.test.virtureclick.tools.trimBrackets
import kotlinx.coroutines.*
import kotlin.system.exitProcess


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private lateinit var vibrator: Vibrator
    private val mListJob: ArrayList<Job> = arrayListOf()

    //服务中断时的回调
    override fun onInterrupt() {
        "onInterrupt".d(TAG)
        cancel()
        exitProcess(0)
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
                val id = event?.source?.viewIdResourceName?.split("/")?.get(1)
                "analyzeEvent , id:$id".d(TAG)
                when (id) {
                    "floatwindow_surrender_tv" -> {
                        cancel()
                        vibrator.vibrate(120)
                        mListJob.add(
                            GlobalScope.launch(Dispatchers.IO) {
                                performSurrender(nodeInfo)
                            }
                        )
                    }

                    "floatwindow_cancel_tv" -> {
                        vibrator.vibrate(120)
                        cancel()
                    }

                    "floatwindow_next_tv" -> {
                        vibrator.vibrate(120)
                        cancel()
                        mListJob.add(
                            GlobalScope.launch(Dispatchers.IO) {
                                performNext(nodeInfo)
                            }
                        )
                    }
                }

            }
        }
    }

    private suspend fun performNext(nodeInfo: AccessibilityNodeInfo) {
        delay(5000)
        "performNext 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        delay(1200)
        "performNext 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        //升星或降星需要再点击一次
        delay(1500)
        "performNext 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        delay(5000)
        "performNext 点击对战".d(TAG)
        click(Coordinate.heartstone_battle)

        nodeInfo.recycle()

    }

    private suspend fun performSurrender(nodeInfo: AccessibilityNodeInfo) {
        "performSurrender 点击设置".d(TAG)
        click(Coordinate.heartstone_setting)

        delay(500)
        "performSurrender 点击认输".d(TAG)
        click(Coordinate.heartstone_surrender)

        delay(8000)
        "performSurrender 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        delay(1000)
        "performSurrender 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        //升星或降星需要再点击一次
        delay(1000)
        "performSurrender 点击空白区域".d(TAG)
        click(Coordinate.heartstone_blank)

        delay(5000)
        "performSurrender 点击对战".d(TAG)
        click(Coordinate.heartstone_battle)

        nodeInfo.recycle()
    }


    private fun cancel() {
        mListJob.forEach {
            it.cancel()
        }
        mListJob.clear()
    }

    private fun click(coordinate: Coordinate) {
        val clickPath = Path()
        val builder = GestureDescription.Builder()
        clickPath.moveTo(coordinate.x.toFloat(), coordinate.y.toFloat())
        val gestureDescription =
            builder.addStroke(GestureDescription.StrokeDescription(clickPath, 100, 50)).build()
        dispatchGesture(gestureDescription, null, null)
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
    }


}
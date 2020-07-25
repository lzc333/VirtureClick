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
import com.test.virtureclick.bean.NextNumberEvent
import com.test.virtureclick.tools.d
import com.test.virtureclick.tools.trimBrackets
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.greenrobot.eventbus.EventBus
import kotlin.system.exitProcess


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private lateinit var vibrator: Vibrator
    private val mListJob: ArrayList<Job> = arrayListOf()
    private var nextNumber = 0

    //服务中断时的回调
    override fun onInterrupt() {
        "onInterrupt".d(TAG)
        cancel()
        exitProcess(0)
    }


    //接收到系统发送AccessibilityEvent时的回调
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // "onAccessibilityEvent:event:$event".d(TAG)
        analyzeEvent(event)
        rootInActiveWindow?.recycle()
        event?.recycle()
    }

    private fun analyzeEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val id = event.source?.viewIdResourceName?.split("/")?.get(1)
                "analyzeEvent , id:$id".d(TAG)
                when (id) {
                    "floatwindow_surrender_tv" -> {
                        cancel()
                        nextNumber = 0
                        EventBus.getDefault().post(NextNumberEvent(nextNumber))
                        mListJob.add(
                            GlobalScope.launch(Dispatchers.Default) {
                                performSurrender()
                            }
                        )
                    }

                    "floatwindow_cancel_tv" -> {
                        cancel()
                    }

                    "floatwindow_next_tv" -> {
                        cancel()
                        nextNumber++
                        EventBus.getDefault().post(NextNumberEvent(nextNumber))
                        mListJob.add(
                            GlobalScope.launch(Dispatchers.Default) {
                                performNext()
                            }
                        )
                    }
                }
            }
        }
    }

    private suspend fun performNext() {
        "performNext  start".d(TAG)
        flow {
            for (i in 1..33) {
                emit(i)
                delay(350)
            }
        }.collect {
            click(Coordinate.heartstone_battle)
            "performNext 点击对战 ,第${it}次".d(TAG)
        }
    }

    private suspend fun performSurrender() {
        "performSurrender 点击设置".d(TAG)
        click(Coordinate.heartstone_setting)

        delay(200)
        "performSurrender 点击认输".d(TAG)
        click(Coordinate.heartstone_surrender)

        //认输动画结束
        delay(5000)

        flow {
            for (i in 1..20) {
                emit(i)
                delay(350)
            }
        }.collect {
            click(Coordinate.heartstone_battle)
            "performSurrender 点击对战 ,第${it}次".d(TAG)
        }

    }


    private fun cancel() {
        vibrator.vibrate(120)
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
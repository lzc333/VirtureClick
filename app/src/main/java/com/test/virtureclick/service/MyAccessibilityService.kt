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
                        nextNumber = 0
                        EventBus.getDefault().post(NextNumberEvent(nextNumber))
                        mListJob.add(
                            GlobalScope.launch(Dispatchers.Default) {
                                performSurrender(this, nodeInfo)
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
                                performNext(this, nodeInfo)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun performNext(coroutineScope: CoroutineScope, nodeInfo: AccessibilityNodeInfo) {
        coroutineScope.launch {
            "performNext  start".d(TAG)
            coroutineScope.launch {
                flow {
                    for (i in 1..21) {
                        emit(i)
                        delay(433)
                    }
                }.collect {
                    click(Coordinate.heartstone_blank)
                    "performNext 点击空白区域 ,第${it}次".d(TAG)
                }
            }


            delay(4500)
            coroutineScope.launch {
                flow {
                    for (i in 1..16) {
                        emit(i)
                        delay(450)
                    }
                }.collect {
                    click(Coordinate.heartstone_battle)
                    "performNext 点击对战 ,第${it}次".d(TAG)
                }
            }

            nodeInfo.recycle()
        }


    }

    private fun performSurrender(coroutineScope: CoroutineScope, nodeInfo: AccessibilityNodeInfo) {
        coroutineScope.launch {
            "performSurrender 点击设置".d(TAG)
            click(Coordinate.heartstone_setting)

            delay(150)
            "performSurrender 点击认输".d(TAG)
            click(Coordinate.heartstone_surrender)

            //认输动画结束
            delay(5500)

            //delay5.7s的同时，每457ms，点击空白区域
            coroutineScope.launch(Dispatchers.Default) {
                flow {
                    for (i in 1..10) {
                        emit(i)
                        delay(457)
                    }
                }.collect {
                    click(Coordinate.heartstone_blank)
                    "performSurrender 点击空白区域 ,第${it}次".d(TAG)
                }
            }

            delay(5377)
            flow {
                for (i in 1..4) {
                    emit(i)
                    delay(437)
                }
            }.collect {
                click(Coordinate.heartstone_battle)
                "performSurrender 点击对战 ,第${it}次".d(TAG)
            }


            nodeInfo.recycle()
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
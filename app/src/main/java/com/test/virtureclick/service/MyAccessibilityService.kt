package com.test.virtureclick.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.graphics.Path
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import com.test.virtureclick.bean.ClickInterval
import com.test.virtureclick.bean.Coordinate
import com.test.virtureclick.bean.HangUpEvent
import com.test.virtureclick.bean.NextNumberEvent
import com.test.virtureclick.tools.FlavorUtils
import com.test.virtureclick.tools.d
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
    private var isHangUp = false

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
                //小米5手机拿不到id，所以根据text来判断。小米9其实也可以通过text来做判断，但这里还是拿id来判断
                if(FlavorUtils.isXiaoMi5){
                    val text = event.source?.text
                    "analyzeEvent , text:$text".d(TAG)
                    when(text){
                        "hang up"->{
                            clickHangUp()
                        }
                        "surrender"->{
                            clickSurrender()
                        }
                        "cancel"->{
                            clickCancel()
                        }
                        "next"->{
                            clickNext()
                        }
                    }
                }else if(FlavorUtils.isXiaoMi9){
                    val id = event.source?.viewIdResourceName?.split("/")?.get(1)
                    "analyzeEvent , id:$id".d(TAG)
                    when (id) {
                        "floatwindow_hangup_bt"-> {
                           clickHangUp()
                        }

                        "floatwindow_surrender_bt" -> {
                           clickSurrender()
                        }

                        "floatwindow_cancel_bt" -> {
                           clickCancel()
                        }

                        "floatwindow_next_bt" -> {
                           clickNext()
                        }
                    }
                }
            }
        }
    }

    private fun clickHangUp(){
        cancel()
        isHangUp = !isHangUp
        EventBus.getDefault().post(HangUpEvent(isHangUp))
        mListJob.add(
            GlobalScope.launch(Dispatchers.Default) {
                performHangUp(this)
            }
        )
    }

    private fun clickSurrender(){
        cancel()
        isHangUp = false
        EventBus.getDefault().post(HangUpEvent(isHangUp))
        nextNumber = 0
        EventBus.getDefault().post(NextNumberEvent(nextNumber))
        mListJob.add(
            GlobalScope.launch(Dispatchers.Default) {
                performSurrender()
            }
        )
    }

    private fun clickCancel(){
        cancel()
        isHangUp = false
        EventBus.getDefault().post(HangUpEvent(isHangUp))
    }

    private fun clickNext(){
        cancel()
        isHangUp = false
        EventBus.getDefault().post(HangUpEvent(isHangUp))
        nextNumber++
        EventBus.getDefault().post(NextNumberEvent(nextNumber))
        mListJob.add(
            GlobalScope.launch(Dispatchers.Default) {
                performNext()
            }
        )
    }

    private suspend fun performNext() {
        "performNext  start".d(TAG)
        flow {
            for (i in 1..33) {
                emit(i)
                delay(ClickInterval.heartstone_next_battle.timeMillis)
            }
        }.collect {
            click(Coordinate.heartstone_battle)
            "performNext 点击对战 ,第${it}次".d(TAG)
        }
    }

    private suspend fun performHangUp(coroutineScope: CoroutineScope) {
        "performHangUp  start".d(TAG)
        click(Coordinate.heartstone_battle)
        delay(ClickInterval.heartstone_hangup.timeMillis)

        coroutineScope.launch(Dispatchers.Default) {
            var number_battle = 0
            while (isHangUp){
                click(Coordinate.heartstone_battle)
                "performHangUp 点击对战 ,第${++number_battle}次".d(TAG)
                delay(ClickInterval.heartstone_hangup_battle.timeMillis)
            }
        }

        coroutineScope.launch(Dispatchers.Default) {
            var number_end_round = 0
            while (isHangUp){
                click(Coordinate.heartstone_end_round)
                "performHangUp 点击结束回合 ,第${++number_end_round}次".d(TAG)
                delay(ClickInterval.heartstone_hangup_end_round.timeMillis)
            }
        }

        coroutineScope.launch(Dispatchers.Default) {
            var number_sure_round = 0
            while (isHangUp){
                click(Coordinate.heartstone_sure_round)
                "performHangUp 点击确认 ,第${++number_sure_round}次".d(TAG)
                delay(ClickInterval.heartstone_hangup_sure_round.timeMillis)
            }
        }


    }

    private suspend fun performSurrender() {
        "performSurrender 点击设置".d(TAG)
        click(Coordinate.heartstone_setting)

        delay(ClickInterval.heartstone_surrender_setting.timeMillis)
        "performSurrender 点击认输".d(TAG)
        click(Coordinate.heartstone_surrender)

        //认输动画结束
        delay(ClickInterval.heartstone_surrender_animation.timeMillis)

        flow {
            for (i in 1..22) {
                emit(i)
                delay(ClickInterval.heartstone_surrender_battle.timeMillis)
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
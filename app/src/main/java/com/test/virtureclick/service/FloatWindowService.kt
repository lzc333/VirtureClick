package com.test.virtureclick.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.test.virtureclick.R
import com.test.virtureclick.bean.NextNumberEvent
import com.test.virtureclick.tools.clickWithTrigger
import com.test.virtureclick.tools.d
import com.test.virtureclick.tools.showToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs

class FloatWindowServices : Service() {
    private val TAG = "FloatWindowServices"
    private var winManager: WindowManager? = null
    private var wmParams: WindowManager.LayoutParams? = null
    private var inflater: LayoutInflater? = null

    //浮动布局
    private var mFloatingLayout: View? = null
    private var mNextBt: Button? = null
    private var chronometer: Chronometer? = null


    override fun onBind(intent: Intent): IBinder? {
        "onBind".d(TAG)
        initWindow()
        //悬浮框点击事件的处理
        initFloating()
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        val service: FloatWindowServices
            get() = this@FloatWindowServices
    }

    override fun onCreate() {
        "onCreate".d(TAG)
        super.onCreate()
        EventBus.getDefault().register(this@FloatWindowServices)
    }

    /**
     * 悬浮窗点击事件
     */
    private fun initFloating() {
        mFloatingLayout?.findViewById<LinearLayout>(R.id.floatwindow_layout)
            ?.setOnTouchListener(FloatingListener())

        mFloatingLayout?.findViewById<Button>(R.id.floatwindow_surrender_tv)
            ?.clickWithTrigger(500) {
                "认输".showToast(this)
            }
        mFloatingLayout?.findViewById<Button>(R.id.floatwindow_cancel_tv)?.clickWithTrigger(500) {
            "取消".showToast(this)
        }
        mNextBt = mFloatingLayout?.findViewById<Button>(R.id.floatwindow_next_tv)
        mNextBt?.clickWithTrigger(500) {
            "下一局".showToast(this)
        }
    }


    //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
    private var mTouchStartX: Int = 0
    private var mTouchStartY: Int = 0
    private var mTouchCurrentX: Int = 0
    private var mTouchCurrentY: Int = 0

    //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
    private var mStartX: Int = 0
    private var mStartY: Int = 0
    private var mStopX: Int = 0
    private var mStopY: Int = 0

    //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
    private var isMove: Boolean = false

    private inner class FloatingListener : View.OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMove = false
                    mTouchStartX = event.rawX.toInt()
                    mTouchStartY = event.rawY.toInt()
                    mStartX = event.x.toInt()
                    mStartY = event.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    mTouchCurrentX = event.rawX.toInt()
                    mTouchCurrentY = event.rawY.toInt()
                    wmParams!!.x += mTouchCurrentX - mTouchStartX
                    wmParams!!.y += mTouchCurrentY - mTouchStartY
                    winManager!!.updateViewLayout(mFloatingLayout, wmParams)
                    mTouchStartX = mTouchCurrentX
                    mTouchStartY = mTouchCurrentY
                }
                MotionEvent.ACTION_UP -> {
                    mStopX = event.x.toInt()
                    mStopY = event.y.toInt()
                    if (abs(mStartX - mStopX) >= 1 || abs(mStartY - mStopY) >= 1) {
                        isMove = true
                    }
                }
                else -> {
                }
            }

            //如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
            return isMove
        }
    }

    /**
     * 初始化窗口
     */
    private fun initWindow() {
        winManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //设置好悬浮窗的参数
        wmParams = params
        // 悬浮窗默认显示以左上角为起始坐标
        wmParams?.run {
            gravity = Gravity.START or Gravity.TOP
            //悬浮窗的开始位置，因为设置的是从左上角开始，所以屏幕左上角是x=0;y=0
            x = winManager!!.defaultDisplay.width
            y = 210

        }
        //得到容器，通过这个inflater来获得悬浮窗控件
        inflater = LayoutInflater.from(applicationContext)
        // 获取浮动窗口视图所在布局
        mFloatingLayout = inflater!!.inflate(R.layout.view_floatwindow, null)
        chronometer = mFloatingLayout!!.findViewById<Chronometer>(R.id.floatwindow_chronometer)
        chronometer?.start()
        // 添加悬浮窗的视图
        winManager?.addView(mFloatingLayout, wmParams)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: NextNumberEvent) {
        "handleEvent event = $event".d(TAG)
        val color = when (event.number) {
            0 -> android.R.color.white
            1 -> android.R.color.holo_green_light
            else -> android.R.color.holo_red_light
        }
        mNextBt?.setTextColor(ContextCompat.getColor(this@FloatWindowServices, color))
    }

    //设置可以显示在状态栏上
    //设置悬浮窗口长宽数据
    //设置悬浮窗透明
    private val params: WindowManager.LayoutParams?
        get() {
            wmParams = WindowManager.LayoutParams()
            wmParams?.run {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                format = PixelFormat.TRANSPARENT
            }
            return wmParams
        }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        "onDestroy".d(TAG)
        winManager!!.removeView(mFloatingLayout)
        EventBus.getDefault().unregister(this@FloatWindowServices)
    }

}
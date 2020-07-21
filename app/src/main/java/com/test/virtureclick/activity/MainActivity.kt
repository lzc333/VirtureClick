package com.test.virtureclick.activity

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.test.virtureclick.R
import com.test.virtureclick.service.FloatWindowServices
import com.test.virtureclick.service.MyAccessibilityService
import com.test.virtureclick.tools.d
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        "onCreate".d(TAG)
        start_accessibility_bt.setOnClickListener {
            if(isAccessibilitySettingsOn(this@MainActivity, MyAccessibilityService::class.java)){
                Toast.makeText(this@MainActivity,"已开启无障碍权限！",Toast.LENGTH_SHORT).show()
            }else{
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        start_window_bt.setOnClickListener {
            startService()
        }
        close_window_bt.setOnClickListener {
            unbindService(mServiceConnection)
        }
    }


    private fun startService() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show()
            startActivityForResult(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ), 0
            )
        } else {
            val intent = Intent(this@MainActivity, FloatWindowServices::class.java)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }

    }

    private var mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // 获取服务的操作对象
            val binder = service as FloatWindowServices.MyBinder
            binder.service
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                if (requestCode == 0) {
                    if (!Settings.canDrawOverlays(this@MainActivity)) {
                        Toast.makeText(this@MainActivity, "授权失败", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this@MainActivity, FloatWindowServices::class.java)
                        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
                    }
                }
            }
        }
    }


   private fun isAccessibilitySettingsOn(mContext: Context, clazz: Class<out AccessibilityService?>): Boolean {
        var accessibilityEnabled = 0
        val service = mContext.packageName + "/" + clazz.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onRestart() {
        super.onRestart()
        "onRestart".d(TAG)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
package com.test.virtureclick.tools

import android.content.Context
import android.widget.Toast

private var lastShowTime = 0L
private var lastShowMsg: String? = null
private var curShowMsg: String? = null
private const val DURATION = 2000

fun CharSequence?.showToast(context: Context) {
    this?.run {
        curShowMsg = this.toString()
        val curShowTime = System.currentTimeMillis()
        if (curShowMsg == lastShowMsg) {
            if (curShowTime - lastShowTime > DURATION) {
                Toast.makeText(context, this, DURATION).show()
                lastShowTime = curShowTime
                lastShowMsg = curShowMsg
            }
        } else {
            Toast.makeText(context, this, DURATION).show()
            lastShowTime = curShowTime
            lastShowMsg = curShowMsg
        }
    }
}
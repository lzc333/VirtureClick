package com.test.virtureclick.tools

import android.util.Log
import android.util.Patterns
import java.util.regex.Pattern

const val TAG = "Liuzhicheng"

fun String.d(tag: String = TAG) {
    Log.d(tag, this)
}

fun String.trimBrackets() =
 this.replace("[","").replace("]","")



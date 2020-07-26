package com.test.virtureclick.tools

import com.test.virtureclick.BuildConfig


object FlavorUtils {
    val isXiaoMi5: Boolean
        get() = "xiaomi5" == BuildConfig.FLAVOR

    val isXiaoMi9: Boolean
        get() = "xiaomi9" == BuildConfig.FLAVOR

    val flavor: String
        get() =  BuildConfig.FLAVOR
}
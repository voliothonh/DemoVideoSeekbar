package com.ht.videoseekerview.seek

import android.content.res.Resources

object MediaConstant {
    const val MIN_SCALE_FACTOR = 0.5f
    const val MAX_SCALE_FACTOR = 5f
    const val DEFAULT_SCALE_FACTOR = 1f
    const val PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR = 150
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun halfScreenWidth(): Int {
    return getScreenWidth() / 2
}

fun halfScreenHeight(): Int {
    return getScreenHeight() / 2
}
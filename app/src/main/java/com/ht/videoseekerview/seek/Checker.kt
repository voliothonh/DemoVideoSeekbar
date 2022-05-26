package com.ht.videoseekerview.seek

import android.view.MotionEvent


fun convertTimeToPixel(timeInMillis: Long, scale: Float): Float {
    return MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR * scale * timeInMillis * 1f / 1000f
}

fun convertPixelToTime(xPosition: Int, scale: Float): Long {
    return (1000f * xPosition / MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR / scale).toLong()
}

fun SeekVideoView.checkClickToSelectThumb(event: MotionEvent?): ThumbMediaView? {
    if (event == null || event.action != MotionEvent.ACTION_UP)
        return null

    val xInRealPixel = (event.x + xPositionScroll).toInt()
    val yInRealPixel = (event.y).toInt()

    return listThumb.find {
        it.getRectWithOffset.contains(xInRealPixel, yInRealPixel)
    }
}


fun SeekVideoView.checkTouchToDragExpandThumb(event: MotionEvent?) {
    if (event == null || event.action != MotionEvent.ACTION_DOWN)
        return

    anchorLeftThumbSelectedRect ?: return
    anchorRightThumbSelectedRect ?: return

    val xInRealPixel = (event.x + xPositionScroll).toInt()
    val yInRealPixel = (event.y).toInt()

    isDragLeftAnchorToExpandThumb =
        anchorLeftThumbSelectedRect!!.contains(xInRealPixel, yInRealPixel)
    isDragRightAnchorToExpandThumb =
        anchorRightThumbSelectedRect!!.contains(xInRealPixel, yInRealPixel)
}
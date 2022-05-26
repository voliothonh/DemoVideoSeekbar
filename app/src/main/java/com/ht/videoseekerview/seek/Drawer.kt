package com.ht.videoseekerview.seek

import android.content.res.Resources
import android.graphics.*
import android.util.Log
import com.ht.videoseekerview.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt


private val paintCenterLine = Paint().apply {
    isAntiAlias = true
    color = Color.WHITE
    strokeCap = Paint.Cap.SQUARE
    strokeJoin = Paint.Join.MITER
    strokeWidth = 4f
}


private val paintListThumb = Paint().apply {
    isAntiAlias = true
}


private val paintTextTime = Paint().apply {
    isAntiAlias = true
    color = Color.WHITE
    textAlign = Paint.Align.CENTER
    typeface = Typeface.DEFAULT
    textSize = 12f
    strokeWidth = 4f
}


fun SeekVideoView.drawCenterLine(canvas: Canvas) {
    val startX = getScreenWidth() / 2f
    val startY = 0f
    val stopY = height / 1f
    canvas.drawLine(startX, startY, startX, stopY, paintCenterLine)
}


fun SeekVideoView.drawListThumbVideo(canvas: Canvas) {
    var totalWidthDraw = 0f
    listThumb.forEachIndexed { index, it ->
        if (totalWidthDraw < 2000) {
            if (it.xOffSetWithParentIncludeWidth > xPositionScroll) { // Chỉ vẽ những thằng có đuôi lớn vị trí đang scrollX
                // Có thể đầu nó bị thò sang bên trái nên di chuyển canvas về vẽ đầu thò
                if (it.xOffSetWithParent < xPositionScroll) {
                    val cutLeftDimen = -abs(xPositionScroll - it.xOffSetWithParent)
                    canvas.save()
                    canvas.translate(cutLeftDimen, 0f)
                    canvas.drawBitmap(it.getThumb(currentScale), 0f, 0f, paintListThumb)
                    totalWidthDraw += it.getDesiredWidth()
                    canvas.restore()
                } else {
                    canvas.save()
                    val translateNextItem = abs(xPositionScroll - it.xOffSetWithParent)
                    canvas.translate(translateNextItem, 0f)
                    canvas.drawBitmap(it.getThumb(currentScale), 0f, 0f, paintListThumb)
                    totalWidthDraw += it.getDesiredWidth()
                    canvas.restore()
                }
            }
        }
    }
}


fun SeekVideoView.drawViewSelected(canvas: Canvas) {
    anchorLeftThumbSelectedRect = null
    anchorRightThumbSelectedRect = null
    thumbMediaViewSelected ?: return

    val res: Resources = context.resources
    val arrowId: Int = R.drawable.ic_arrow
    val arrowLeft = BitmapFactory.decodeResource(res, arrowId)

    val bmpLeft = Bitmap.createBitmap(
        arrowLeft,
        0,
        0,
        60,
        MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR
    )

    val matrix = Matrix()
    matrix.postRotate(180f)
    val bmpRight = Bitmap.createBitmap(bmpLeft, 0, 0, bmpLeft.width, bmpLeft.height, matrix, true)

    val widthArrow = bmpLeft.width

    val xLeftArrowWillDraw =
        thumbMediaViewSelected!!.xOffSetWithParent - xPositionScroll - widthArrow
    val xRightArrowWillDraw =
        thumbMediaViewSelected!!.xOffSetWithParentIncludeWidth - xPositionScroll

    canvas.drawBitmap(bmpLeft, xLeftArrowWillDraw, 0f, paintListThumb)
    canvas.drawBitmap(bmpRight, xRightArrowWillDraw, 0f, paintListThumb)

    val leftArrowLeft = (thumbMediaViewSelected!!.xOffSetWithParent - widthArrow).toInt()
    val rightArrowLeft = (thumbMediaViewSelected!!.xOffSetWithParent).toInt()

    val leftArrowRight = (thumbMediaViewSelected!!.xOffSetWithParentIncludeWidth).toInt()
    val rightArrowRight =
        (thumbMediaViewSelected!!.xOffSetWithParentIncludeWidth + widthArrow).toInt()

    anchorLeftThumbSelectedRect = Rect(
        leftArrowLeft,
        0,
        rightArrowLeft,
        MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR
    )
    anchorRightThumbSelectedRect = Rect(
        leftArrowRight,
        0,
        rightArrowRight,
        MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR
    )
    postInvalidate()
}


fun SeekVideoView.drawTextTime(canvas: Canvas) {
    val bounds = Rect()
    val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
    val text = sdf.format(Date(1000))
    paintTextTime.getTextBounds(text, 0, text.length, bounds)

    val roundUpTimeUnitSecond = (getCurrentDurationInMillis / 1000).toInt() * 1000

    val diffMillis = roundUpTimeUnitSecond - getCurrentDurationInMillis

    val pixelRoundUpByTime = convertTimeToPixel(diffMillis, currentScale)

    val pixelRoundedTimed = getCurrentCenterInPixel + pixelRoundUpByTime

    val listPointDrawText = mutableListOf<Float>()

    // Tính Offset của tọa độ đó với ScrollX => Đoạn bị dôi ra bên tay phải
    val offsetFromXScrollToRoundedTime = abs(xPositionScroll - pixelRoundedTimed)
    listPointDrawText.add(offsetFromXScrollToRoundedTime)

    val pixelOfOneSecond = MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR * currentScale
    for (i in 1..4) {
        // Từ vị trí đó + 2 bên 1s
        listPointDrawText.add(offsetFromXScrollToRoundedTime + i * pixelOfOneSecond)
        listPointDrawText.add(offsetFromXScrollToRoundedTime - i * pixelOfOneSecond)
    }

    val metric: Paint.FontMetrics = paintTextTime.fontMetrics
    val textHeight = ceil(metric.descent - metric.ascent)
    val y = textHeight - metric.descent


    var lastPixel = -1f


    listPointDrawText.forEach { pixel ->
        val pixelReal = pixel + xPositionScroll
        if (pixelReal >= 0 && pixelReal <= totalWidthInPixel) {
            canvas.save()
            val text = sdf.format(Date(convertPixelToTime((pixelReal).roundToInt(), currentScale)))
            paintTextTime.getTextBounds(text, 0, text.length, bounds)
            canvas.translate(pixel, 0f)
            canvas.drawText(text, 0f, y, paintTextTime)
            canvas.restore()

            if (lastPixel != -1f && abs(pixel - lastPixel) >= 300) {
                Log.e("ThoNH","drawCircle")
                canvas.save()
                val offset = abs(pixel - lastPixel) / 2
                val position = if (pixel > lastPixel) lastPixel + offset else pixel + offset
                canvas.translate(position, 0f)
                canvas.drawCircle(0f, 0f, 12f, paintTextTime)
                canvas.restore()
            }

            lastPixel = pixel

        }
    }
}
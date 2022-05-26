package com.ht.videoseekerview.seek

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import com.ht.videoseekerview.seek.MediaConstant.DEFAULT_SCALE_FACTOR
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class SeekVideoView : View {

    var xPositionScroll = 0
    var currentScale = DEFAULT_SCALE_FACTOR

    private var flingAnimator = ValueAnimator.ofFloat(0f, 0f)

    val listThumb = mutableListOf<ThumbMediaView>()

    val totalWidthInPixel: Float
        get() {
            return listThumb.map { it.getDesiredWidth() }.sum()
        }

    var thumbMediaViewSelected: ThumbMediaView? = null
    var anchorLeftThumbSelectedRect: Rect? = null
    var anchorRightThumbSelectedRect: Rect? = null
    var isDragLeftAnchorToExpandThumb = false
    var isDragRightAnchorToExpandThumb = false

    val totalTimeInMillis: Long
        get() {
            return 1000 * (totalWidthInPixel / MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR / currentScale).roundToLong()
        }

    val getCurrentDurationInMillis: Long
        get() {
            return (1000 * ((xPositionScroll + measuredWidth / 2f) / MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR / currentScale)).roundToLong()
        }

    val getCurrentCenterInPixel: Int
        get() {
            return xPositionScroll + width / 2
        }

    private val mScaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {

                val totalWidth = totalWidthInPixel
                val centerPointers = detector.focusX
                val centerIncludeXScroll = xPositionScroll + centerPointers
                val oldRatio = centerIncludeXScroll / totalWidth

                currentScale *= detector.scaleFactor
                if (currentScale < MediaConstant.MIN_SCALE_FACTOR) {
                    currentScale = MediaConstant.MIN_SCALE_FACTOR
                }

                if (currentScale > MediaConstant.MAX_SCALE_FACTOR) {
                    currentScale = MediaConstant.MAX_SCALE_FACTOR
                }

                var offset = 0f
                listThumb.forEach {
                    it.currentScale = currentScale
                    it.dirty = true
                    it.xOffSetWithParent = offset
                    offset += it.getDesiredWidth()
                }


                val newWidth = totalWidthInPixel
                val newCenterXWithXScroll = oldRatio * newWidth
                val newXPositionScroll = newCenterXWithXScroll - centerPointers
                xPositionScroll = newXPositionScroll.toInt()

                invalidate()
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {

            }
        })


    val mDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.OnGestureListener {
            override fun onDown(p0: MotionEvent?): Boolean {
                checkTouchToDragExpandThumb(p0)
                Log.e("ThoNH", "onDown")
                flingAnimator.cancel()
                return true
            }

            override fun onShowPress(p0: MotionEvent?) {
                Log.e("ThoNH", "onShowPress")
            }

            override fun onSingleTapUp(p0: MotionEvent?): Boolean {
                thumbMediaViewSelected = checkClickToSelectThumb(p0)
                Log.e("ThoNH", "onSingleTapUp => ${p0?.action} => ${p0?.x} => ${p0?.y}")
                return false
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (isDragLeftAnchorToExpandThumb || isDragRightAnchorToExpandThumb) {

                    if (isDragRightAnchorToExpandThumb) {

                        if (listThumb.indexOf(thumbMediaViewSelected) == listThumb.lastIndex) {
                            return true
                        }


                        handleDragAnchorRightOverScreen.removeCallbacks(
                            runnableDragAnchorRightOverScreen
                        )
                        if (e2.x >= width - 150) {
                            handleDragAnchorRightOverScreen.postDelayed(
                                runnableDragAnchorRightOverScreen,
                                10
                            )
                        } else {
                            thumbMediaViewSelected?.appendTimeByPixel(-distanceX)
                        }
                    }

                    if (isDragLeftAnchorToExpandThumb) {

                        if (listThumb.indexOf(thumbMediaViewSelected) == 0) {
                            return true
                        }

                        handleDragAnchorLeftOverScreen.removeCallbacks(
                            runnableDragAnchorLeftOverScreen
                        )
                        if (e2.x <= 150) {
                            handleDragAnchorLeftOverScreen.postDelayed(
                                runnableDragAnchorLeftOverScreen,
                                10
                            )
                        } else {
                            if (thumbMediaViewSelected?.appendTimeByPixel(distanceX) == true) {
                                xPositionScroll += distanceX.toInt()
                            }
                        }
                    }


                    calculatePositionThumb()
                    invalidate()
                    return false
                }


                if (xPositionScroll < -halfScreenWidth()) {
                    xPositionScroll = -halfScreenWidth()
                }



                if (xPositionScroll == 0 && distanceX >= 0) {
                    xPositionScroll = 0
                } else {
                    xPositionScroll += distanceX.toInt()
                }

                if (xPositionScroll < -halfScreenWidth()) {
                    xPositionScroll = -halfScreenWidth()
                }

                val maxWidthThreshold = listThumb
                    .map { it.getDesiredWidth() }
                    .sum().roundToInt() - halfScreenWidth()


                if (xPositionScroll > maxWidthThreshold) {
                    xPositionScroll = maxWidthThreshold
                }

                invalidate()

                return false
            }

            override fun onLongPress(p0: MotionEvent?) {
                Log.e("ThoNH", "onLongPress")
            }

            override fun onFling(
                event1: MotionEvent,
                event2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                var lastRemain = 0

                flingAnimator = ValueAnimator.ofFloat(0f, velocityX)
                flingAnimator.duration = 1000 //in millis
                flingAnimator.interpolator = DecelerateInterpolator()
                flingAnimator.addUpdateListener {

                    val valueRemain = (it.animatedValue as Float).toInt()
                    val diff = valueRemain - lastRemain
                    xPositionScroll -= diff / 3


                    if (xPositionScroll < -halfScreenWidth()) {
                        xPositionScroll = -halfScreenWidth()
                        Log.e("ThoNH", "onScroll => : $1")
                        invalidate()
                        flingAnimator.cancel()
                        return@addUpdateListener
                    }

                    val maxWidthThreshold = listThumb
                        .map { it.getDesiredWidth() }
                        .sum().roundToInt() - halfScreenWidth()


                    if (xPositionScroll > maxWidthThreshold) {
                        xPositionScroll = maxWidthThreshold
                        Log.e("ThoNH", "onScroll => : $2")
                        invalidate()
                        flingAnimator.cancel()
                        return@addUpdateListener
                    }



                    invalidate()
                    lastRemain = valueRemain

                }
                flingAnimator.start()
                return false
            }
        })
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {

        var offset = 0f
        for (i in 0 until 10) {
            listThumb.add(
                ThumbMediaView(
                    context,
                    Uri.parse(""),
                    MediaType.Image,
                    currentScale,
                    1000
                ).apply {
                    xOffSetWithParent = offset
                    offset += getDesiredWidth()
                }
            )
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            handleDragAnchorRightOverScreen.removeCallbacks(runnableDragAnchorRightOverScreen)
            handleDragAnchorLeftOverScreen.removeCallbacks(runnableDragAnchorLeftOverScreen)
        }

        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            return true
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawListThumbVideo(canvas)
        drawViewSelected(canvas)
        drawCenterLine(canvas)
        drawTextTime(canvas)
    }


    val handleDragAnchorRightOverScreen = Handler()
    val runnableDragAnchorRightOverScreen = object : Runnable {
        override fun run() {
            val sampleDistanceX = 20
            xPositionScroll += sampleDistanceX
            thumbMediaViewSelected?.appendTimeByPixel((sampleDistanceX).toFloat())
            calculatePositionThumb()
            invalidate()
            handleDragAnchorRightOverScreen.postDelayed(this, 5)
        }
    }


    val handleDragAnchorLeftOverScreen = Handler()
    val runnableDragAnchorLeftOverScreen = object : Runnable {
        override fun run() {
            val sampleDistanceX = -20
            thumbMediaViewSelected?.appendTimeByPixel((-sampleDistanceX).toFloat())
            calculatePositionThumb()
            invalidate()
            handleDragAnchorLeftOverScreen.postDelayed(this, 5)
        }
    }
}

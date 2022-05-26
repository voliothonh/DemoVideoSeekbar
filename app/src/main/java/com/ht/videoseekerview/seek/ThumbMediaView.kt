package com.ht.videoseekerview.seek

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.math.roundToInt


class ThumbMediaView(
    context: Context,
    private val mediaPath: Uri,
    private val mediaType: MediaType,
    var currentScale: Float,
    var timeOfThumb: Long
) : LinearLayout(context) {

    var xOffSetWithParent = 0f

    val xOffSetWithParentIncludeWidth
        get() = xOffSetWithParent + getDesiredWidth()

    val getRectWithOffset: Rect
        get() {
            return Rect(
                xOffSetWithParent.toInt(),
                0,
                xOffSetWithParentIncludeWidth.toInt(),
                getDesiredHeight().toInt()
            )
        }


    var bitmapThumb: Bitmap? = null
    var dirty = false


    val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.CYAN)

    var color: Int

    init {
        color = colors.random()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(getDesiredWidth().toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(getDesiredHeight().toInt(), MeasureSpec.EXACTLY)
        )
    }

    fun getDesiredWidth(): Float {
        return (MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR * currentScale * timeOfThumb / 1000)
    }

    fun getDesiredHeight(): Float {
        return MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR * 1f
    }

    fun getThumb(scale: Float): Bitmap {
        if (bitmapThumb == null || dirty) {
            dirty = false

            val numberImage: Int = (getDesiredWidth() / getDesiredHeight()).toInt()
            for (i in 0..numberImage) {
                val imv = ImageView(context)
                if (mediaType == MediaType.Image) {
                    Glide.with(context)
                        .load(mediaPath)
                        .override(
                            MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR,
                            MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR
                        )
                        .into(imv)
                } else {

//                Glide.with(context)
//                    .load(mediaPath)
//                    .override(
//                        MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR,
//                        MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR
//                    )
//                    .apply(RequestOptions()).frame()
//                    .into(imv)
                }
            }
            val bitmap =
                Bitmap.createBitmap(
                    getDesiredWidth().roundToInt(),
                    getDesiredHeight().roundToInt(),
                    Bitmap.Config.ARGB_8888
                )
            val canvas = Canvas(bitmap)


            canvas.drawColor(color)

            bitmapThumb = bitmap
        }

        return bitmapThumb!!
    }

    fun getListBitmapForThumb(): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        when (mediaType) {
            MediaType.Image -> {
            }
            MediaType.Video -> {

                val microSecond = 6000000 // 6th second as an example

                val options = RequestOptions().frame(microSecond.toLong()).override(50, 50)

//                Glide.with(context)
//                    .asBitmap()
//                    .load(videoUri)
//                    .apply(options)
//                    .into<Target<Bitmap>>(yourImageView)

            }
        }
        return bitmaps
    }


    /**
     * Thay đổi thời gian cho thumbnail này bằng cách quy đổi pixel ra thời gian
     * Trả về true nếu thay đổi thành công
     * Trả về false nếu thay đổi thất bại
     * Thất bại khi nó bé hơn ngưỡng min
     */
    fun appendTimeByPixel(pixel: Float): Boolean {
        val timeAdding =
            (pixel * 1000 / MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR / currentScale).toLong()

        if (timeOfThumb + timeAdding < 500) {
            return false
        }
        timeOfThumb += (pixel * 1000 / MediaConstant.PIXEL_PER_SECOND_IN_DEFAULT_SCALE_FACTOR / currentScale).toLong()
        return true
    }
}
package com.example.macrominder.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.macrominder.R

class CircularProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0
    private var max = 100
    private val strokeWidth = 20f
    private val startAngle = -90f
    private var isReversed = false

    private val backgroundPaint = Paint().apply {
        color = context.getColor(R.color.dark_green)
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressBar.strokeWidth
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = context.getColor(R.color.cyan)
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressBar.strokeWidth
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = true
    }


    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - strokeWidth / 2f


        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)


        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        val sweepAngle = (progress.toFloat() / max) * 360
        val adjustedSweepAngle = if (isReversed) -sweepAngle else sweepAngle


        if (isReversed) {

            canvas.drawArc(rectF, startAngle, 360f, false, progressPaint)
            canvas.drawArc(rectF, startAngle, adjustedSweepAngle, false, backgroundPaint)
        } else {

            canvas.drawArc(rectF, startAngle, adjustedSweepAngle, false, progressPaint)
        }
    }


    fun setProgress(value: Int) {
        progress = value.coerceIn(0, max)
        invalidate()
    }

    fun setMax(value: Int) {
        max = value.coerceAtLeast(1)
    }

    fun setReversed(reversed: Boolean) {
        isReversed = reversed
    }
}

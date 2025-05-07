package com.example.macrominder.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularProgressPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 50f
        style = Paint.Style.FILL
    }
    private val rectF: RectF = RectF(0f, 0f, 0f, 0f)

    private val defaultColors = arrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.WHITE, Color.DKGRAY, Color.LTGRAY
    )
    private var values = mutableListOf<Float>()
    private var totalValue = 0f


    val colors: List<Int>
        get() = defaultColors.toList()

    fun setData(data: Map<String, Float>) {
        values.clear()
        totalValue = 0f


        data.values.forEach { value ->
            totalValue += value
        }


        values.addAll(data.values)
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var startAngle = -90f
        val sweepAngle = 360f

        values.forEachIndexed { index, value ->
            paint.color = defaultColors[index % defaultColors.size]
            val angle = (value / totalValue) * sweepAngle
            canvas.drawArc(rectF, startAngle, angle, true, paint)
            startAngle += angle
        }
    }
}

package com.example.orangetracker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class TrafficChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var inSeries: IntArray = intArrayOf()
    private var outSeries: IntArray = intArrayOf()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33000000")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val inPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#35d07f")
        strokeWidth = 3.5f
        style = Paint.Style.STROKE
    }

    private val outPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4f8cff")
        strokeWidth = 3.5f
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setSeries(inSeries: IntArray, outSeries: IntArray) {
        this.inSeries = inSeries
        this.outSeries = outSeries
        invalidate()
    }

    private fun maxValue(arr: IntArray): Int {
        var m = 0
        for (v in arr) {
            if (v > m) m = v
        }
        return m
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val n = max(inSeries.size, outSeries.size)
        if (n <= 1) {
            // Nothing to draw yet.
            return
        }

        val paddingLeft = 28f
        val paddingRight = 12f
        val paddingTop = 12f
        val paddingBottom = 24f

        val plotW = (w - paddingLeft - paddingRight).coerceAtLeast(1f)
        val plotH = (h - paddingTop - paddingBottom).coerceAtLeast(1f)

        val maxY = max(1, max(maxValue(inSeries), maxValue(outSeries))).toFloat()

        // Grid
        val horizontalLines = 4
        for (i in 0..horizontalLines) {
            val t = i / horizontalLines.toFloat()
            val y = paddingTop + plotH * t
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint)
        }

        // Vertical grid (per point)
        val verticalCount = n - 1
        for (i in 0..verticalCount) {
            val x = paddingLeft + plotW * (i / verticalCount.toFloat())
            canvas.drawLine(x, paddingTop, x, paddingTop + plotH, gridPaint)
        }

        fun xAt(i: Int): Float = paddingLeft + plotW * (i / verticalCount.toFloat())
        fun yAt(value: Int): Float {
            val t = value / maxY
            return paddingTop + plotH * (1f - t)
        }

        // Lines
        val pathIn = Path()
        val pathOut = Path()
        for (i in 0 until n) {
            val vIn = inSeries.getOrNull(i) ?: 0
            val vOut = outSeries.getOrNull(i) ?: 0
            val x = xAt(i)
            val yIn = yAt(vIn)
            val yOut = yAt(vOut)
            if (i == 0) {
                pathIn.moveTo(x, yIn)
                pathOut.moveTo(x, yOut)
            } else {
                pathIn.lineTo(x, yIn)
                pathOut.lineTo(x, yOut)
            }
        }
        canvas.drawPath(pathIn, inPaint)
        canvas.drawPath(pathOut, outPaint)

        // Points
        for (i in 0 until n) {
            val vIn = inSeries.getOrNull(i) ?: 0
            val vOut = outSeries.getOrNull(i) ?: 0
            val x = xAt(i)
            val yIn = yAt(vIn)
            val yOut = yAt(vOut)

            pointPaint.color = inPaint.color
            canvas.drawCircle(x, yIn, 4f, pointPaint)

            pointPaint.color = outPaint.color
            canvas.drawCircle(x, yOut, 4f, pointPaint)
        }
    }
}


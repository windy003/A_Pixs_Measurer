package com.example.pixelmeasure

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class MeasureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 主红色线条画笔
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    // 虚线引导线画笔（浅红色）
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(100, 255, 0, 0)
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    // 端点圆点画笔
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    // 标签文字画笔
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // 标签背景画笔
    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 255, 255, 255)
        style = Paint.Style.FILL
    }

    // 箭头画笔
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }

    // 提示文字画笔
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 100, 100, 100)
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var isTouching = false

    // 阈值：水平移动大于垂直移动时吸附为水平线，否则为垂直线
    private val snapThreshold = 15f

    // 已完成的测量结果，保留在屏幕上
    data class Measurement(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val pixels: Int,
        val isHorizontal: Boolean
    )

    private val measurements = mutableListOf<Measurement>()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                currentX = event.x
                currentY = event.y
                isTouching = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                isTouching = false
                val dx = abs(currentX - startX)
                val dy = abs(currentY - startY)
                // 仅在移动距离有意义时保存
                if (dx > snapThreshold || dy > snapThreshold) {
                    val isHorizontal = dx >= dy
                    val endX: Float
                    val endY: Float
                    val pixels: Int
                    if (isHorizontal) {
                        endX = currentX
                        endY = startY
                        pixels = dx.toInt()
                    } else {
                        endX = startX
                        endY = currentY
                        pixels = dy.toInt()
                    }
                    measurements.add(Measurement(startX, startY, endX, endY, pixels, isHorizontal))
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun clearMeasurements() {
        measurements.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 无测量记录且未触摸时绘制提示
        if (measurements.isEmpty() && !isTouching) {
            canvas.drawText(
                "Drag your finger to measure pixels",
                width / 2f, height / 2f - 30f, hintPaint
            )
            canvas.drawText(
                "Horizontal or Vertical",
                width / 2f, height / 2f + 30f, hintPaint
            )
            return
        }

        // 绘制所有已保存的测量
        for (m in measurements) {
            drawMeasurement(canvas, m.x1, m.y1, m.x2, m.y2, m.pixels, m.isHorizontal)
        }

        // 绘制当前正在进行的测量
        if (isTouching) {
            val dx = abs(currentX - startX)
            val dy = abs(currentY - startY)

            if (dx > snapThreshold || dy > snapThreshold) {
                val isHorizontal = dx >= dy
                val endX: Float
                val endY: Float
                val pixels: Int

                if (isHorizontal) {
                    endX = currentX
                    endY = startY
                    pixels = dx.toInt()
                    // 绘制从手指到吸附线的引导线
                    canvas.drawLine(currentX, currentY, currentX, startY, guidePaint)
                } else {
                    endX = startX
                    endY = currentY
                    pixels = dy.toInt()
                    // 绘制从手指到吸附线的引导线
                    canvas.drawLine(currentX, currentY, startX, currentY, guidePaint)
                }

                drawMeasurement(canvas, startX, startY, endX, endY, pixels, isHorizontal)
            }
        }
    }

    private fun drawMeasurement(
        canvas: Canvas,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        pixels: Int,
        isHorizontal: Boolean
    ) {
        // 绘制主红色线条
        canvas.drawLine(x1, y1, x2, y2, linePaint)

        // 绘制端点圆点
        val dotRadius = 8f
        canvas.drawCircle(x1, y1, dotRadius, dotPaint)
        canvas.drawCircle(x2, y2, dotRadius, dotPaint)

        // 绘制箭头
        drawArrows(canvas, x1, y1, x2, y2, isHorizontal)

        // 绘制垂直端帽
        val capLen = 16f
        if (isHorizontal) {
            canvas.drawLine(x1, y1 - capLen, x1, y1 + capLen, linePaint)
            canvas.drawLine(x2, y2 - capLen, x2, y2 + capLen, linePaint)
        } else {
            canvas.drawLine(x1 - capLen, y1, x1 + capLen, y1, linePaint)
            canvas.drawLine(x2 - capLen, y2, x2 + capLen, y2, linePaint)
        }

        // 绘制带背景的标签
        val label = "${pixels} px"
        val textWidth = textPaint.measureText(label)
        val textX: Float
        val textY: Float
        val padding = 12f

        if (isHorizontal) {
            textX = (x1 + x2) / 2f
            textY = y1 - 30f
        } else {
            textX = x1 + 30f + textWidth / 2f
            textY = (y1 + y2) / 2f
        }

        // 绘制文字后面的白色背景
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(label, 0, label.length, textBounds)
        canvas.drawRoundRect(
            textX - textWidth / 2 - padding,
            textY - textBounds.height() - padding,
            textX + textWidth / 2 + padding,
            textY + padding,
            8f, 8f,
            textBgPaint
        )

        canvas.drawText(label, textX, textY, textPaint)
    }

    private fun drawArrows(
        canvas: Canvas,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        isHorizontal: Boolean
    ) {
        val arrowSize = 20f
        val path = Path()

        if (isHorizontal) {
            val dir = if (x2 > x1) 1f else -1f
            // 终点箭头
            path.moveTo(x2, y2)
            path.lineTo(x2 - dir * arrowSize, y2 - arrowSize / 2)
            path.lineTo(x2 - dir * arrowSize, y2 + arrowSize / 2)
            path.close()
            canvas.drawPath(path, arrowPaint)

            // 起点箭头
            path.reset()
            path.moveTo(x1, y1)
            path.lineTo(x1 + dir * arrowSize, y1 - arrowSize / 2)
            path.lineTo(x1 + dir * arrowSize, y1 + arrowSize / 2)
            path.close()
            canvas.drawPath(path, arrowPaint)
        } else {
            val dir = if (y2 > y1) 1f else -1f
            // 终点箭头
            path.moveTo(x2, y2)
            path.lineTo(x2 - arrowSize / 2, y2 - dir * arrowSize)
            path.lineTo(x2 + arrowSize / 2, y2 - dir * arrowSize)
            path.close()
            canvas.drawPath(path, arrowPaint)

            // 起点箭头
            path.reset()
            path.moveTo(x1, y1)
            path.lineTo(x1 - arrowSize / 2, y1 + dir * arrowSize)
            path.lineTo(x1 + arrowSize / 2, y1 + dir * arrowSize)
            path.close()
            canvas.drawPath(path, arrowPaint)
        }
    }
}

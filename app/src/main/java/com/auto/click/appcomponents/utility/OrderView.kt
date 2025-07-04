package com.auto.click.appcomponents.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrderView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var circleRadius1 = 20
    private var circleRadius2 = 20
    private var circleStrokeWidth = 10
    private var outlineCircleRadius = 2

    // Biến lưu trữ tọa độ các điểm của đường nối
    var centerX1 = 0
    var centerY1 = 0
    var centerX2 = 0
    var centerY2 = 0

    private var statusBarHeight: Int = getStatusBarHeight(context)

    private var isDraggingCircle1 = false
    private var isDraggingCircle2 = false
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var paintBackground: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00000000")
    }
    private var paintCircle1: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00000000")
    }
    private var paintCircle2: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99c4c4c4")
        strokeWidth = convertToPixels(circleStrokeWidth.toFloat()).toFloat()
        style = Paint.Style.FILL
    }
    private var paintCircleOutline: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#000000")
        style = Paint.Style.FILL
    }

    private fun convertToPixels(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", context.packageName)
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Vẽ các hình tròn và đường nối
        canvas.drawCircle(centerX1.toFloat(), centerY1.toFloat(), convertToPixels(circleRadius1.toFloat()).toFloat(), paintBackground)
        canvas.drawCircle(centerX2.toFloat(), centerY2.toFloat(), convertToPixels(circleRadius2.toFloat()).toFloat(), paintCircle1)
        canvas.drawCircle(centerX2.toFloat(), centerY2.toFloat(), convertToPixels(outlineCircleRadius.toFloat()).toFloat(), paintCircleOutline)
        canvas.drawLine(centerX1.toFloat(), centerY1.toFloat(), centerX2.toFloat(), centerY2.toFloat(), paintCircle2)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    @SuppressLint("AutoClickService")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt() - statusBarHeight
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTouchY = event.y
                checkIfTouchingCircle(x, y)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingCircle1) {
                    updateCirclePosition(x, y, true)
                } else if (isDraggingCircle2) {
                    updateCirclePosition(x, y, false)
                }
                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                isDraggingCircle1 = false
                isDraggingCircle2 = false
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun checkIfTouchingCircle(x: Int, y: Int) {
        val radius1 = convertToPixels(circleRadius1.toFloat())
        val radius2 = convertToPixels(circleRadius2.toFloat())
        if (Math.hypot((x - centerX1).toDouble(), (y - centerY1).toDouble()) <= radius1) {
            isDraggingCircle1 = true
        } else if (Math.hypot((x - centerX2).toDouble(), (y - centerY2).toDouble()) <= radius2) {
            isDraggingCircle2 = true
        }
    }

    fun updateCirclePosition(x: Int, y: Int, isCircle1: Boolean) {
        if (isCircle1) {
            centerX1 = x
            centerY1 = y
        } else {
            centerX2 = x
            centerY2 = y
        }
        invalidate()
    }

    // Phương thức để cập nhật tọa độ của các điểm
    fun setLinePosition(startX: Int, startY: Int, endX: Int, endY: Int) {
        centerX1 = startX
        centerY1 = startY
        centerX2 = endX
        centerY2 = endY
//        invalidate() // Vẽ lại view với các tọa độ mới
    }
}

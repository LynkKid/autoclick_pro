package com.auto.click.appcomponents.utility

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.auto.click.appcomponents.utility.Utils.dp2px
import kotlin.math.pow

class TouchAndDragListener(
    private val params: WindowManager.LayoutParams,
    private val startDragDistance: Int = 10,
    private val onTouch: Action?,
    private val onDrag: Action?,
    private val isButtonStart: Boolean? = false,
    private val isSwiping: Boolean? = false
) : View.OnTouchListener {
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0.toFloat()
    private var initialTouchY: Float = 0.toFloat()
    private var isDrag = false

    private fun isDragging(event: MotionEvent): Boolean =
        (((event.rawX - initialTouchX).toDouble().pow(2.0)
                + (event.rawY - initialTouchY).toDouble().pow(2.0))
                > startDragDistance * startDragDistance)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isDrag = false
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                if (isButtonStart!!) {
                    onTouch?.invoke()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDrag && isDragging(event)) {
                    isDrag = true
                }
                if (!isDrag) return true
                val displayMetrics = v.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                if (isSwiping!!) {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    val y = initialY + (event.rawY - initialTouchY).toInt()
                    if (y >= screenHeight - v.context.dp2px(40f)) {
                        params.y = screenHeight - v.context.dp2px(40f)
                    } else {
                        params.y = y
                    }
                } else {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                }
                onDrag?.invoke()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDrag && !isButtonStart!!) {
                    onTouch?.invoke()
                    v.performClick()
                    return true
                }
            }
        }
        return false
    }
}
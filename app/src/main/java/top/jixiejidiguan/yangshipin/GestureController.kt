package top.jixiejidiguan.yangshipin

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * 手势控制器 - 处理上下滑动和点击事件
 *
 * @param context 上下文
 * @param view 需要添加手势控制的视图
 * @param listener 手势事件监听器
 */
class GestureController(
    context: Context,
    view: View,
    private val listener: GestureListener
) {

    /**
     * 手势事件监听器接口
     */
    interface GestureListener {
        /**
         * 点击事件回调
         */
        fun onSingleTap()

        /**
         * 向下滑动事件回调
         */
        fun onSwipeDown()

        /**
         * 向上滑动事件回调
         */
        fun onSwipeUp()
    }

    // 手势检测器
    private val gestureDetector: GestureDetector

    // 滑动阈值（像素）
    @Suppress("PrivatePropertyName")
    private val SWIPE_THRESHOLD = 100
    // 滑动速度阈值（像素/秒）
    @Suppress("PrivatePropertyName")
    private val SWIPE_VELOCITY_THRESHOLD = 100

    init {
        // 初始化手势检测器
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            // 单击事件
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                listener.onSingleTap()
                return true
            }

            // 滑动事件
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                try {
                    // 计算Y轴方向的滑动距离
                    val diffY = e2.y - (e1?.y ?: 0f)
                    
                    // 检查是否符合滑动阈值和速度阈值
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // 向下滑动
                            listener.onSwipeDown()
                        } else {
                            // 向上滑动
                            listener.onSwipeUp()
                        }
                        return true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return false
            }
        })

        // 将手势检测器附加到视图
        view.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            // 确保可访问性，当检测到点击时调用performClick
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                v.performClick()
            }
            handled
        }
    }
}

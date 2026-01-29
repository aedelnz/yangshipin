package top.jixiejidiguan.yangshipin

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class GestureController(
    private val activity: MainActivity
) {
    
    val gestureDetector: GestureDetector by lazy {
        GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null) {
                    val deltaX = abs(e2.x - e1.x)
                    val deltaY = abs(e2.y - e1.y)
                    
                    // 优先检测左右滑动手势（水平方向）
                    if (deltaX > deltaY) {
                        // 右滑手势：从左向右滑动，显示侧边栏
                        if (e2.x - e1.x > 100 && abs(velocityX) > 100) {
                            activity.showSidebar()
                            return true
                        }
                        // 左滑手势：从右向左滑动，隐藏侧边栏
                        if (e1.x - e2.x > 100 && abs(velocityX) > 100) {
                            activity.hideSidebar()
                            return true
                        }
                    } else {
                        // 只有当侧边栏不可见时，才执行上下滑动切换频道的逻辑
                        if (activity.channelSidebar.visibility != View.VISIBLE) {
                            // 上下滑动手势（垂直方向），切换频道
                            val handled = activity.handleVerticalSwipe(e1, e2, velocityY)
                            if (handled) {
                                return true
                            }
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
    }
}

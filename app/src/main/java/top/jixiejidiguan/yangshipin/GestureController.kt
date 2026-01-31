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
                e1 ?: return super.onFling(e1, e2, velocityX, velocityY)
                
                val deltaX = abs(e2.x - e1.x)
                val deltaY = abs(e2.y - e1.y)
                
                if (deltaX > deltaY) {
                    // 水平滑动
                    if (e2.x - e1.x > 100 && abs(velocityX) > 100) {
                        activity.showSidebar()
                        return true
                    }
                    if (e1.x - e2.x > 100 && abs(velocityX) > 100) {
                        activity.hideSidebar()
                        return true
                    }
                } else if (activity.channelSidebar.visibility != View.VISIBLE) {
                    
                    if (abs(deltaY) > 100 && abs(velocityY) > 100) {
                        val channelAdapter = activity.channelList.adapter as? ChannelAdapter ?: return false
                        val channelCount = activity.channels.size
                        activity.currentChannelPosition = when {
                            deltaY > 0 -> if (activity.isReverseSwitching) {
                                (activity.currentChannelPosition - 1 + channelCount) % channelCount
                            } else {
                                (activity.currentChannelPosition + 1) % channelCount
                            }
                            else -> if (activity.isReverseSwitching) {
                                (activity.currentChannelPosition + 1) % channelCount
                            } else {
                                (activity.currentChannelPosition - 1 + channelCount) % channelCount
                            }
                        }

                        val currentChannel = activity.channels[activity.currentChannelPosition]
                        channelAdapter.setSelectedPosition(activity.currentChannelPosition)
                        activity.showChannelCard(currentChannel)
                        
                        val url = AppConfig.getChannelData()[currentChannel] ?: return true
                        activity.loadUrl(url)
                        return true
                    }
                }
                
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            /**
             * 当用户在屏幕上快速双击时触发
             *
             * @param e 触摸事件对象，包含坐标等信息
             * @return 返回 true 表示该双击事件已被成功处理
             */
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 逻辑：通过判断透明度 (Alpha) 来检测“反向频道切换设置面板”当前是否显示
                // 1f 表示完全显示（不透明）
                if (activity.cardReverse.alpha == 1f) {
                    // 如果面板当前是显示的，则执行隐藏操作
                    activity.hideReverseCard()
                } else {
                    // 如果面板当前是隐藏的（alpha 通常为 0），则执行显示操作
                    activity.showReverseCard()
                }
                return true
            }
        })
    }
}

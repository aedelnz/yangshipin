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
                    // 垂直滑动且侧边栏不可见时切换频道
                    val deltaY = e2.y - e1.y
                    
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
                        
                        // 保存值到SharedPreferences
                        activity.saveInt("current_channel", activity.currentChannelPosition)
                        
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
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 双击打开/关闭反向频道切换设置面板
                if (activity.cardReverse.alpha == 1f) {
                    activity.hideReverseCard()
                } else {
                    activity.showReverseCard()
                }
                return true
            }
        })
    }
}

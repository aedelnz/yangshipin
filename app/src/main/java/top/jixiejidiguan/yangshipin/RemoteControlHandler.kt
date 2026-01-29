package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.util.Log
import android.view.KeyEvent
import android.view.View

private const val TAG = "RemoteControlHandler"

/**
 * 遥控器控制器
 * 处理遥控器按键事件，实现频道切换、侧边栏显示/隐藏等功能
 * 
 * @param activity MainActivity 实例，用于访问和控制界面元素
 */
class RemoteControlHandler(
    private val activity: MainActivity
) {
    
    /**
     * 处理按键事件
     * 
     * @param event KeyEvent 实例，包含按键信息
     * @return Boolean 返回 true 表示事件已处理，false 表示事件未处理
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "handleKeyEvent 被调用: action=${event.action}, keyCode=${event.keyCode}")
        // 只处理按键按下事件，忽略按键抬起事件
        if (event.action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "处理 ACTION_DOWN 事件: keyCode=${event.keyCode}")
            when (event.keyCode) {
                // 上方向键
                KeyEvent.KEYCODE_DPAD_UP -> {
                    Log.d(TAG, "处理上方向键事件")
                    return handleDpadUp()
                }
                // 下方向键
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    Log.d(TAG, "处理下方向键事件")
                    return handleDpadDown()
                }
                // 确认键（包括多种确认键类型）
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    Log.d(TAG, "处理确认键事件 (DPAD_CENTER)")
                    return handleConfirm()
                }
                KeyEvent.KEYCODE_ENTER -> {
                    Log.d(TAG, "处理确认键事件 (ENTER)")
                    return handleConfirm()
                }
                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    Log.d(TAG, "处理确认键事件 (NUMPAD_ENTER)")
                    return handleConfirm()
                }
                // 返回键
                KeyEvent.KEYCODE_BACK -> {
                    Log.d(TAG, "处理返回键事件")
                    return handleBack()
                }
                else -> {
                    Log.d(TAG, "未处理的按键事件: keyCode=${event.keyCode}")
                }
            }
        } else {
            Log.d(TAG, "忽略非 ACTION_DOWN 事件: action=${event.action}")
        }
        // 事件未处理
        return false
    }
    
    /**
     * 处理上方向键事件
     * 
     * @return Boolean 返回 true 表示事件已处理，false 表示事件未处理
     */
    @SuppressLint("UseKtx")
    private fun handleDpadUp(): Boolean {
        if (activity.channelSidebar.visibility == View.VISIBLE) {
            // 侧边栏显示时，上下键用于预选频道
            Log.d(TAG, "侧边栏显示，上键预选频道")
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter
            if (channelAdapter != null) {
                val currentPosition = getCurrentSelectedPosition(channelAdapter)
                val channelCount = activity.channels.size
                // 实现无限循环：当到达第一个频道时，循环到最后一个
                val newPosition = if (currentPosition > 0) {
                    currentPosition - 1
                } else {
                    channelCount - 1 // 循环到最后一个频道
                }
                Log.d(TAG, "预选频道：从位置 $currentPosition 移动到 $newPosition")
                // 更新选中位置
                channelAdapter.setSelectedPosition(newPosition)
                // 平滑滚动到新位置
                activity.channelList.smoothScrollToPosition(newPosition)
                // 重置自动隐藏计时器，确保用户操作时不隐藏
                resetSidebarHideTimer()
                return true
            }
        } else {
            // 侧边栏隐藏时，上下键用于切换频道
            Log.d(TAG, "侧边栏隐藏，上键切换频道")
            val channelCount = activity.channels.size
            // 实现无限循环：当到达第一个频道时，循环到最后一个
            val newPosition = if (activity.currentChannelPosition > 0) {
                activity.currentChannelPosition - 1
            } else {
                channelCount - 1 // 循环到最后一个频道
            }
            val oldPosition = activity.currentChannelPosition
            // 更新当前频道位置
            activity.currentChannelPosition = newPosition
            Log.d(TAG, "切换频道：从位置 $oldPosition 到 $newPosition")
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter
            // 更新适配器中的选中位置
            channelAdapter?.setSelectedPosition(activity.currentChannelPosition)
            // 获取当前频道名称
            val currentChannel = activity.channels[activity.currentChannelPosition]
            // 显示频道卡片
            activity.showChannelCard(currentChannel)
            // 加载新频道的 URL
            val url = AppConfig.getChannelData()[currentChannel] ?: return true
            Log.d(TAG, "加载频道 URL: $url")
            activity.loadUrl(url)
            return true
        }
        return false
    }
    
    /**
     * 处理下方向键事件
     * 
     * @return Boolean 返回 true 表示事件已处理，false 表示事件未处理
     */
    @SuppressLint("UseKtx")
    private fun handleDpadDown(): Boolean {
        if (activity.channelSidebar.visibility == View.VISIBLE) {
            // 侧边栏显示时，上下键用于预选频道
            Log.d(TAG, "侧边栏显示，下键预选频道")
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter
            if (channelAdapter != null) {
                val currentPosition = getCurrentSelectedPosition(channelAdapter)
                val channelCount = activity.channels.size
                // 实现无限循环：当到达最后一个频道时，循环到第一个
                val newPosition = if (currentPosition < channelCount - 1) {
                    currentPosition + 1
                } else {
                    0 // 循环到第一个频道
                }
                Log.d(TAG, "预选频道：从位置 $currentPosition 移动到 $newPosition")
                // 更新选中位置
                channelAdapter.setSelectedPosition(newPosition)
                // 平滑滚动到新位置
                activity.channelList.smoothScrollToPosition(newPosition)
                // 重置自动隐藏计时器，确保用户操作时不隐藏
                resetSidebarHideTimer()
                return true
            }
        } else {
            // 侧边栏隐藏时，上下键用于切换频道
            Log.d(TAG, "侧边栏隐藏，下键切换频道")
            val channelCount = activity.channels.size
            // 实现无限循环：当到达最后一个频道时，循环到第一个
            val newPosition = if (activity.currentChannelPosition < channelCount - 1) {
                activity.currentChannelPosition + 1
            } else {
                0 // 循环到第一个频道
            }
            val oldPosition = activity.currentChannelPosition
            // 更新当前频道位置
            activity.currentChannelPosition = newPosition
            Log.d(TAG, "切换频道：从位置 $oldPosition 到 $newPosition")
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter
            // 更新适配器中的选中位置
            channelAdapter?.setSelectedPosition(activity.currentChannelPosition)
            // 获取当前频道名称
            val currentChannel = activity.channels[activity.currentChannelPosition]
            // 显示频道卡片
            activity.showChannelCard(currentChannel)
            // 加载新频道的 URL
            val url = AppConfig.getChannelData()[currentChannel] ?: return true
            Log.d(TAG, "加载频道 URL: $url")
            activity.loadUrl(url)
            return true
        }
        return false
    }
    
    /**
     * 重置侧边栏的自动隐藏计时器
     * 当用户操作时调用，确保侧边栏不会在用户操作过程中隐藏
     */
    private fun resetSidebarHideTimer() {
        Log.d(TAG, "重置侧边栏自动隐藏计时器")
        // 取消之前的隐藏任务
        activity.hideSidebarHandler?.removeCallbacksAndMessages(null)
        // 重新设置自动隐藏计时器
        activity.hideSidebarHandler?.postDelayed({
            activity.hideSidebar()
        }, 3000)
    }
    
    /**
     * 处理确认键事件
     * 
     * @return Boolean 返回 true 表示事件已处理，false 表示事件未处理
     */
    private fun handleConfirm(): Boolean {
        if (activity.channelSidebar.visibility != View.VISIBLE) {
            // 侧边栏隐藏时，确认键用于显示侧边栏
            Log.d(TAG, "侧边栏隐藏，确认键显示侧边栏")
            activity.showSidebar()
            return true
        } else {
            // 侧边栏显示时，确认键用于确认当前选中的频道
            Log.d(TAG, "侧边栏显示，确认键确认选中频道")
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter
            if (channelAdapter != null) {
                val currentPosition = getCurrentSelectedPosition(channelAdapter)
                // 确保位置在有效范围内
                if (currentPosition >= 0 && currentPosition < activity.channels.size) {
                    // 获取选中的频道名称
                    val selectedChannel = activity.channels[currentPosition]
                    Log.d(TAG, "确认选中频道：$selectedChannel (位置: $currentPosition)")
                    // 更新当前频道位置
                    activity.currentChannelPosition = currentPosition
                    // 显示频道卡片
                    activity.showChannelCard(selectedChannel)
                    // 加载频道对应的 URL
                    val url = AppConfig.getChannelData()[selectedChannel] ?: return true
                    Log.d(TAG, "加载频道 URL: $url")
                    activity.loadUrl(url)
                    // 更新适配器中的选中位置
                    channelAdapter.setSelectedPosition(currentPosition)
                    // 确认后隐藏侧边栏
                    Log.d(TAG, "确认后隐藏侧边栏")
                    activity.hideSidebar()
                    return true
                } else {
                    Log.d(TAG, "无效的频道位置: $currentPosition")
                }
            }
        }
        return false
    }
    
    /**
     * 处理返回键事件
     * 
     * @return Boolean 返回 true 表示事件已处理，false 表示事件未处理
     */
    @SuppressLint("UseKtx")
    private fun handleBack(): Boolean {
        if (activity.channelSidebar.visibility == View.VISIBLE) {
            // 侧边栏显示时，返回键用于隐藏侧边栏
            Log.d(TAG, "侧边栏显示，返回键隐藏侧边栏")
            activity.hideSidebar()
            return true
        }
        Log.d(TAG, "侧边栏隐藏，返回键事件未处理")
        return false
    }
    
    /**
     * 获取当前选中的频道位置
     * 
     * @param adapter ChannelAdapter 实例
     * @return Int 当前选中的位置索引，默认返回 0
     */
    private fun getCurrentSelectedPosition(adapter: ChannelAdapter): Int {
        // 通过反射获取 ChannelAdapter 中的 selectedPosition 字段
        try {
            val field = adapter.javaClass.getDeclaredField("selectedPosition")
            // 设置字段为可访问
            field.isAccessible = true
            // 返回字段值
            return field.getInt(adapter)
        } catch (e: Exception) {
            // 发生异常时打印堆栈信息
            e.printStackTrace()
        }
        // 发生异常时返回默认值 0
        return 0
    }
}

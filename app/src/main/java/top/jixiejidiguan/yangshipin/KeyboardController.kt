package top.jixiejidiguan.yangshipin

import android.view.KeyEvent
import android.view.View

/**
 * 键盘/遥控器控制器
 * 处理遥控器方向键、确认键、数字键和返回键
 */
class KeyboardController(
    private val view: View,
    private val onChannelUp: () -> Unit,
    private val onChannelDown: () -> Unit,
    private val onEnterKey: () -> Unit,
    private val onNumberKey: (Int) -> Unit = {}
) {
    
    init {
        // 设置键盘事件监听
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                handleKeyEvent(keyCode)
            } else {
                // 对于返回键，我们需要在 ACTION_UP 时拦截，防止应用直接退出
                keyCode == KeyEvent.KEYCODE_BACK
            }
        }
        
        // 关键：确保视图能获取焦点并保持焦点
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }
    
    /**
     * 处理键盘事件
     */
    private fun handleKeyEvent(keyCode: Int): Boolean {
        return when (keyCode) {
            // 遥控器上/左键 - 切换到上一个频道
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_PAGE_UP -> {
                onChannelUp.invoke()
                true
            }
            // 遥控器下/右键 - 切换到下一个频道
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_PAGE_DOWN -> {
                onChannelDown.invoke()
                true
            }
            // 遥控器确认键/中心键 - 切换全屏或显示UI
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> {
                onEnterKey.invoke()
                true
            }
            // 遥控器返回键 - 用于呼出/隐藏UI，而不是直接退出应用
            KeyEvent.KEYCODE_BACK -> {
                onEnterKey.invoke()
                true
            }
            // 数字键处理 (0-9)
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                onNumberKey(keyCode - KeyEvent.KEYCODE_0)
                true
            }
            // 电视遥控器特有的频道增减键
            KeyEvent.KEYCODE_CHANNEL_UP -> {
                onChannelUp.invoke()
                true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                onChannelDown.invoke()
                true
            }
            else -> false
        }
    }
    
    /**
     * 重置焦点，确保视图可以接收键盘事件
     */
    fun resetFocus() {
        view.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
    }
}

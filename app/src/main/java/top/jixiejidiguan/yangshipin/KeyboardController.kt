package top.jixiejidiguan.yangshipin

import android.view.KeyEvent
import android.view.View

/**
 * 键盘控制器
 * 处理键盘事件，实现上下键切换频道，回车键/空格键切换全屏/显示UI，数字键快速切换频道
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
            // 只处理按键按下事件
            if (event.action == KeyEvent.ACTION_DOWN) {
                handleKeyEvent(keyCode)
            } else {
                false
            }
        }
        // 设置视图可聚焦，以便接收键盘事件
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }
    
    /**
     * 处理键盘事件
     */
    private fun handleKeyEvent(keyCode: Int): Boolean {
        return when (keyCode) {
            // 上箭头键 - 切换到上一个频道
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_PAGE_UP -> {
                onChannelUp.invoke()
                true
            }
            // 下箭头键 - 切换到下一个频道
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_PAGE_DOWN -> {
                onChannelDown.invoke()
                true
            }
            // 回车键/空格键 - 切换全屏/显示UI
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> {
                onEnterKey.invoke()
                true
            }
            // 返回键 - 切换全屏/显示UI
            KeyEvent.KEYCODE_BACK -> {
                onEnterKey.invoke()
                true
            }
            // 数字键 - 快速切换频道 (0-9)
            KeyEvent.KEYCODE_0 -> {
                onNumberKey(0)
                true
            }
            KeyEvent.KEYCODE_1 -> {
                onNumberKey(1)
                true
            }
            KeyEvent.KEYCODE_2 -> {
                onNumberKey(2)
                true
            }
            KeyEvent.KEYCODE_3 -> {
                onNumberKey(3)
                true
            }
            KeyEvent.KEYCODE_4 -> {
                onNumberKey(4)
                true
            }
            KeyEvent.KEYCODE_5 -> {
                onNumberKey(5)
                true
            }
            KeyEvent.KEYCODE_6 -> {
                onNumberKey(6)
                true
            }
            KeyEvent.KEYCODE_7 -> {
                onNumberKey(7)
                true
            }
            KeyEvent.KEYCODE_8 -> {
                onNumberKey(8)
                true
            }
            KeyEvent.KEYCODE_9 -> {
                onNumberKey(9)
                true
            }
            // 其他按键不处理
            else -> false
        }
    }
    
    /**
     * 重置焦点，确保视图可以接收键盘事件
     */
    fun resetFocus() {
        view.requestFocus()
    }
}
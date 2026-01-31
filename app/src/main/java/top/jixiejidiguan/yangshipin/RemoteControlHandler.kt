package top.jixiejidiguan.yangshipin

import android.view.KeyEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager

class RemoteControlHandler(private val activity: MainActivity) {
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> if (activity.isReverseSwitching) handleDpadDown() else handleDpadUp()
            KeyEvent.KEYCODE_DPAD_DOWN -> if (activity.isReverseSwitching) handleDpadUp() else handleDpadDown()
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_SPACE -> handleConfirm()
            KeyEvent.KEYCODE_BACK -> handleBack()
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_ESCAPE -> handleMenu()
            else -> false
        }
    }
    
    private fun handleMenu(): Boolean {
        if (activity.cardReverse.alpha == 1f) {
            activity.hideReverseCard()
        } else {
            activity.showReverseCard()
        }
        return true
    }
    
    private fun handleDpadUp(): Boolean {
        if (activity.channelSidebar.isVisible) {
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter ?: return false
            val channelCount = activity.channels.size
            val currentPosition = getCurrentSelectedPosition(channelAdapter)
            val newPosition = if (currentPosition > 0) currentPosition - 1 else channelCount - 1
            
            updateSidebarSelection(newPosition, channelAdapter)
            return true
        } else {
            val newPosition = if (activity.isReverseSwitching) {
                (activity.currentChannelPosition + 1) % activity.channels.size
            } else {
                (activity.currentChannelPosition - 1 + activity.channels.size) % activity.channels.size
            }
            switchChannel(newPosition)
            return true
        }
    }
    
    private fun handleDpadDown(): Boolean {
        if (activity.channelSidebar.isVisible) {
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter ?: return false
            val channelCount = activity.channels.size
            val currentPosition = getCurrentSelectedPosition(channelAdapter)
            val newPosition = if (currentPosition < channelCount - 1) currentPosition + 1 else 0
            
            updateSidebarSelection(newPosition, channelAdapter)
            return true
        } else {
            val newPosition = if (activity.isReverseSwitching) {
                (activity.currentChannelPosition - 1 + activity.channels.size) % activity.channels.size
            } else {
                (activity.currentChannelPosition + 1) % activity.channels.size
            }
            switchChannel(newPosition)
            return true
        }
    }
    
    private fun updateSidebarSelection(newPosition: Int, channelAdapter: ChannelAdapter) {
        channelAdapter.setSelectedPosition(newPosition)
        activity.currentChannelPosition = newPosition
        
        val layoutManager = activity.channelList.layoutManager as? LinearLayoutManager
        layoutManager?.scrollToPositionWithOffset(newPosition, 100)
        
        resetSidebarHideTimer()
    }
    
    private fun switchChannel(newPosition: Int) {
        activity.currentChannelPosition = newPosition
        // 保存值到SharedPreferences
        activity.saveInt("current_channel", newPosition)
        
        val channelAdapter = activity.channelList.adapter as? ChannelAdapter
        channelAdapter?.setSelectedPosition(newPosition)
        
        val currentChannel = activity.channels[newPosition]
        activity.showChannelCard(currentChannel)
        
        val url = AppConfig.getChannelData()[currentChannel] ?: return
        activity.loadUrl(url)
    }
    
    private fun resetSidebarHideTimer() {
        activity.resetSidebarHideTimer()
    }
    
    private fun handleConfirm(): Boolean {
        return if (!activity.channelSidebar.isVisible) {
            activity.showSidebar()
            true
        } else {
            val channelAdapter = activity.channelList.adapter as? ChannelAdapter ?: return false
            val currentPosition = getCurrentSelectedPosition(channelAdapter)
            
            if (currentPosition in activity.channels.indices) {
                switchChannel(currentPosition)
                activity.hideSidebar()
                true
            } else false
        }
    }

    private fun handleBack(): Boolean {
        if (activity.channelSidebar.isVisible) {
            activity.hideSidebar()
            return true
        } else {
            handleMenu()
            return false
        }
    }
    
    private fun getCurrentSelectedPosition(adapter: ChannelAdapter): Int {
        return adapter.getSelectedPosition()
    }
}
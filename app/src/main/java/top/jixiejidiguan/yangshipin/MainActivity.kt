package top.jixiejidiguan.yangshipin

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.webkit.WebView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), WebViewHelper.BrowserCallback, GestureController.GestureListener {
    private lateinit var webViewHelper: WebViewHelper
    private lateinit var gestureController: GestureController
    private lateinit var keyboardController: KeyboardController
    private lateinit var webView: WebView
    private lateinit var bottomToast: TextView
    private lateinit var channelList: RecyclerView
    private lateinit var channelAdapter: ChannelListAdapter
    private var currentChannelIndex = 0 // 当前频道索引
    private val hideDelay = 5000L // 自动隐藏延迟时间，5秒
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideUIElements() }
    private var isUIHidden = false // UI元素是否已隐藏
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 隐藏底部小白条（导航栏）
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        // 设置横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        webView = findViewById(R.id.web_view)
        bottomToast = findViewById(R.id.bottom_toast)
        channelList = findViewById(R.id.channel_list)

        // 初始化浏览器
        webViewHelper = WebViewHelper.getInstance()
        webViewHelper.init(this, webView, this)
        
        // 初始化手势控制器
        gestureController = GestureController(this, webView, this)
        
        // 初始化键盘控制器
        keyboardController = KeyboardController(
            webView,
            onChannelUp = { onSwipeUp() }, // 上键对应向上滑动切换频道
            onChannelDown = { onSwipeDown() }, // 下键对应向下滑动切换频道
            onEnterKey = { onSingleTap() }, // 回车键对应点击屏幕
            onNumberKey = { number -> 
                // 数字键快速切换频道，数字1对应第一个频道，数字0对应第10个频道
                val channelIndex = if (number == 0) 9 else number - 1
                val channelCount = AppConfig.getData().size
                if (channelIndex < channelCount) {
                    currentChannelIndex = channelIndex
                    helper(currentChannelIndex)
                }
            }
        )
        
        // 初始化频道列表
        initChannelList()
        
        // 获取CCTV1的URL并加载
        helper(currentChannelIndex)
        
        // 初始化时显示UI元素，然后启动自动隐藏定时器
        showUIElements()
        startHideTimer()
    }

    override fun onPageFinished(url: String?) {
        // 页面加载完成时，尝试让视频自动全屏播放
        webViewHelper.triggerVideoFullscreen()
    }

    
    // 实现GestureListener接口的方法
    override fun onSingleTap() {
        // 点击屏幕可以切换全屏/非全屏模式
        webViewHelper.toggleFullscreen()
        
        // 点击屏幕时显示UI元素并重置定时器
        toggleUIElements()
    }
    
    override fun onSwipeDown() {
        // 向下滑动 - 切换到下一个频道
        currentChannelIndex++
        // 获取频道总数
        val channelCount = AppConfig.getData().size
        // 如果超出范围，循环到第一个频道
        if (currentChannelIndex >= channelCount) {
            currentChannelIndex = 0
        }
        helper(currentChannelIndex)
        // 验证并打印当前选中位置
        val selectedPos = channelAdapter.getSelectedPosition()
        println("Current selected channel position: $selectedPos")
    }
    
    override fun onSwipeUp() {
        // 向上滑动 - 切换到上一个频道
        currentChannelIndex--
        // 获取频道总数
        val channelCount = AppConfig.getData().size
        // 如果超出范围，循环到最后一个频道
        if (currentChannelIndex < 0) {
            currentChannelIndex = channelCount - 1
        }
        helper(currentChannelIndex)
        // 验证并打印当前选中位置
        val selectedPos = channelAdapter.getSelectedPosition()
        println("Current selected channel position: $selectedPos")
    }
    /**
     * 初始化频道列表
     */
    private fun initChannelList() {
        // 获取所有频道数据
        val channels = AppConfig.getData()
        // 转换为Pair列表，用于适配器
        val channelListData = channels.map { it.key to it.value }
        
        // 创建适配器，添加选中变化回调用于自动滚动
        channelAdapter = ChannelListAdapter(channelListData, { position ->
            // 点击频道项时切换频道
            currentChannelIndex = position
            helper(currentChannelIndex)
        }, {
            // 选中项变化时自动滚动到可见区域，添加位置验证避免无效位置导致闪退
            if (it >= 0 && it < channelListData.size) {
                channelList.smoothScrollToPosition(it)
            }
        })
        
        // 设置布局管理器和适配器
        channelList.layoutManager = LinearLayoutManager(this)
        channelList.adapter = channelAdapter
        
        // 设置初始选中项并滚动到可见区域
        channelAdapter.setSelectedPosition(currentChannelIndex)
        channelList.smoothScrollToPosition(currentChannelIndex)
    }
    
    /**
     * 显示所有UI元素
     */
    private fun showUIElements() {
        // 显示频道列表
        channelList.visibility = View.VISIBLE
        // 显示底部提示
        bottomToast.visibility = View.VISIBLE
        
        // 添加淡入动画
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300
        channelList.startAnimation(fadeIn)
        bottomToast.startAnimation(fadeIn)
        
        isUIHidden = false
        // 显示UI时恢复选中状态
        channelAdapter.setSelectedPosition(currentChannelIndex)
    }
    
    /**
     * 隐藏所有UI元素
     */
    private fun hideUIElements() {
        // 添加淡出动画
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 300
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // 动画结束后隐藏元素
                channelList.visibility = View.GONE
                bottomToast.visibility = View.GONE
                isUIHidden = true
                // 隐藏UI时清除选中状态，减少视觉干扰
                channelAdapter.clearSelection()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        
        channelList.startAnimation(fadeOut)
        bottomToast.startAnimation(fadeOut)
    }
    
    /**
     * 切换UI元素的显示状态
     */
    private fun toggleUIElements() {
        if (isUIHidden) {
            showUIElements()
        } else {
            hideUIElements()
        }
        // 重置定时器
        resetHideTimer()
    }
    
    /**
     * 启动自动隐藏定时器
     */
    private fun startHideTimer() {
        handler.postDelayed(hideRunnable, hideDelay)
    }
    
    /**
     * 重置自动隐藏定时器
     */
    private fun resetHideTimer() {
        handler.removeCallbacks(hideRunnable)
        startHideTimer()
    }
    
    private fun helper(zhi: Int) {
        // 使用getChannel()方法获取频道数据
        val channel = channelAdapter.getChannel(zhi)
        if (channel != null) {
            val (title, url) = channel
            bottomToast.text = title
            webViewHelper.loadUrl(url)
        } else {
            // 降级处理：如果获取失败，使用原有方式
            val cctv1Url = AppConfig.getUrlByIndex(zhi)
            val channelTitle = AppConfig.getChannelTitleByIndex(zhi)
            bottomToast.text = channelTitle
            webViewHelper.loadUrl(cctv1Url)
        }
        
        // 更新列表选中状态
        channelAdapter.setSelectedPosition(zhi)
        
        // 显示UI元素并重置定时器
        showUIElements()
        resetHideTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewHelper.cleanup()
        // 移除所有定时器回调
        handler.removeCallbacks(hideRunnable)
    }
    
    override fun onResume() {
        super.onResume()
        // 恢复焦点，确保键盘事件能被正确接收
        keyboardController.resetFocus()
    }
}


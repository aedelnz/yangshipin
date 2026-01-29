package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    lateinit var channelSidebar: LinearLayout
    lateinit var channelList: RecyclerView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    var hideSidebarHandler: Handler? = null
    private var hideChannelCardHandler: Handler? = null
    private var updateTimeHandler: Handler? = null
    private lateinit var gestureController: GestureController
    private lateinit var remoteControlHandler: RemoteControlHandler
    var currentChannelPosition = 0
    lateinit var channels: List<String>
    private lateinit var channelCard: CardView
    private lateinit var channelTitle: TextView
    private lateinit var currentTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)
        channelSidebar = findViewById(R.id.channel_sidebar)

        // 初始化频道卡片
        channelCard = findViewById(R.id.channel_card)
        channelTitle = findViewById(R.id.channel_title)
        currentTime = findViewById(R.id.current_time)

        // 设置频道选择器
        channels = AppConfig.getChannelData().keys.toList()

        // 初始化RecyclerView（布局中id为channelList）
        channelList = findViewById(R.id.channel_recyclerview)
        // 设置布局管理器：垂直线性列表（核心，RecyclerView必须设置）
        channelList.layoutManager = LinearLayoutManager(this)
        // 初始化适配器，设置点击回调
        val channelAdapter = ChannelAdapter(channels) { selectedChannel ->
            // 点击回调：获取选中频道的URL，加载页面
            val url = AppConfig.getChannelData()[selectedChannel] ?: return@ChannelAdapter
            // 更新当前选中的频道位置
            currentChannelPosition = channels.indexOf(selectedChannel)
            // 显示频道卡片
            showChannelCard(selectedChannel)
            // 加载页面
            loadUrl(url)
            // 点击频道后立即隐藏侧边栏
            hideSidebar()
        }
        // 绑定适配器
        channelList.adapter = channelAdapter
        
        // 添加滚动监听器，优化滚动时不隐藏
        channelList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // 开始滚动，取消自动隐藏
                        hideSidebarHandler?.removeCallbacksAndMessages(null)
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 滚动停止，重新设置自动隐藏
                        hideSidebarHandler?.postDelayed({
                            hideSidebar()
                        }, 3000)
                    }
                }
            }
        })
        if (channels.isNotEmpty()) {
            val defaultUrl = AppConfig.getChannelData()[channels[0]] ?: return
            loadUrl(defaultUrl)
            // 默认选中第一个频道
            channelAdapter.setSelectedPosition(0)
        }
        setupWebView()
        
        // 初始化 Handler
        hideSidebarHandler = Handler(Looper.getMainLooper())
        hideChannelCardHandler = Handler(Looper.getMainLooper())
        updateTimeHandler = Handler(Looper.getMainLooper())
        
        // 初始化手势控制器
        gestureController = GestureController(this)
        
        // 初始化遥控器控制器
        remoteControlHandler = RemoteControlHandler(this)
        
        // 初始显示侧边栏，3秒后自动隐藏
        showSidebar()
    }
    
    fun showSidebar() {
        channelSidebar.visibility = View.VISIBLE
        channelSidebar.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
        
        // 启用RecyclerView滚动
        channelList.isEnabled = true
        channelList.requestFocus()
        
        // 取消之前的隐藏任务
        hideSidebarHandler?.removeCallbacksAndMessages(null)
        
        // 3秒后自动隐藏
        hideSidebarHandler?.postDelayed({
            hideSidebar()
        }, 3000)
    }
    
    fun hideSidebar() {
        // 禁用RecyclerView滚动
        channelList.isEnabled = false
        
        channelSidebar.animate()
            .translationX(-240f)
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                channelSidebar.visibility = View.GONE
            }
            .start()
    }

    // 时间更新的Runnable
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            // 更新当前时间，只显示时分秒
            val currentTimeStr = java.text.SimpleDateFormat("HH时mm分ss秒", java.util.Locale.getDefault()).format(java.util.Date())
            currentTime.text = currentTimeStr
            // 每秒更新一次
            updateTimeHandler?.postDelayed(this, 1000)
        }
    }

    /**
     * 显示频道切换卡片
     * @param channelName 频道名称
     */
    fun showChannelCard(channelName: String) {
        // 取消之前的隐藏任务
        hideChannelCardHandler?.removeCallbacksAndMessages(null)
        // 取消之前的时间更新任务
        updateTimeHandler?.removeCallbacksAndMessages(null)
        
        // 设置频道标题
        channelTitle.text = channelName
        
        // 立即设置当前时间
        val currentTimeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        currentTime.text = currentTimeStr
        
        // 显示卡片
        channelCard.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
        
        // 启动时间实时更新
        updateTimeHandler?.postDelayed(updateTimeRunnable, 1000)
        
        // 3秒后自动隐藏
        hideChannelCardHandler?.postDelayed({
            hideChannelCard()
        }, 5000)
    }

    /**
     * 隐藏频道切换卡片
     */
    fun hideChannelCard() {
        // 取消时间更新任务
        updateTimeHandler?.removeCallbacksAndMessages(null)
        
        channelCard.animate()
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .start()
    }

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt", "ClickableViewAccessibility")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // PC模式核心配置
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.setSupportZoom(false)

        // 横屏PC网页适配优化（推荐追加）
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isHorizontalScrollBarEnabled = true

        // 支持混合内容（http/https）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 注入JavaScript去除臃肿元素和增强播放器
                val jsCode = $$"""
(function(){
window.androidPlayer={makeFullscreen(){const s=document.createElement('style');s.innerHTML='body,html{overflow:hidden!important;background:#000!important;}video{position:fixed!important;top:0;left:0;width:100vw!important;height:100vh!important;object-fit:contain!important;z-index:99999!important;background:#000!important;}';document.head.appendChild(s)},getVideo(){return document.querySelector('video')||null},togglePlay(){const v=this.getVideo();v&&(v.paused?v.play():this.makeFullscreen())}};
(function(){let t=null,c=0;t=setInterval(()=>{c++;const v=document.querySelector('video');if(v||c>=15){clearInterval(t);v&&(v.muted=!0,window.androidPlayer.togglePlay())}},1e3)})()
})();
                """.trimIndent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(jsCode, null)
                } else {
                    webView.loadUrl("javascript:$jsCode")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
                setContentView(customView)
            }

            override fun onHideCustomView() {
                if (customView == null) return
                setContentView(R.layout.activity_main)
                customView = null
                customViewCallback?.onCustomViewHidden()
            }
        }



        // 禁用 WebView 的点击交互
        webView.isClickable = false
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false
        webView.setOnTouchListener { _, _ -> true }
        
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        hideSidebarHandler?.removeCallbacksAndMessages(null)
        hideChannelCardHandler?.removeCallbacksAndMessages(null)
        updateTimeHandler?.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }
    
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "按键事件分发: keyCode=${event.keyCode}, action=${event.action}")
        // 处理遥控器按键事件
        if (remoteControlHandler.handleKeyEvent(event)) {
            Log.d(TAG, "按键事件被 RemoteControlHandler 处理")
            return true
        }
        // 对于未处理的按键事件，仍然返回true，确保不会传递给WebView
        // 这样可以完全禁止WebView处理任何按键事件
        return true
    }
    
    fun handleVerticalSwipe(e1: MotionEvent, e2: MotionEvent, velocityY: Float): Boolean {
        val channelAdapter = channelList.adapter as? ChannelAdapter
        if (channelAdapter != null) {
            val channelCount = channels.size
            // 下滑手势：从上向下滑动，切换到下一个频道
            if (e2.y - e1.y > 100 && abs(velocityY) > 100) {
                // 实现无限循环：当到达最后一个频道时，循环到第一个
                currentChannelPosition = if (currentChannelPosition < channelCount - 1) {
                    currentChannelPosition + 1
                } else {
                    0 // 循环到第一个频道
                }
                val currentChannel = channels[currentChannelPosition]
                channelAdapter.setSelectedPosition(currentChannelPosition)
                showChannelCard(currentChannel)
                val url = AppConfig.getChannelData()[currentChannel] ?: return true
                loadUrl(url)
                return true
            }
            // 上滑手势：从下向上滑动，切换到上一个频道
            if (e1.y - e2.y > 100 && abs(velocityY) > 100) {
                // 实现无限循环：当到达第一个频道时，循环到最后一个
                currentChannelPosition = if (currentChannelPosition > 0) {
                    currentChannelPosition - 1
                } else {
                    channelCount - 1 // 循环到最后一个频道
                }
                val currentChannel = channels[currentChannelPosition]
                channelAdapter.setSelectedPosition(currentChannelPosition)
                showChannelCard(currentChannel)
                val url = AppConfig.getChannelData()[currentChannel] ?: return true
                loadUrl(url)
                return true
            }
        }
        return false
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureController.gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
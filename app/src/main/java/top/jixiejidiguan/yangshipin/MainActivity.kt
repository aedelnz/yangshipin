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
        channelCard = findViewById(R.id.channel_card)
        channelTitle = findViewById(R.id.channel_title)
        currentTime = findViewById(R.id.current_time)
        // 设置频道选择器
        channels = AppConfig.getChannelData().keys.toList()
        // 初始化RecyclerView（布局中id为channelList）
        channelList = findViewById(R.id.channel_recyclerview)
        // 设置布局管理器：垂直线性列表（核心，RecyclerView必须设置）
        channelList.layoutManager = LinearLayoutManager(this)
        // 性能优化设置
        channelList.setHasFixedSize(true) // 固定大小，提高性能
        channelList.setItemViewCacheSize(20) // 增加缓存大小
        channelList.isNestedScrollingEnabled = false // 禁用嵌套滚动
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
            currentChannelPosition = 0 // 同步更新当前频道位置
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
            val currentTimeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            currentTime.text = currentTimeStr
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

    private fun setupWebView() {
        // 优先使用 Google WebView 内核
        try {
            // 1. 检测当前 WebView 实现
            val webViewFactoryClass = Class.forName("android.webkit.WebViewFactory")
            val getProviderMethod = webViewFactoryClass.getDeclaredMethod("getProvider")
            getProviderMethod.isAccessible = true
            val provider = getProviderMethod.invoke(null)
            val providerClass = provider.javaClass.name
            // 2. 检查是否安装了 Google WebView
            val packageManager = packageManager
            var hasGoogleWebView = false
            try {
                val googleWebViewInfo = packageManager.getPackageInfo(
                    "com.google.android.webview",
                    0
                )
                hasGoogleWebView = true
            } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
                hasGoogleWebView = false
            }
            // 4. 检查系统 WebView
            var hasSystemWebView = false
            try {
                val systemWebViewInfo = packageManager.getPackageInfo(
                    "com.android.webview",
                    0
                )
                hasSystemWebView = true
            } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
                hasSystemWebView = false
            }
        } catch (e: Exception) {
        }
        
        // 6. 确保 WebView 硬件加速已启用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        
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
        
        // 视频播放优化设置
        settings.mediaPlaybackRequiresUserGesture = false // 允许自动播放
        settings.allowContentAccess = true // 允许内容访问
        settings.allowFileAccess = true // 允许文件访问
        @Suppress("DEPRECATION")
        settings.allowFileAccessFromFileURLs = true // 允许从文件 URL 访问文件
        @Suppress("DEPRECATION")
        settings.allowUniversalAccessFromFileURLs = true // 允许从文件 URL 访问任何资源
        
        // 启用硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        
        // 媒体播放优化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
        }
        
        // 启用 HTML5 视频支持
        @Suppress("DEPRECATION")
        settings.pluginState = WebSettings.PluginState.ON
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 注入JavaScript去除臃肿元素和增强播放器
                val jsCode = """
(function(){
console.log('注入视频播放器增强脚本');
window.androidPlayer={
    makeFullscreen:function(){
        console.log('进入全屏模式');
        const s=document.createElement('style');
        s.innerHTML='body,html{overflow:hidden!important;background:#000!important;}video{position:fixed!important;top:0;left:0;width:100vw!important;height:100vh!important;object-fit:contain!important;z-index:99999!important;background:#000!important;}';
        document.head.appendChild(s);
    },
    getVideo:function(){return document.querySelector('video')||null},
    togglePlay:function(){const v=this.getVideo();if(v){console.log('切换播放状态:',v.paused?'播放':'全屏');v.paused?v.play():this.makeFullscreen()}}
};

(function(){let t=null,c=0;t=setInterval(()=>{c++;const v=document.querySelector('video');if(v||c>=15){clearInterval(t);if(v){console.log('找到视频元素，准备播放');v.muted=false;window.androidPlayer.togglePlay()}}},1e3)})()
})();
                """.trimIndent()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(jsCode) { result -> }
                } else {
                    webView.loadUrl("javascript:$jsCode")
                }
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
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
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
                setContentView(customView)
            }

            override fun onHideCustomView() {
                if (customView == null) return
                setContentView(R.layout.activity_main)
                customView = null
                customViewCallback?.onCustomViewHidden()
            }
            
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }

        // 禁用 WebView 的点击交互
        webView.isClickable = false
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false
        webView.setOnTouchListener { _, _ -> true }
        
    }

    fun loadUrl(url: String) {
        // 清除 WebView 缓存，避免之前的错误影响
        webView.clearCache(false)
        webView.clearHistory()
        // 重置 WebView 状态
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
        }
        // 加载 URL
        webView.loadUrl(url)
    }

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
        if (remoteControlHandler.handleKeyEvent(event)) {
            return true
        }
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
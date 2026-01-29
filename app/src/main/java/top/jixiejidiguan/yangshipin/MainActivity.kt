package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var channelSidebar: LinearLayout
    private lateinit var channelList: RecyclerView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var hideSidebarHandler: Handler? = null
    private lateinit var gestureDetector: GestureDetector
    private var currentChannelPosition = 0
    private lateinit var channels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)
        channelSidebar = findViewById(R.id.channel_sidebar)

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
            loadUrl(url)
            // 更新当前选中的频道位置
            currentChannelPosition = channels.indexOf(selectedChannel)
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
        
        // 初始化手势检测器
        gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null) {
                    val deltaX = Math.abs(e2.x - e1.x)
                    val deltaY = Math.abs(e2.y - e1.y)
                    
                    // 优先检测左右滑动手势（水平方向）
                    if (deltaX > deltaY) {
                        // 右滑手势：从左向右滑动，显示侧边栏
                        if (e2.x - e1.x > 100 && Math.abs(velocityX) > 100) {
                            showSidebar()
                            return true
                        }
                        // 左滑手势：从右向左滑动，隐藏侧边栏
                        if (e1.x - e2.x > 100 && Math.abs(velocityX) > 100) {
                            hideSidebar()
                            return true
                        }
                    } else {
                        // 只有当侧边栏不可见时，才执行上下滑动切换频道的逻辑
                        if (channelSidebar.visibility != View.VISIBLE) {
                            // 上下滑动手势（垂直方向），切换频道
                            val channelAdapter = channelList.adapter as? ChannelAdapter
                            if (channelAdapter != null) {
                                // 下滑手势：从上向下滑动，切换到下一个频道
                                if (e2.y - e1.y > 100 && Math.abs(velocityY) > 100) {
                                    if (currentChannelPosition < channels.size - 1) {
                                        currentChannelPosition++
                                        channelAdapter.setSelectedPosition(currentChannelPosition)
                                        val url = AppConfig.getChannelData()[channels[currentChannelPosition]] ?: return true
                                        loadUrl(url)
                                        return true
                                    }
                                }
                                // 上滑手势：从下向上滑动，切换到上一个频道
                                if (e1.y - e2.y > 100 && Math.abs(velocityY) > 100) {
                                    if (currentChannelPosition > 0) {
                                        currentChannelPosition--
                                        channelAdapter.setSelectedPosition(currentChannelPosition)
                                        val url = AppConfig.getChannelData()[channels[currentChannelPosition]] ?: return true
                                        loadUrl(url)
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        
        // 初始显示侧边栏，3秒后自动隐藏
        showSidebar()
    }
    
    private fun showSidebar() {
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
    
    private fun hideSidebar() {
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

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
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
                val jsCode = """
            (function(){
                window.androidPlayer = {
                    getVideo: function() { return document.querySelector('video'); },
                    togglePlay: function() { 
                        var v = this.getVideo();
                        if(v) v.paused ? v.play() : v.pause();
                    },
                    seek: function(sec) {
                        var v = this.getVideo();
                        if(v) v.currentTime += sec;
                    },
                    makeFullscreen: function() {
                        var s = document.createElement('style');
                        s.innerHTML = 'body,html{overflow:hidden!important;background:#000!important;} video{position:fixed!important;top:0;left:0;width:100vw!important;height:100vh!important;object-fit:contain!important;z-index:99999!important;background:#000!important;}';
                        document.head.appendChild(s);
                    }
                };
                window.androidPlayer.makeFullscreen();
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
    }

    private fun loadUrl(url: String) {
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
        webView.destroy()
        super.onDestroy()
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
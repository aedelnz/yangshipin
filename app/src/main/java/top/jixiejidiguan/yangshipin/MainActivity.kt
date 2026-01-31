package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CheckBox
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
    private val handler = Handler(Looper.getMainLooper())
    
    fun resetSidebarHideTimer() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ hideSidebar() }, 3000)
    }
    private lateinit var gestureController: GestureController
    private lateinit var remoteControlHandler: RemoteControlHandler
    var currentChannelPosition = 0
    lateinit var channels: List<String>
    private lateinit var channelCard: CardView
    private lateinit var channelTitle: TextView
    private lateinit var currentTime: TextView
    lateinit var cardReverse: CardView
    private lateinit var checkBoxSwitching: CheckBox
    private val preferencesManager by lazy {
        PreferencesManager(this)
    }
    var isReverseSwitching = false
    
    fun saveBoolean(key: String, value: Boolean) {
        preferencesManager.saveBoolean(key, value)
    }
    
    fun saveInt(key: String, value: Int) {
        preferencesManager.saveInt(key, value)
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferencesManager.getBoolean(key, defaultValue)
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return preferencesManager.getInt(key, defaultValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.web_view)
        channelSidebar = findViewById(R.id.channel_sidebar)
        channelCard = findViewById(R.id.channel_card)
        channelTitle = findViewById(R.id.channel_title)
        currentTime = findViewById(R.id.current_time)
        cardReverse = findViewById(R.id.card_reverse)
        checkBoxSwitching = findViewById(R.id.checkBox_switching)
        
        channels = AppConfig.getChannelData().keys.toList()
        // 确保currentChannelPosition在有效范围内
        if (channels.isEmpty() || currentChannelPosition >= channels.size) {
            currentChannelPosition = 0
            saveInt("current_channel", currentChannelPosition)
        }
        channelList = findViewById(R.id.channel_recyclerview)
        channelList.layoutManager = LinearLayoutManager(this)
        channelList.setHasFixedSize(true)
        channelList.setItemViewCacheSize(20)
        channelList.isNestedScrollingEnabled = false
        
        val channelAdapter = ChannelAdapter(this, channels) { selectedChannel ->
            val url = AppConfig.getChannelData()[selectedChannel] ?: return@ChannelAdapter
            currentChannelPosition = channels.indexOf(selectedChannel)
            // 保存值到SharedPreferences
            saveInt("current_channel", currentChannelPosition)
            showChannelCard(selectedChannel)
            loadUrl(url)
            hideSidebar()
        }
        channelList.adapter = channelAdapter
        
        channelList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> handler.removeCallbacksAndMessages(null)
                    RecyclerView.SCROLL_STATE_IDLE -> handler.postDelayed({ hideSidebar() }, 3000)
                }
            }
        })
        
        if (channels.isNotEmpty()) {
            // 确保currentChannelPosition在有效范围内
            if (currentChannelPosition >= channels.size) {
                currentChannelPosition = 0
                saveInt("current_channel", currentChannelPosition)
            }
            val defaultUrl = AppConfig.getChannelData()[channels[currentChannelPosition]] ?: return
            loadUrl(defaultUrl)
            channelAdapter.setSelectedPosition(currentChannelPosition)
        }
        setupWebView()
        gestureController = GestureController(this)
        remoteControlHandler = RemoteControlHandler(this)
        showSidebar()
        
        // 从PreferencesManager中读取保存的值
        isReverseSwitching = getBoolean("reverse_switching", false)
        checkBoxSwitching.isChecked = isReverseSwitching
        // 从PreferencesManager中读取保存的频道位置
        currentChannelPosition = getInt("current_channel", 0)
        
        checkBoxSwitching.setOnCheckedChangeListener { _, isChecked ->
            isReverseSwitching = isChecked
            // 保存值到SharedPreferences
            saveBoolean("reverse_switching", isChecked)
        }
    }
    
    fun showSidebar() {
        animateView(channelSidebar, 0f, 1f, 300, android.view.animation.DecelerateInterpolator()) { 
            channelSidebar.visibility = View.VISIBLE
            channelList.isEnabled = true
            channelList.requestFocus()
            handler.removeCallbacksAndMessages(null)
            currentTime.text = getCurrentTime()
            handler.postDelayed(updateTimeRunnable, 1000)
            handler.postDelayed({ hideSidebar() }, 3000)
        }
    }
    
    fun hideSidebar() {
        channelList.isEnabled = false
        handler.removeCallbacksAndMessages(null)
        animateView(channelSidebar, -240f, 0f, 300, android.view.animation.AccelerateInterpolator()) { 
            channelSidebar.visibility = View.GONE
        }
    }
    
    private val updateTimeRunnable: Runnable = Runnable { 
        currentTime.text = getCurrentTime()
        handler.postDelayed(updateTimeRunnable, 1000)
    }
    
    private fun getCurrentTime() = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    
    fun showChannelCard(channelName: String) {
        handler.removeCallbacksAndMessages(null)
        channelTitle.text = channelName
        currentTime.text = getCurrentTime()
        animateView(channelCard, 0f, 1f, 300, android.view.animation.DecelerateInterpolator())
        handler.postDelayed(updateTimeRunnable, 1000)
        handler.postDelayed({ hideChannelCard() }, 5000)
    }
    
    fun hideChannelCard() {
        handler.removeCallbacksAndMessages(null)
        animateView(channelCard, 0f, 0f, 300, android.view.animation.AccelerateInterpolator())
    }
    
    fun showReverseCard() {
        handler.removeCallbacksAndMessages(null)
        animateView(cardReverse, 0f, 1f, 300, android.view.animation.DecelerateInterpolator())
    }
    
    fun hideReverseCard() {
        handler.removeCallbacksAndMessages(null)
        animateView(cardReverse, 0f, 0f, 300, android.view.animation.AccelerateInterpolator())
    }
    
    private fun animateView(view: View, translationX: Float, alpha: Float, duration: Long, interpolator: android.view.animation.Interpolator, action: (() -> Unit)? = null) {
        view.animate()
            .translationX(translationX)
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction { action?.invoke() }
            .start()
    }

    private fun setupWebView() {
        webView.setLayerType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
            null
        )
        
        val settings = webView.settings
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            
            allowContentAccess = true
            allowFileAccess = false
        }

        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isHorizontalScrollBarEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val jsCode = """(function(){window.androidPlayer={makeFullscreen:function(){const s=document.createElement('style');s.innerHTML='body,html{overflow:hidden!important;background:#000!important;}video{position:fixed!important;top:0;left:0;width:100vw!important;height:100vh!important;object-fit:contain!important;z-index:99999!important;background:#000!important;}';document.head.appendChild(s)},getVideo:function(){return document.querySelector('video')||null},togglePlay:function(){const v=this.getVideo();if(v){v.paused?v.play():this.makeFullscreen()}}};(function(){let t=null,c=0;t=setInterval(()=>{c++;const v=document.querySelector('video');if(v||c>=15){clearInterval(t);if(v){v.muted=false;window.androidPlayer.togglePlay()}}},1e3)})()})();""".trimIndent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(jsCode) { }
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
                // Use WindowInsetsController for API 30+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.hide(
                        android.view.WindowInsets.Type.statusBars() or 
                        android.view.WindowInsets.Type.navigationBars()
                    )
                    window.insetsController?.systemBarsBehavior = 
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or 
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or 
                        View.SYSTEM_UI_FLAG_IMMERSIVE
                }
                setContentView(customView)
            }

            override fun onHideCustomView() {
                if (customView == null) return
                
                // Restore system UI
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.show(
                        android.view.WindowInsets.Type.statusBars() or 
                        android.view.WindowInsets.Type.navigationBars()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                
                setContentView(R.layout.activity_main)
                customView = null
                customViewCallback?.onCustomViewHidden()
            }
        }

        @Suppress("DEPRECATION")
        webView.apply {
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            setOnTouchListener { _, _ -> true }
        }
    }

    fun loadUrl(url: String) {
        webView.clearCache(false)
        webView.clearHistory()
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
        }
        webView.loadUrl(url)
    }


    /**
     * 当 Activity 销毁时调用
     * 主要用于释放资源，防止内存泄漏
     */
    override fun onDestroy() {
        // 移除 Handler 中所有待执行的消息和回调
        // 传入 null 代表移除全部，防止 Activity 销毁后异步任务还在运行导致崩溃
        handler.removeCallbacksAndMessages(null)
        // 销毁 WebView 实例
        // 必须显式调用，以释放内存、停止 Flash/JS 加载并解除与窗口系统的绑定
        webView.destroy()
        // 调用父类的销毁逻辑
        super.onDestroy()
    }

    /**
     * 分发按键事件
     * 在按键到达具体的 View 之前进行拦截，常用于适配电视遥控器
     *
     * @param event 键盘事件（确认返回键跟上下键等）
     * @return 如果返回 true，表示该事件已被处理，不再向下传递
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return remoteControlHandler.handleKeyEvent(event)
    }

    /**
     * 分发屏幕触摸事件
     * 在触摸事件传给子 View 之前进行拦截
     *
     * @param ev 触摸动作（上下左右滑动，双击屏幕等）
     * @return 返回值遵循系统分发机制
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureController.gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 优化的浏览器辅助类
 * 提供完整的浏览器功能，支持视频播放、JavaScript交互等
 */
@Suppress("OVERRIDE_DEPRECATION")
class WebViewHelper private constructor() {

    companion object {
        private const val TAG = "WebViewHelper"
        private const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (HTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

        @SuppressLint("StaticFieldLeak")
        private var instance: WebViewHelper? = null

        fun getInstance(): WebViewHelper {
            return instance ?: synchronized(this) {
                instance ?: WebViewHelper().also { instance = it }
            }
        }
    }

    // 核心成员变量
    private var webView: WebView? = null
    private var activity: Activity? = null
    private var isInitialized = false
    
    // 全屏相关变量
    private var isFullscreen = false
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    
    // 回调接口
    private var callback: BrowserCallback? = null

    /**
     * 浏览器配置接口
     */
    interface BrowserCallback {
        fun onPageFinished(url: String?)
    }

    /**
     * 初始化浏览器
     * @param activity Activity上下文
     * @param webView WebView实例
     * @param callback 浏览器回调
     */
    fun init(activity: Activity, webView: WebView, callback: BrowserCallback? = null) {
        if (isInitialized) {
            Log.w(TAG, "WebViewHelper already initialized")
            return
        }

        this.activity = activity
        this.webView = webView
        this.callback = callback
        this.isInitialized = true

        setupWebView()
        Log.i(TAG, "WebViewHelper initialized successfully")
    }

    /**
     * 设置WebView
     */
    private fun setupWebView() {
        val webView = this.webView ?: return

        // 基础清理
        resetWebViewState()
        
        // 配置浏览器客户端
        configureWebClients(webView)
        
        // 配置WebSettings
        configureWebSettings(webView.settings)
        
        // 禁用所有交互功能
        disableAllInteractions()
    }
    
    /**
     * 重置WebView状态
     */
    private fun resetWebViewState() {
        webView?.apply {
            clearCache(true)
            clearHistory()
            clearFormData()
        }
        CookieManager.getInstance().removeAllCookies(null)
    }
    
    /**
     * 配置WebView客户端
     */
    private fun configureWebClients(webView: WebView) {
        // WebChromeClient - 处理视频播放等
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                enterFullscreen(view, callback)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                exitFullscreen()
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                Log.d(TAG, "Page title: $title")
            }
            
            // 禁止JavaScript对话框
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                result?.cancel()
                return true
            }
            
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                result?.cancel()
                return true
            }
            
            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: android.webkit.JsPromptResult?): Boolean {
                result?.cancel()
                return true
            }
            
            override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                result?.cancel()
                return true
            }
        }

        // WebViewClient - 处理页面加载
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // 禁止所有链接跳转
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                callback?.onPageFinished(url)
                Log.d(TAG, "Page finished loading: $url")
            }
        }
        
        // 禁止触摸事件和点击
        webView.setOnTouchListener { _, _ -> true }
        
        // 禁止长按上下文菜单
        webView.setOnLongClickListener { true }
    }
    
    /**
     * 进入全屏模式
     */
    private fun enterFullscreen(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (customView != null) {
            exitFullscreen()
            return
        }

        customView = view
        customViewCallback = callback
        isFullscreen = true

        // 将视频视图添加到全屏布局
        activity?.window?.decorView?.let { decorView ->
            (decorView as? ViewGroup)?.addView(view, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
    
    /**
     * 退出全屏模式
     */
    private fun exitFullscreen() {
        if (customView == null) return

        // 移除视频视图
        activity?.window?.decorView?.let {
            (it as? ViewGroup)?.removeView(customView)
        }
        
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        isFullscreen = false
    }
    

    /**
     * 配置WebSettings
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings(settings: WebSettings) {
        // 基础设置
        settings.apply {
            javaScriptEnabled = true // 启用JavaScript（视频播放需要）
            javaScriptCanOpenWindowsAutomatically = false // 禁止JavaScript自动打开窗口
            mediaPlaybackRequiresUserGesture = false // 允许自动播放视频
            
            // 显示设置
            loadWithOverviewMode = false // 缩放至适应屏幕
            useWideViewPort = false // 支持宽视图
            builtInZoomControls = false // 禁用内置缩放控件
            displayZoomControls = false // 隐藏缩放控件
            
            // 缓存设置
            cacheMode = WebSettings.LOAD_DEFAULT // 默认缓存模式
            domStorageEnabled = true // 启用DOM存储
            @Suppress("DEPRECATION")
            databaseEnabled = true // 启用数据库存储
            allowFileAccess = false // 禁止访问文件
            allowContentAccess = false // 禁止访问内容
            
            // 编码设置
            defaultTextEncodingName = "UTF-8" // 默认UTF-8编码
            
            // 硬件加速设置（根据设备情况自动调整）
            @Suppress("DEPRECATION")
            setRenderPriority(WebSettings.RenderPriority.HIGH)

            // 仅在高版本Android上设置UserAgent，避免低版本兼容性问题
            try {
                val osVersion = android.os.Build.VERSION.SDK_INT
                if (osVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    userAgentString = DEFAULT_USER_AGENT
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set user agent: ${e.message}")
            }
            
            // 禁止浏览器控制设置
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false // 禁止从文件URL访问文件
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false // 禁止从文件URL访问所有资源
            setGeolocationEnabled(false) // 禁止地理位置
            setSupportMultipleWindows(false) // 禁止多窗口
        }
    }

    /**
     * 加载URL
     * @param url 要加载的网址
     */
    fun loadUrl(url: String) {
        if (!isInitialized) {
            Log.e(TAG, "WebViewHelper not initialized")
            return
        }
        webView?.loadUrl(url)
    }
    
    /**
     * 禁用WebView的所有交互功能
     */
    private fun disableAllInteractions() {
        webView?.apply {
            // 禁用所有触摸事件
            setOnTouchListener { _, _ -> true }
            // 禁用长按菜单
            setOnLongClickListener { true }
            // 禁用焦点
            isFocusable = false
            isFocusableInTouchMode = false
            // 禁用滚动
            isScrollContainer = false
            // 禁用滚动条
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            // 禁用键盘事件
            isClickable = false
            isLongClickable = false
            // 禁用选择功能
            isSelected = false
            // 禁用复制粘贴
            isHapticFeedbackEnabled = false
            // 禁用焦点查找
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

    /**
     * 尝试让视频播放器全屏
     */
    fun triggerVideoFullscreen() {
        if (!isInitialized) {
            Log.e(TAG, "WebViewHelper not initialized")
            return
        }
        
        webView?.evaluateJavascript(
            """
            (function() {
                var s = document.createElement('style');
                s.innerHTML = `
                body,html{margin:0;padding:0;overflow:hidden;background:#000;}
                video{position:fixed!important;top:0;left:0;width:100vw!important;height:100vh!important;
                object-fit:contain!important;z-index:9999!important;background:#000;}
                `;
                document.head.appendChild(s);
            })();
            """
        ) { result ->
            Log.d(TAG, "Video fullscreen trigger result: $result")
        }
    }
    
    /**
     * 切换全屏/非全屏模式
     */
    fun toggleFullscreen() {
        if (!isInitialized) {
            Log.e(TAG, "WebViewHelper not initialized")
            return
        }
        
        if (isFullscreen) {
            exitFullscreen()
        } else {
            // 触发视频全屏
            triggerVideoFullscreen()
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        // 确保退出全屏
        exitFullscreen()
        
        webView?.apply {
            stopLoading()
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            destroy()
        }
        
        // 重置所有引用
        webView = null
        activity = null
        callback = null
        isInitialized = false
        
        Log.i(TAG, "WebViewHelper cleaned up")
    }
}
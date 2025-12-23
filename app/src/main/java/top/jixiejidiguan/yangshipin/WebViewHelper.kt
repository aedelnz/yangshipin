package top.jixiejidiguan.yangshipin

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 深度优化的浏览器辅助类 - 侧重加载速度与视频性能
 */
class WebViewHelper private constructor() {

    companion object {
        private const val TAG = "WebViewHelper"
        private const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

        @SuppressLint("StaticFieldLeak")
        private var instance: WebViewHelper? = null

        fun getInstance(): WebViewHelper = instance ?: synchronized(this) {
            instance ?: WebViewHelper().also { instance = it }
        }
    }

    private var webView: WebView? = null
    private var activity: Activity? = null
    private var isFullscreen = false
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var callback: BrowserCallback? = null

    interface BrowserCallback {
        fun onPageFinished(url: String?)
    }

    fun init(activity: Activity, webView: WebView, callback: BrowserCallback? = null) {
        this.activity = activity
        this.webView = webView
        this.callback = callback
        setupWebView()
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun setupWebView() {
        val wv = webView ?: return
        
        // 开启硬件加速提升渲染速度
        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        wv.settings.apply {
            // 1. 核心性能设置
            javaScriptEnabled = true
            domStorageEnabled = true // 必须开启，许多视频网站依赖它缓存配置
            @Suppress("DEPRECATION")
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT // 智能缓存模式
            
            // 2. 网络与安全优化
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许HTTPS页面加载HTTP资源，加快加载速度
            javaScriptCanOpenWindowsAutomatically = false
            mediaPlaybackRequiresUserGesture = false // 允许自动播放
            
            // 3. 渲染预热
            @Suppress("DEPRECATION")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                offscreenPreRaster = true // 提前渲染屏幕外内容
            }
            
            // 4. 适配与UA
            userAgentString = UA
            loadWithOverviewMode = false
            useWideViewPort = false
            
            // 5. 禁用不必要的功能以提速
            allowFileAccess = false
            allowContentAccess = false
        }

        wv.apply {
            isFocusable = false
            isClickable = false
            // 禁用滚动条减少绘制压力
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            setOnTouchListener { v, _ -> 
                v.performClick()
                false // 允许 GestureController 处理手势
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, cb: CustomViewCallback?) {
                if (customView != null) { cb?.onCustomViewHidden(); return }
                customView = view
                customViewCallback = cb
                isFullscreen = true
                (activity?.window?.decorView as? ViewGroup)?.addView(view, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            override fun onHideCustomView() { exitFullscreen() }
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean = true
            
            override fun onPageFinished(v: WebView?, url: String?) {
                injectControllerJS()
                callback?.onPageFinished(url)
            }
            
            // 监控加载错误以便调试
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e(TAG, "Load Error: ${error?.description}")
                }
            }
        }
    }

    private fun injectControllerJS() {
        val js = """
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
        webView?.evaluateJavascript(js, null)
    }

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    fun seek(seconds: Int) { webView?.evaluateJavascript("window.androidPlayer.seek($seconds)", null) }

    fun triggerVideoFullscreen() { webView?.evaluateJavascript("window.androidPlayer.makeFullscreen()", null) }

    fun toggleFullscreen() {
        if (isFullscreen) exitFullscreen() else triggerVideoFullscreen()
    }

    private fun exitFullscreen() {
        customView?.let { (activity?.window?.decorView as? ViewGroup)?.removeView(it) }
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        isFullscreen = false
    }

    fun cleanup() {
        exitFullscreen()
        webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        webView = null
        Log.i(TAG, "Optimized cleanup done")
    }
}

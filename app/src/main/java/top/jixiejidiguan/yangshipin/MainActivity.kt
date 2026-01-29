package top.jixiejidiguan.yangshipin

import ChannelAdapter
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)

        // 设置频道选择器
        val channels = AppConfig.getChannelData().keys.toList()

        // 初始化RecyclerView（布局中id为channelList）
        val channelList: RecyclerView = findViewById(R.id.channel_recyclerview)
        // 设置布局管理器：垂直线性列表（核心，RecyclerView必须设置）
        channelList.layoutManager = LinearLayoutManager(this)
        // 初始化适配器，设置点击回调
        val channelAdapter = ChannelAdapter(channels) { selectedChannel ->
            // 点击回调：获取选中频道的URL，加载页面
            val url = AppConfig.getChannelData()[selectedChannel] ?: return@ChannelAdapter
            loadUrl(url)
        }
        // 绑定适配器
        channelList.adapter = channelAdapter
        if (channels.isNotEmpty()) {
            val defaultUrl = AppConfig.getChannelData()[channels[0]] ?: return
            loadUrl(defaultUrl)
        }
        setupWebView()
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

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
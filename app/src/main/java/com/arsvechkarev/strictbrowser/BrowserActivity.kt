package com.arsvechkarev.strictbrowser

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isInvisible
import com.arsvechkarev.strictbrowser.extensions.toDuckDuckGoSearchUrl

class BrowserActivity : AppCompatActivity(R.layout.activity_browser) {

    private val handler = Handler(checkNotNull(Looper.myLooper()))

    private val editTextSearch by lazy { findViewById<EditText>(R.id.editTextSearch) }
    private val webView by lazy { findViewById<WebView>(R.id.webView) }
    private val progress by lazy { findViewById<ProgressBar>(R.id.progress) }
    private val imageMenu by lazy { findViewById<ImageView>(R.id.imageMenu) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEditTextSearch()
        setupWebView()
        setupMenuClick()
        handleUrlIntent()
    }

    private fun setupEditTextSearch() {
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearchRequest(editTextSearch.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setupWebView() {
        clearData()
        with(webView.settings) {
            @SuppressLint("SetJavaScriptEnabled")
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportZoom(true)
        }
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress > 99) {
                    scheduleProgressVisibility(isVisible = false)
                }
                progress.progress = newProgress
            }
        }
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (BLACK_LIST.any { website -> request.url.host?.contains(website) == true }) {
                    webView.loadUrl(REDIRECT_URL)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                scheduleProgressVisibility(isVisible = true)
                editTextSearch.setText(url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                scheduleProgressVisibility(isVisible = false)
            }

            override fun onPageFinished(view: WebView, url: String) {
                scheduleProgressVisibility(isVisible = false)
            }
        }
    }

    private fun setupMenuClick() {
        imageMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, imageMenu)
            popupMenu.inflate(R.menu.menu)
            popupMenu.menu.findItem(R.id.itemGoBack).isEnabled = webView.canGoBack()
            popupMenu.menu.findItem(R.id.itemGoForward).isEnabled = webView.canGoForward()
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.itemCopyUrl -> copyCurrentUrl()
                    R.id.itemRefresh -> webView.reload()
                    R.id.itemGoBack -> webView.goBack()
                    R.id.itemGoForward -> webView.goForward()
                    R.id.itemClearAll -> clearData(finishActivity = true)
                }
                false
            }
            popupMenu.show()
        }
    }

    private fun scheduleProgressVisibility(isVisible: Boolean) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ progress.isInvisible = !isVisible }, HANDLER_VISIBILITY_DELAY)
    }

    private fun handleUrlIntent() {
        handleSearchRequest(intent.dataString ?: requireNotNull(intent.getStringExtra(SEARCH_TEXT)))
    }

    private fun handleSearchRequest(urlOrText: String) {
        editTextSearch.setText(urlOrText)
        if (urlOrText.startsWith(HTTP_PREFIX)) {
            webView.loadUrl(urlOrText)
        } else {
            webView.loadUrl(urlOrText.toDuckDuckGoSearchUrl())
        }
    }

    private fun copyCurrentUrl() {
        val url = webView.url ?: return
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
            setPrimaryClip(ClipData.newPlainText(getString(R.string.label_url), url))
        }
        Toast.makeText(this, getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT)
            .show()
    }

    private fun clearData(finishActivity: Boolean = false) {
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        webView.clearMatches()
        webView.clearSslPreferences()
        if (finishActivity) {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSearchRequest(intent.dataString ?: return)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        const val HTTP_PREFIX = "http"
        const val SEARCH_TEXT = "SEARCH_TEXT"

        private const val HANDLER_VISIBILITY_DELAY = 100L
    }
}

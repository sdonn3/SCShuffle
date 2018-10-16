package com.donnelly.steve.scshuffle.features.webAuth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.network.SCService
import kotlinx.android.synthetic.main.activity_web_auth.*


class WebAuthActivity : AppCompatActivity() {
    companion object {
        const val CONNECT_DATA = "ConnectData"
        const val REDIRECT_URI = "RedirectUri"
    }

     var redirectUri: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_auth)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = AuthClient()
        val url: String? = intent.getStringExtra(CONNECT_DATA)
        redirectUri = intent.getStringExtra(REDIRECT_URI)
        url?.let{
            webView.loadUrl(url)
        } ?: finish()
    }

    inner class AuthClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url: String? = request?.url.toString()
            url?.let{
                if (it.startsWith(SCService.REDIRECT_URI)) {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    return true
                }
            }
            return false
        }
    }
}
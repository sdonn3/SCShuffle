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
import kotlinx.android.synthetic.main.activity_web_auth.*

private const val SOUNDCLOUD_CLIENT_ID = "IhtlaRd6b0rFJltJuuJANoRF5c2CQB9a"
private const val REDIRECT_URI = "scshuffle://redirect"
private const val url: String =
        "https://www.soundcloud.com/connect?" +
                "client_id=" + SOUNDCLOUD_CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=" + "code" +
                "&scope=" + "non-expiring" +
                "&display=" + "popup" +
                "&state=" + "asdf"


class WebAuthActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_auth)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = AuthClient()

        webView.loadUrl(url)
    }

    inner class AuthClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url: String? = request?.url.toString()
            return if (url?.startsWith(REDIRECT_URI) == true) {
                intent = Intent(Intent.ACTION_VIEW).also {
                    it.data = Uri.parse(url)
                }
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }
}
package com.donnelly.steve.scshuffle.features.login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.exts.launchActivity
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.features.webAuth.WebAuthActivity
import com.donnelly.steve.scshuffle.network.SCService
import com.jakewharton.rxbinding2.view.clicks
import io.fabric.sdk.android.Fabric
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject



class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var scService: SCService

    @Inject
    lateinit var session: Session

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())

        setContentView(R.layout.activity_login)
        shuffleApp.netComponent.inject(this)

        disposables += ivConnect
                .clicks()
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe{
                    launchActivity<WebAuthActivity> {
                        putExtra(WebAuthActivity.CONNECT_DATA, generateUrl())
                        putExtra(WebAuthActivity.REDIRECT_URI, SCService.REDIRECT_URI)
                        finish()
                    }
                }

        if (session.authCode != null && session.authToken != null){
            Toast.makeText(this, "Previously logged in", Toast.LENGTH_LONG).show()
        }

        val intent = intent
        intent.data?.let{
            val code = it.getQueryParameter("code")
            code?.let{ oAuthCode ->
                session.putAuthCode(code)
                disposables += scService
                        .token(oAuthCode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { tokenResponse ->
                            tokenResponse.accessToken?.let { token ->
                                session.putAuthToken(token)
                                Toast.makeText(this, "Logged in from code: $token", Toast.LENGTH_LONG).show()
                            }
                        }
                }
        }
    }

    private fun generateUrl(): String =
            "https://www.soundcloud.com/connect?" +
                    "client_id=" + SCService.SOUNDCLOUD_CLIENT_ID +
                    "&redirect_uri=" + SCService.REDIRECT_URI +
                    "&response_type=" + "code" +
                    "&scope=" + "non-expiring" +
                    "&display=" + "popup" +
                    "&state=" + "asdf"

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}

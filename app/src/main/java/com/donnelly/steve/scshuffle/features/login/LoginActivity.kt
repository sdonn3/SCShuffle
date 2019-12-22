package com.donnelly.steve.scshuffle.features.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.exts.launchActivity
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.features.player.PlayerActivity
import com.donnelly.steve.scshuffle.features.webAuth.WebAuthActivity
import com.donnelly.steve.scshuffle.network.SCService
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import reactivecircus.flowbinding.android.view.clicks
import javax.inject.Inject

class LoginActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var scService: SCService

    @Inject
    lateinit var session: Session

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (session.authCode != null && session.authToken != null){
            navigateToPlayer()
        }

        val authCode = intent?.data?.getQueryParameter("code")
        if (authCode != null) {
            lifecycleScope.launch (Dispatchers.IO) {
                val tokenResponse = scService.token(authCode)
                session.authToken = tokenResponse.accessToken
                withContext(Dispatchers.Main) {
                    navigateToPlayer()
                }
            }
        }

        lifecycleScope.launch {
            ivConnect.clicks()
                    .onEach{
                        launchActivity<WebAuthActivity> {
                            finish()
                        }
                    }
                    .launchIn(lifecycleScope)
        }
    }

    private fun navigateToPlayer() {
        launchActivity<PlayerActivity>{}
        finish()
    }
}

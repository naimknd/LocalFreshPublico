package com.example.localfresh.activitys

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.localfresh.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen_layout)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextIntent = Intent(this, LogInActivity::class.java)
            startActivity(nextIntent)
            finish()
        }, 1000)
    }
}
package com.mpcs.taskme.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.mpcs.taskme.R

class ActivitySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            // Start your app's main activity
            startActivity(Intent(this, ActivityDashboard::class.java))
            // Close the splash screen activity
            finish()
        }, 2000)

    }
}
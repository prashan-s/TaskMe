package com.mpcs.taskme

import android.app.Application

class CheckedIntentApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}
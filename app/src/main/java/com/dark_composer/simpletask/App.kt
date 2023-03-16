package com.dark_composer.simpletask

import android.app.Application
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        lateinit var resource: Resources
    }

    override fun onCreate() {
        super.onCreate()
        resource = resources
    }
}
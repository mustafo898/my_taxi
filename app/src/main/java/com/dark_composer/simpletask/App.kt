package com.dark_composer.simpletask

import android.app.Application
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        lateinit var resource: Resources
//        lateinit var sharedPref: EncryptedSharedPref
    }

    override fun onCreate() {
        super.onCreate()
        resource = resources

//        sharedPref = EncryptedSharedPref(applicationContext)
//        Log.d("OOOOOOOOOOOOOOOOOOOO", "onViewCreate: ${sharedPref.getNightMode()}")
//
//        if (sharedPref.getNightMode()) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }

//        sharedPref = EncryptedSharedPref(applicationContext)

    }
}
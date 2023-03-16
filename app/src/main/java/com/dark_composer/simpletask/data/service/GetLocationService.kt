package com.dark_composer.simpletask.data.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dark_composer.simpletask.R
import com.dark_composer.simpletask.presentation.ui.main.MainActivity
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest

class GetLocationService : Service() {

    private var locationEngine: LocationEngine? = null
    private val callback = LocationChangeListeningActivityLocationCallback(this)

    private var failureListener: ((id: String) -> Unit)? = null

    fun setFailureListener(f: (id: String) -> Unit) {
        failureListener = f
    }

    private var locationListener: ((location: Location) -> Unit)? = null

    fun setLocationListener(f: (location: Location) -> Unit) {
        locationListener = f
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        val request = LocationEngineRequest.Builder(
            DEFAULT_INTERVAL_IN_MILLISECONDS
        ).setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
        locationEngine!!.getLastLocation(callback)
    }

    @SuppressLint("LogNotTimber")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val intent = Intent("Location Updated")

        callback.setLocationListener {
            Log.d("saslfdlsdjfkdhf", "onStartCommand: ${it.latitude}  ${it.longitude}")

            sendBroadcast(
                intent.putExtra("longitude", it.longitude).putExtra("latitude", it.latitude)
            )
        }

        callback.setFailureListener {
            sendBroadcast(
                intent.putExtra("fail", it)
            )
        }

        startForeground(1, notificationToDisplayServiceInformation())
        return START_STICKY
    }


    private fun notificationToDisplayServiceInformation(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Simple Foreground Service")
            .setContentText("Explain about the service")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
            .build()
    }

    private val CHANNEL_ID = "ForegroundServiceChannel"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine!!.removeLocationUpdates(callback)
        }
    }

    companion object {
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }

    override fun onBind(intent: Intent?) = null
}
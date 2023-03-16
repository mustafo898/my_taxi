package com.dark_composer.simpletask.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.dark_composer.simpletask.R
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.ref.WeakReference

class LocationChangeListeningActivityLocationCallback constructor(context: Context) :
    LocationEngineCallback<LocationEngineResult> {
    private val activityWeakReference: WeakReference<Context>

    private var failureListener: ((id: String) -> Unit)? = null

    fun setFailureListener(f: (id: String) -> Unit) {
        failureListener = f
    }

    private var locationListener: ((location: Location) -> Unit)? = null

    fun setLocationListener(f: (location: Location) -> Unit) {
        locationListener = f
    }

    init {
        activityWeakReference = WeakReference(context)
    }

    @SuppressLint("LogNotTimber")
    override fun onSuccess(result: LocationEngineResult) {
        val activity = activityWeakReference.get()
        if (activity != null) {
            val location = result.lastLocation ?: return

            if (result.lastLocation != null) {
                locationListener?.invoke(result.lastLocation!!)

                Log.d(
                    "sdlfjsdfjds", "onSuccess: ${
                        String.format(
                            activity.getString(
                                R.string.new_location,
                                result.lastLocation!!
                                    .latitude.toString(),
                                result.lastLocation!!.longitude.toString()
                            )
                        )
                    }"
                )
            }
        }
    }

    override fun onFailure(exception: Exception) {
        val activity = activityWeakReference.get()
        if (activity != null) {
            failureListener?.invoke(exception.localizedMessage?.toString() ?: "Error")
        }
    }
}
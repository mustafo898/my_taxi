package com.dark_composer.simpletask.presentation.ui.main


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dark_composer.simpletask.R
import com.dark_composer.simpletask.data.service.GetLocationService
import com.dark_composer.simpletask.databinding.ActivityMainBinding
import com.dark_composer.simpletask.domain.model.LocationModel
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null

    private var longitude: Double? = null
    private var latitude: Double? = null

    private var isFirst = false

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        statusCheck()
        observeList()
        clickTabs()
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    private fun observeList() = lifecycleScope.launchWhenStarted {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getLocations().collectLatest {
                it.data?.let { locations ->
                    if (locations.isNotEmpty()) {
                        longitude = locations.last().lon
                        latitude = locations.last().lat
                    }
                }
            }
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            if (changeStyle()) Style.TRAFFIC_DAY else Style.TRAFFIC_NIGHT
        ) { style ->
            enableLocationComponent(style)

            myLocationClick()
            clickZoomPlus()
            clickZoomMinus()
        }
    }

    private fun changeStyle() =
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                true
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                false
            }
            else -> true
        }

    @SuppressLint("MissingPermission", "LogNotTimber", "UseCompatLoadingForDrawables")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            val locationComponent = mapboxMap!!.locationComponent

            val customLocationComponentOptions =
                LocationComponentOptions.builder(this).elevation(5f)
                    .backgroundTintColor(Color.TRANSPARENT).accuracyColor(Color.TRANSPARENT)
                    .foregroundDrawable(R.drawable.taxi).build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions).build()

            locationComponent.activateLocationComponent(locationComponentActivationOptions)

            locationComponent.isLocationComponentEnabled = true

            locationComponent.cameraMode = CameraMode.TRACKING

            locationComponent.zoomWhileTracking(18.0, 2000)

            mapboxMap!!.uiSettings.isCompassEnabled = false
            mapboxMap!!.uiSettings.isZoomGesturesEnabled = true
            mapboxMap!!.uiSettings.isAttributionEnabled = false
            mapboxMap!!.uiSettings.isLogoEnabled = false

            locationComponent.renderMode = RenderMode.NORMAL

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, GetLocationService::class.java))
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun myLocationClick() = binding.myLocation.setOnClickListener {
        if (longitude != null && latitude != null) {
            observeList()
            cameraUpdate(longitude!!, latitude!!)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        if (changeStyle()) {
            uiModeManager.nightMode = UiModeManager.MODE_NIGHT_NO
            mapboxMap!!.setStyle(Style.TRAFFIC_DAY)
        } else {
            uiModeManager.nightMode = UiModeManager.MODE_NIGHT_YES
            mapboxMap!!.setStyle(Style.TRAFFIC_NIGHT)
        }
    }

    @SuppressLint("NewApi")
    private fun clickTabs() = binding.apply {
        binding.busy.setOnClickListener {
            binding.busy.setTextColor(getColor(R.color.white))
            binding.busy.setBackgroundColor(getColor(R.color.green))
            binding.free.setBackgroundColor(getColor(R.color.white_1))
            binding.free.setTextColor(getColor(R.color.black_2))
        }

        binding.free.setOnClickListener {
            binding.free.setTextColor(getColor(R.color.white))
            binding.free.setBackgroundColor(getColor(R.color.green))
            binding.busy.setBackgroundColor(getColor(R.color.white_1))
            binding.busy.setTextColor(getColor(R.color.black_2))
        }

    }

    private fun cameraUpdate(longitude: Double, latitude: Double) {
        val position = CameraPosition.Builder().target(
            LatLng(
                latitude, longitude
            )
        ).zoom(18.0).bearing(0.0).tilt(0.0).build()

        mapboxMap!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(position), 3000
        )
    }

    private fun clickZoomMinus() = binding.minus.setOnClickListener {
        mapboxMap!!.easeCamera(
            CameraUpdateFactory.zoomOut()
        )
    }

    private fun clickZoomPlus() = binding.plus.setOnClickListener {
        mapboxMap!!.easeCamera(
            CameraUpdateFactory.zoomIn()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(
            this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap!!.getStyle { style -> enableLocationComponent(style) }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("LogNotTimber")
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                Log.d("eoeoeoeoe", "longitude: ${bundle.getDouble("longitude")}")
                Log.d("eoeoeoeoe", "latitude: ${bundle.getDouble("latitude")}")

                longitude = bundle.getDouble("longitude", 0.0)
                latitude = bundle.getDouble("latitude", 0.0)



                lifecycleScope.launch {
                    viewModel.addToList(
                        LocationModel(
                            System.currentTimeMillis(), latitude!!, longitude!!
                        )
                    )
                }

                if (!isFirst && longitude != null && latitude != null) cameraUpdate(
                    longitude!!,
                    latitude!!
                )

                isFirst = true
            }
        }
    }

    private fun statusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    override fun onStart() {
        super.onStart()
        observeList()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            receiver, IntentFilter("Location Updated")
        )
        observeList()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(receiver)
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
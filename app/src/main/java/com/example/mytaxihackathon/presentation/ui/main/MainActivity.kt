package com.example.mytaxihackathon.presentation.ui.main

import android.Manifest
import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mytaxihackathon.R
import com.example.mytaxihackathon.data.service.GetLocationService
import com.example.mytaxihackathon.databinding.ActivityMainBinding
import com.example.mytaxihackathon.domain.model.LocationModel
import com.example.mytaxihackathon.utils.extensions.showToast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModel<MainViewModel>()

    private var mapBox: MapboxMap? = null

    private var permissionsManager: PermissionsManager? = null

    private var lon = 0.0
    private var lat = 0.0

    private var isFirst = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstanceMapbox()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        statusCheck()
        observeList()
        clickTabs()

    }

    private fun getInstanceMapbox() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
    }

    private fun clickTabs() = binding.apply {
        binding.busy.setOnClickListener {
            binding.busy.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            binding.busy.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity, R.color.picked_bg
                )
            )
            binding.free.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity, R.color.white_1
                )
            )
            binding.free.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black_2))
        }

        binding.free.setOnClickListener {
            binding.free.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            binding.free.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity, R.color.picked_bg
                )
            )
            binding.busy.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity, R.color.white_1
                )
            )
            binding.busy.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black_2))
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

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapBox = mapboxMap
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.uiSettings.isAttributionEnabled = false
        mapBox?.setStyle(
            if (changeStyle()) Style.MAPBOX_STREETS else Style.DARK
        ) { style ->
            enableLocationComponent(style)
        }
        findMySelf()
        zoomIn()
        zoomOut()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(p0: MutableList<String>?) {
        this.showToast("Permission explanation for user")
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapBox?.getStyle { style -> enableLocationComponent(style) }
        } else {
            this.showToast("permission denied")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        if (changeStyle()) {
            uiModeManager.nightMode = UiModeManager.MODE_NIGHT_NO
            mapBox?.setStyle(Style.LIGHT)
        } else {
            uiModeManager.nightMode = UiModeManager.MODE_NIGHT_YES
            mapBox?.setStyle(Style.DARK)
        }
    }

    private fun findMySelf() = binding.nav.setOnClickListener {
        observeList()
        cameraUpdate(lat = lat, lon = lon)
    }

    private fun cameraUpdate(lat: Double, lon: Double) {
        val position = CameraPosition
            .Builder()
            .target(
                LatLng(
                    lat, lon
                )
            ).zoom(15.0)
            .bearing(3.0)
            .tilt(0.0)
            .build()

        mapBox?.animateCamera(
            CameraUpdateFactory.newCameraPosition(position), 3000
        )

    }

    private fun zoomIn() = binding.zoomIn.setOnClickListener {
        mapBox?.easeCamera(
            CameraUpdateFactory.zoomIn()
        )
    }

    private fun zoomOut() = binding.zoomOut.setOnClickListener {
        mapBox?.easeCamera(
            CameraUpdateFactory.zoomOut()
        )
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {

                Log.d("HJDSK", "longitude: ${bundle.getDouble("longitude")}")
                Log.d("HJDSK", "latitude: ${bundle.getDouble("latitude")}")

                lon = bundle.getDouble("longitude", 0.0)
                lat = bundle.getDouble("latitude", 0.0)

                getCity(lat = lat, lon = lon)

                lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                        viewModel.addToList(
                            LocationModel(
                                System.currentTimeMillis(), lat, lon
                            )
                        )
                    }
                }

                if (!isFirst) cameraUpdate(lat = lat, lon = lon)

                isFirst = true
            }
        }
    }

    private fun getCity(lat: Double, lon: Double) {
        val addresses: List<Address>
        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1)!!
            val address: String = addresses[0].getAddressLine(0)
            val city: String = addresses[0].locality
            Log.d("HJDSK", "onStartCommand: $city")

        } catch (e: IOException) {
            e.printStackTrace()
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
            .setCancelable(false).setPositiveButton(
                "Yes"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            val locationComponent = mapBox?.locationComponent

            val customLocationComponentOptions =
                LocationComponentOptions.builder(this@MainActivity).elevation(5f)
                    .backgroundTintColor(Color.TRANSPARENT).accuracyColor(Color.TRANSPARENT)
                    .foregroundDrawable(R.drawable.taxi).compassAnimationEnabled(true).build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions).build()

            locationComponent?.let { components ->
                components.activateLocationComponent(locationComponentActivationOptions)

                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    statusCheck()
                    return
                }
                components.isLocationComponentEnabled = true

                components.cameraMode = CameraMode.TRACKING_COMPASS

                components.zoomWhileTracking(18.0, 2000)
                components.renderMode = RenderMode.NORMAL
            }

            mapBox?.uiSettings?.isCompassEnabled = false
            mapBox?.uiSettings?.isZoomGesturesEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, GetLocationService::class.java))
            } else {
                startService(Intent(this, GetLocationService::class.java))
            }

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    private fun observeList() = lifecycleScope.launchWhenStarted {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getLocations().collectLatest {
                it.data?.let { locations ->
                    if (locations.isNotEmpty()) {
                        lon = locations.last().lon
                        lat = locations.last().lat
                    }
                }
            }
        }
    }

    /* -------------------------------- LIFECYCLE  -----------------------------------------------*/

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            receiver, IntentFilter("Location Updated")
        )
        observeList()
        binding.mapView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        val intent = Intent(this, GetLocationService::class.java)
        stopService(intent)
    }
}
package com.example.mytaxihackathon.data.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.mytaxihackathon.R
import com.example.mytaxihackathon.presentation.ui.main.MainActivity
import com.mapbox.android.core.location.*
import java.lang.ref.WeakReference

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

    override fun onCreate() {
        super.onCreate()

        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        val request = LocationEngineRequest.Builder(
            DEFAULT_INTERVAL_IN_MILLISECONDS
        ).setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
        locationEngine!!.getLastLocation(callback)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(1, notificationToDisplayServiceInformation())

        val intent = Intent("Location Updated")

        var location: Location? = null

        callback.setLocationListener {
            location = it
            sendBroadcast(
                intent.putExtra("longitude", it.longitude)
                    .putExtra("latitude", it.latitude)
            )
        }

        callback.setFailureListener {
            sendBroadcast(
                intent.putExtra("fail", it)
            )
        }

        return START_STICKY
    }


    private fun notificationToDisplayServiceInformation(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            );

        } else {
            PendingIntent.getActivity(
                this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("In order to get location service")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
            .build()
    }

    private val CHANNEL_ID = "ForegroundServiceChannel"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_HIGH
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

    inner class LocationChangeListeningActivityLocationCallback constructor(context: Context) :
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

        override fun onSuccess(result: LocationEngineResult) {
            val activity = activityWeakReference.get()
            activity?.let {
                result.lastLocation?.let {
                    locationListener?.invoke(result.lastLocation!!)
                }
            }
        }

        override fun onFailure(exception: Exception) {
            val activity = activityWeakReference.get()
            activity?.let {
                failureListener?.invoke(exception.localizedMessage?.toString() ?: "Error")
            }
        }
    }
}
package com.example.connector

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import org.jetbrains.anko.doAsync

class Service : Service() {
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            getLocationService()
        }.start()
        return super.onStartCommand(intent, flags, startId)
    }

    fun getLocationService(){
        Looper.prepare()
        while(true){
            val location = getLocation()

            class LocationModel {
                var lat = 2.2
                var lon = 2.2
            }
            val locationObject = LocationModel()
            locationObject.lat = location[0]
            locationObject.lon = location[1]
            val locationJson = Gson().toJson(locationObject)

            doAsync {
                val http = Connector()
                http.post(locationJson)
            }

//            Log.d("Service", "Lat: " + location[0].toString() + " Lon: " + location[1].toString())
            Thread.sleep(5000)
        }
        Looper.loop()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() : Array<Double> {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    locationGps = location
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}

        }

        if (hasGps || hasNetwork) {
            if (hasGps) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null)
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if(locationGps!= null && locationNetwork!= null){
                if(locationGps!!.accuracy > locationNetwork!!.accuracy){
                    return arrayOf<Double>(locationGps!!.latitude, locationGps!!.longitude)
                }else{
                    return arrayOf<Double>(locationNetwork!!.latitude, locationNetwork!!.longitude)
                }
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        return arrayOf<Double>(locationGps!!.latitude, locationGps!!.longitude)
    }

}

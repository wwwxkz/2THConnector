package com.example.connector

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
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
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wInfo = wifiManager.connectionInfo
            var macAddress = wInfo.macAddress

            val rule = Regex("[:]")
            macAddress = rule.replace(macAddress, "")

            var location = getLocation()
            location.add(macAddress)

            val json = Gson().toJson("")

            doAsync {
                val http = Connector()
                http.post(json, location)
            }

            Thread.sleep(3600000)
        }
        Looper.loop()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() : ArrayList<String> {
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
                    return arrayListOf(locationGps!!.latitude.toString(), locationGps!!.longitude.toString())
                }else{
                    return arrayListOf(locationNetwork!!.latitude.toString(), locationNetwork!!.longitude.toString())
                }
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        return arrayListOf(locationGps!!.latitude.toString(), locationGps!!.longitude.toString())
    }

}

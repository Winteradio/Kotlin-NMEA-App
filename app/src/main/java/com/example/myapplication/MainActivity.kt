package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.annotation.SuppressLint
import android.location.Location
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.widget.TextView
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity(), LocationListener, OnNmeaMessageListener {

    private var PERMISSONS_REQUEST_ACCESS_FINE_LOCATION_ID = 0x10
    private lateinit var locationManager : LocationManager
    private var datanmea = ""
    private var nmeaTag = "NMEA APP"
    private var nmeaLocation : Location? = null

    private lateinit var countView: TextView
    private lateinit var locationView : TextView
    private lateinit var nmeaView : TextView
    private var startTime : Long = System.currentTimeMillis()
    private var currentTime : Long = startTime

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countView = findViewById(R.id.textView1)
        locationView = findViewById(R.id.textView2)
        nmeaView = findViewById(R.id.textView3)

        this.initGPSSetting()

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                // 반복해서 실행할 코드
                handler.postDelayed(this, 1000) // 1초 후에 다시 실행
                updateView()
            }
        }

        handler.post(runnable)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode)
        {
            PERMISSONS_REQUEST_ACCESS_FINE_LOCATION_ID -> {
                if ( grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                {
                    this.initGPSSetting()
                }
                return
            }
        }
    }

    private fun initGPSSetting()
    {
        this.locationManager = this.getSystemService( Context.LOCATION_SERVICE ) as LocationManager

        val checkGPSEnable : Boolean = this.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )

        if ( checkGPSEnable )
        {
            Log.d( nmeaTag, javaClass.name + " : " + " GPS ON : ")

            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
            {
                Log.d( nmeaTag, javaClass.name + " : " + " Request Permission")
                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), PERMISSONS_REQUEST_ACCESS_FINE_LOCATION_ID )
            }

            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10000f, this )
            this.locationManager.addNmeaListener(this, null)
        }
        else
        {
            Log.d( nmeaTag, javaClass.name + " : " + " can't use GPS devices on this devices ")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(nmeaTag, javaClass.name + " : " + " Change Location ")
        this.nmeaLocation = location
        this.updateView()
    }

    override fun onNmeaMessage(message: String?, timestamp: Long) {
        Log.d( nmeaTag, javaClass.name + " : " + "[" + timestamp+ "]"+ message )
        sendData(message)
    }

    public fun getData() : String
    {
        return datanmea
    }

    private fun sendData(message: String?)
    {
        val data = message?.substringBefore(",")
        val title = data?.substringAfter("$")
        val gen = title?.substring(2,5)

        if ( gen == "GGA")
        {
            datanmea = ""
            datanmea += "$message\r\n"
        }

        if ( gen == "GSA" || gen == "RMC")
        {
            datanmea += "$message\r\n"
        }

        if ( gen == "RMC" )
        {
            return
        }
    }

    private fun getlocation(): Location {
        return if ( nmeaLocation != null ) {
            nmeaLocation as Location
        } else {
            getGPSLocation()
        }
    }

    private fun getGPSLocation() : Location
    {
        val isGPSEnabled : Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val hasFineLocationPermission : Int = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission : Int = this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        var location : Location? = null

        if ( hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
            && hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED )
        {
            Log.d( nmeaTag, javaClass.name + " : " + " No permission for GPS Location")
        }
        else
        {
            if ( isGPSEnabled )
            {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    Log.d(
                        nmeaTag,
                        javaClass.name + " : GPS Pos Lat ${location.latitude} Lon ${location.longitude}"
                    )
                    return location
                }
            }
        }

        val defaultLocation : Location = Location("왕십리역")
        defaultLocation.longitude = 127.0354573
        defaultLocation.latitude = 37.5611295
        return defaultLocation
    }

    public fun getNetworkLocation() : Location
    {
        val isNetworkEnabled : Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val hasFineLocationPermission : Int = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission : Int = this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        var location : Location? = null

        if ( hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
            && hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED )
        {
            Log.d( nmeaTag, javaClass.name + " : " + " No permission for GPS Location")
        }
        else
        {
            if ( isNetworkEnabled )
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    Log.d( nmeaTag, javaClass.name + " : NetWork Pos Lat ${location.latitude} Lon ${location.longitude}")
                    return location
                }
            }
        }

        val defaultLocation : Location = Location("왕십리역")
        defaultLocation.longitude = 127.0354573
        defaultLocation.latitude = 37.5611295
        return defaultLocation
    }

    @SuppressLint("SetTextI18n")
    public fun updateView()
    {
        currentTime = System.currentTimeMillis()
        countView.text = "Run Time ${ ((currentTime - startTime)/1000).toFloat() }s"
        locationView.text = "Lat ${getGPSLocation().latitude} \nLon ${getGPSLocation().longitude}"
        nmeaView.text = "NMEA $datanmea"
    }
}
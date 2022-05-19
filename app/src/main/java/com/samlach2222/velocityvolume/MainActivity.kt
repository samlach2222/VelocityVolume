package com.samlach2222.velocityvolume

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Main activity of the application
 */
class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager // Creation of GPS manager
    private lateinit var tvGpsLocation: TextView // The TextView where the speed where displayed
    private val locationPermissionCode = 2
    private val criteria = Criteria() // Geolocation criteria variable creation
    var previousLocation: Location? = null // save of the previous location to calculate speed

    /**
     * onCreate function who initialize all data we need to run the application
     */
    override fun onCreate(savedInstanceState: Bundle?) { // When the app is launched
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "VelocityVolume" // Set the title of the App on the main window
        val button: Button = findViewById(R.id.getLocation) // Get the button "Get Location"
        button.setOnClickListener {
            getLocation() // call the function when the button in clicked
        }

        // GPS criteria
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isCostAllowed = false
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        criteria.isSpeedRequired = false
    }

    /**
     * function, which is used to request the authorization to have access to the GPS and to launch a regular automatic scan to obtain the GPS coordinates.
     */
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager // Create instance of location Manager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) { // Request permissions to get GPS data
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 1f, this)
        locationManager.requestLocationUpdates(0, 0f, criteria, this, null) // Request updates of location using criteria and locationListener
    }

    /**
     * function called each time the geolocation is retrieved and allowing to calculate the speed in m/ s then in km / h to then be able to display it on the application
     * @param[location] actual geolocation of the user
     */
    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        val speed = if (location.hasSpeed()) {
            location.speed // initial value
        } else {
            previousLocation?.let { lastLocation ->
                // Convert milliseconds to seconds
                val elapsedTimeInSeconds = (location.time - lastLocation.time) / 1_000
                val distanceInMeters = lastLocation.distanceTo(location)
                // Speed in m/s
                distanceInMeters / elapsedTimeInSeconds
            } ?: 0.0
        }
        previousLocation = location

        tvGpsLocation = findViewById(R.id.textView) // TextView to display the speed in km/h
        val speedInKmH = speed.toDouble() * 18/5 // speed in Km/h
        tvGpsLocation.text = "Speed: " + speedInKmH.toInt() +  " km/h" // display the speed
    }

    /**
     * function to request permission from the user for the application to have access to GPS data
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
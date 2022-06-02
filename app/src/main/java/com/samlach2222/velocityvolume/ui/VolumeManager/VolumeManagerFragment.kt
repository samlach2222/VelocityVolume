package com.samlach2222.velocityvolume.ui.VolumeManager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentHomeBinding


class VolumeManagerFragment : Fragment() , LocationListener {
    private lateinit var locationManager: LocationManager // Creation of GPS manager
    private lateinit var tvGpsLocation: TextView // The TextView where the speed where displayed
    private val criteria = Criteria() // Geolocation criteria variable creation
    private var previousLocation: Location? = null // save of the previous location to calculate speed
    private var _binding: FragmentHomeBinding? = null

    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    init{
        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(allAreGranted) {
                Toast.makeText(activity,"You are now geolocated",Toast.LENGTH_SHORT).show()
                getLocation()
            }
            else {
                Toast.makeText(activity,"Error, please accept geolocation",Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ID of Profile
        val bundle = arguments
        //idProfile.text = bundle?.getString("id")
        bundle?.getString("id")?.let { setActivityTitle(it) }

        val button: Button = view.findViewById(R.id.getLocation) // Get the button "Get Location"
        button.setOnClickListener {
            val appPerms = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            activityResultLauncher.launch(appPerms)
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
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Create instance of location Manager
        locationManager.requestLocationUpdates(0, 0f, criteria, this, null) // Request updates of location using criteria and locationListener
//        var providerName: String? = locationManager.getBestProvider(criteria, true)
//        if (providerName != null) {
//            locationManager.requestLocationUpdates(providerName, 0,0f,this)
//        }
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

        tvGpsLocation  = requireView().findViewById(R.id.textView) // TextView to display the speed in km/h
        val speedInKmH = speed.toDouble() * 18/5 // speed in Km/h
        tvGpsLocation.text = "Speed: " + speedInKmH.toInt() +  " km/h" // display the speed
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    fun Fragment.setActivityTitle(@StringRes id: Int) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(id)
    }

    fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }
}


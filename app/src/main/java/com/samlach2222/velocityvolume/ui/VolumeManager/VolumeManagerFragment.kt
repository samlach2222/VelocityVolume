package com.samlach2222.velocityvolume.ui.VolumeManager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentVolumemanagerBinding


class VolumeManagerFragment : Fragment() , LocationListener {
    private lateinit var locationManager: LocationManager // Creation of GPS manager
    private lateinit var tvGpsLocation: TextView // The TextView where the speed where displayed
    private val criteria = Criteria() // Geolocation criteria variable creation
    private var previousLocation: Location? = null // save of the previous location to calculate speed
    private var _binding: FragmentVolumemanagerBinding? = null

    // Application states
    private var volumeManagerRunning = false
    private val arraySlidersWindowClosed = arrayOf(100, 100, 100, 100, 100)
    private val arraySlidersWindowOpened = arrayOf(100, 100, 100, 100, 100)

    // Audio Slider Controllers
    private var slider1Value = 100
    private var slider2Value = 100
    private var slider3Value = 100
    private var slider4Value = 100
    private var slider5Value = 100

    // Variables for Audio
    private var speedUnit = "km/h" // TODO : Get speedUnit from database/parameters
    private lateinit var audioManager: AudioManager // not yet initialized
    private var currentVolume: Int = -1 // not yet initialized
    private var maxVolume: Int = -1 // not yet initialized

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
        _binding = FragmentVolumemanagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ID of Profile
        val bundle = arguments
        //idProfile.text = bundle?.getString("id")
        bundle?.getString("id")?.let { setActivityTitle(it) }

        // Play Button
        val button: FloatingActionButton = view.findViewById(R.id.getLocation) // Get the button "Get Location"
        button.setOnClickListener {
            if(!volumeManagerRunning) {
                volumeManagerRunning = true
                button.setImageResource(android.R.drawable.ic_media_pause)
                val appPerms = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                activityResultLauncher.launch(appPerms)
            }
            else {
                volumeManagerRunning = false
                view.findViewById<TextView>(R.id.textView).text = ""
                stopGPS()
                button.setImageResource(android.R.drawable.ic_media_play)
            }
        }

        // Windows slider
        val windowSwitch : SwitchMaterial = view.findViewById(R.id.switchWindow)
        windowSwitch.setOnClickListener {
            val slider1: SeekBar = view.findViewById(R.id.slider_1)
            val slider2: SeekBar = view.findViewById(R.id.slider_2)
            val slider3: SeekBar = view.findViewById(R.id.slider_3)
            val slider4: SeekBar = view.findViewById(R.id.slider_4)
            val slider5: SeekBar = view.findViewById(R.id.slider_5)
            val percentage1 : TextView = view.findViewById(R.id.percentage_1)
            val percentage2 : TextView = view.findViewById(R.id.percentage_2)
            val percentage3 : TextView = view.findViewById(R.id.percentage_3)
            val percentage4 : TextView = view.findViewById(R.id.percentage_4)
            val percentage5 : TextView = view.findViewById(R.id.percentage_5)

            if(!windowSwitch.isChecked) { // window closed
                //Save current
                arraySlidersWindowOpened[0] = slider1Value
                arraySlidersWindowOpened[1] = slider2Value
                arraySlidersWindowOpened[2] = slider3Value
                arraySlidersWindowOpened[3] = slider4Value
                arraySlidersWindowOpened[4] = slider5Value

                // set current
                slider1.progress = arraySlidersWindowClosed[0]
                slider2.progress = arraySlidersWindowClosed[1]
                slider3.progress = arraySlidersWindowClosed[2]
                slider4.progress = arraySlidersWindowClosed[3]
                slider5.progress = arraySlidersWindowClosed[4]
                slider1Value = arraySlidersWindowClosed[0]
                slider2Value = arraySlidersWindowClosed[1]
                slider3Value = arraySlidersWindowClosed[2]
                slider4Value = arraySlidersWindowClosed[3]
                slider5Value = arraySlidersWindowClosed[4]
                percentage1.text = arraySlidersWindowClosed[0].toString()
                percentage2.text = arraySlidersWindowClosed[1].toString()
                percentage3.text = arraySlidersWindowClosed[2].toString()
                percentage4.text = arraySlidersWindowClosed[3].toString()
                percentage5.text = arraySlidersWindowClosed[4].toString()
            }
            else { // window opened
                //Save current
                arraySlidersWindowClosed[0] = slider1Value
                arraySlidersWindowClosed[1] = slider2Value
                arraySlidersWindowClosed[2] = slider3Value
                arraySlidersWindowClosed[3] = slider4Value
                arraySlidersWindowClosed[4] = slider5Value

                // set current
                slider1.progress = arraySlidersWindowOpened[0]
                slider2.progress = arraySlidersWindowOpened[1]
                slider3.progress = arraySlidersWindowOpened[2]
                slider4.progress = arraySlidersWindowOpened[3]
                slider5.progress = arraySlidersWindowOpened[4]
                slider1Value = arraySlidersWindowOpened[0]
                slider2Value = arraySlidersWindowOpened[1]
                slider3Value = arraySlidersWindowOpened[2]
                slider4Value = arraySlidersWindowOpened[3]
                slider5Value = arraySlidersWindowOpened[4]
                percentage1.text = arraySlidersWindowOpened[0].toString()
                percentage2.text = arraySlidersWindowOpened[1].toString()
                percentage3.text = arraySlidersWindowOpened[2].toString()
                percentage4.text = arraySlidersWindowOpened[3].toString()
                percentage5.text = arraySlidersWindowOpened[4].toString()
            }
        }

        // Sliders
        val slider1: SeekBar = view.findViewById(R.id.slider_1)
        slider1.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.percentage_1).text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                slider1Value = seek.progress
            }
        })

        val slider2: SeekBar = view.findViewById(R.id.slider_2)
        slider2.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.percentage_2).text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                slider2Value = seek.progress
            }
        })

        val slider3: SeekBar = view.findViewById(R.id.slider_3)
        slider3.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.percentage_3).text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                slider3Value = seek.progress
            }
        })

        val slider4: SeekBar = view.findViewById(R.id.slider_4)
        slider4.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.percentage_4).text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                slider4Value = seek.progress
            }
        })

        val slider5: SeekBar = view.findViewById(R.id.slider_5)
        slider5.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.percentage_5).text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                slider5Value = seek.progress
            }
        })

        // GPS criteria
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isCostAllowed = false
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        criteria.isSpeedRequired = false

        // AUDIO
        audioManager = activity?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
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

        setAudioVolumeBySpeed(speedInKmH.toInt())
    }

    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    private fun setAudioVolumeWithPercent(percent : Int) {
        //val currentVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percentBetween0And1 = percent / 100f
        val seventyVolume = (maxVolume * percentBetween0And1).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0)
    }

    private fun setAudioVolumeBySpeed(speed : Int) {
        if(speedUnit == "km/h") {
            if(speed < 20) {
                setAudioVolumeWithPercent(slider1Value)
            }
            else if(speed in 20..39) {
                setAudioVolumeWithPercent(slider2Value)
            }
            else if(speed in 40 .. 59) {
                setAudioVolumeWithPercent(slider3Value)
            }
            else if(speed in 60 .. 99) {
                setAudioVolumeWithPercent(slider4Value)
            }
            else if(speed >= 100) { // NOT ALWAYS 100
                setAudioVolumeWithPercent(slider5Value)
            }

            // Block changes when speed > 5km/h to avoid changes while driving
            if(speed >= 5) { // TODO : Popup to ask if you are the driver to know if we have to deactivate this
                val slider1: SeekBar? = view?.findViewById(R.id.slider_1)
                if (slider1 != null) {
                    slider1.isEnabled = false
                }
                val slider2: SeekBar? = view?.findViewById(R.id.slider_2)
                if (slider2 != null) {
                    slider2.isEnabled = false
                }
                val slider3: SeekBar? = view?.findViewById(R.id.slider_3)
                if (slider3 != null) {
                    slider3.isEnabled = false
                }
                val slider4: SeekBar? = view?.findViewById(R.id.slider_4)
                if (slider4 != null) {
                    slider4.isEnabled = false
                }
                val slider5: SeekBar? = view?.findViewById(R.id.slider_5)
                if (slider5 != null) {
                    slider5.isEnabled = false
                }
            }
        }
    }

    fun stopGPS() {
        locationManager.removeUpdates(this)
    }
}

// TODO : Block changes when speed > 5km/h to avoid changes while driving


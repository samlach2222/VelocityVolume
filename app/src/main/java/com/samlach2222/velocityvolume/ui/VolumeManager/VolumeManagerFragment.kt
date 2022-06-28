package com.samlach2222.velocityvolume.ui.volumemanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.hardware.display.DisplayManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.Bundle
import android.view.Display
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
import com.samlach2222.velocityvolume.DBHelper
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentVolumemanagerBinding
import java.lang.Thread.sleep
import kotlin.math.absoluteValue
import kotlin.properties.Delegates


/**
 * VolumeManager fragment class who manages all the volume, speed, GPS and interaction between the user and these functions
 */
class VolumeManagerFragment : Fragment() , LocationListener {

    private var _binding: FragmentVolumemanagerBinding? = null
    private var isUserPassenger = false
    private var isPopupDisplayed = false
    private var isUserExceedSpeedLimit = false

    //GPS needs
    private lateinit var locationManager: LocationManager // Creation of GPS manager
    private var previousLocation: Location? = null // save of the previous location to calculate speed
    private lateinit var tvGpsLocation: TextView // The TextView where the speed where displayed
    private var started = false
    private var geolocationGranted = false
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q // TODO : Display <Android Q popup or handle Android Q different GPS mode
    init{
        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(allAreGranted) {
                geolocationGranted = true
                getLocation()
            }
            else {
                Toast.makeText(activity,getString(R.string.Allow_GPS_error),Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Application states
    private var volumeManagerRunning = false
    private lateinit var profileName : String
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
    private var threadExist = false


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * function called when the view is created
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolumemanagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * function called when the view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if(::profileName.isInitialized) {
            val vvDB = DBHelper(requireContext(), null) // get DBHelper
            val windowSwitch : SwitchMaterial? = view?.findViewById(R.id.switchWindow)
            vvDB.updateProfileSwitchClose(profileName, arraySlidersWindowClosed[0], arraySlidersWindowClosed[1], arraySlidersWindowClosed[2], arraySlidersWindowClosed[3], arraySlidersWindowClosed[4])
            vvDB.updateProfileSwitchOpen(profileName, arraySlidersWindowOpened[0], arraySlidersWindowOpened[1], arraySlidersWindowOpened[2], arraySlidersWindowOpened[3], arraySlidersWindowOpened[4])
            if (windowSwitch != null) {
                vvDB.switchWindowOpenStatueChange(profileName, windowSwitch.isChecked)
            }
            vvDB.close()
        }
    }

    /**
     * function called when the view is stopped, override to call stopGPS
     */
    override fun onStop() {
        super.onStop()
        // BDD Save
        if(::profileName.isInitialized) {
            val vvDB = DBHelper(requireContext(), null) // get DBHelper
            val windowSwitch : SwitchMaterial? = view?.findViewById(R.id.switchWindow)
            vvDB.updateProfileSwitchClose(profileName, arraySlidersWindowClosed[0], arraySlidersWindowClosed[1], arraySlidersWindowClosed[2], arraySlidersWindowClosed[3], arraySlidersWindowClosed[4])
            vvDB.updateProfileSwitchOpen(profileName, arraySlidersWindowOpened[0], arraySlidersWindowOpened[1], arraySlidersWindowOpened[2], arraySlidersWindowOpened[3], arraySlidersWindowOpened[4])
            if (windowSwitch != null) {
                vvDB.switchWindowOpenStatueChange(profileName, windowSwitch.isChecked)
            }
            vvDB.close()
        }
        stopGPS()
    }

    /**
     * function called to know if geolocation is enabled in Android parameters
     * @return if GPS services are enabled
     */
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * function called when the view is in creation, initialize variables and button bindings
     * @param[view] actual view of the fragment
     * @param[savedInstanceState] saved fragment
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ID of Profile
        val bundle = arguments
        bundle?.getString("id")?.let { setActivityTitle(it) }
        if (bundle != null) {
            if(bundle.getString("id") != null) {
                profileName = bundle.getString("id")!!
            }
        }

        // Play Button
        val button: FloatingActionButton = view.findViewById(R.id.getLocation) // Get the button "Get Location"
        button.setOnClickListener {
            if(!volumeManagerRunning) {
                volumeManagerRunning = true
                val appPerms = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                activityResultLauncher.launch(appPerms)
            }
            else {
                volumeManagerRunning = false
                view.findViewById<TextView>(R.id.textView).text = ""
                view.findViewById<TextView>(R.id.textView2).text = ""
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
                percentage1.text = arraySlidersWindowClosed[0].toString() + "%"
                percentage2.text = arraySlidersWindowClosed[1].toString() + "%"
                percentage3.text = arraySlidersWindowClosed[2].toString() + "%"
                percentage4.text = arraySlidersWindowClosed[3].toString() + "%"
                percentage5.text = arraySlidersWindowClosed[4].toString() + "%"
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
                percentage1.text = arraySlidersWindowOpened[0].toString() + "%"
                percentage2.text = arraySlidersWindowOpened[1].toString() + "%"
                percentage3.text = arraySlidersWindowOpened[2].toString() + "%"
                percentage4.text = arraySlidersWindowOpened[3].toString() + "%"
                percentage5.text = arraySlidersWindowOpened[4].toString() + "%"
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
                if(!windowSwitch.isChecked){ // windows closed
                    arraySlidersWindowClosed[0] = slider1Value
                }
                else { // windows opened
                    arraySlidersWindowOpened[0] = slider1Value
                }
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
                if(!windowSwitch.isChecked){ // windows closed
                    arraySlidersWindowClosed[1] = slider2Value
                }
                else { // windows opened
                    arraySlidersWindowOpened[1] = slider2Value
                }
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
                if(!windowSwitch.isChecked){ // windows closed
                    arraySlidersWindowClosed[2] = slider3Value
                }
                else { // windows opened
                    arraySlidersWindowOpened[2] = slider3Value
                }
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
                if(!windowSwitch.isChecked){ // windows closed
                    arraySlidersWindowClosed[3] = slider4Value
                }
                else { // windows opened
                    arraySlidersWindowOpened[3] = slider4Value
                }
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
                if(!windowSwitch.isChecked){ // windows closed
                    arraySlidersWindowClosed[4] = slider5Value
                }
                else { // windows opened
                    arraySlidersWindowOpened[4] = slider5Value
                }
            }
        })

        // AUDIO
        audioManager = activity?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // get profiles from DB
        if(::profileName.isInitialized) {
            val vvDB = DBHelper(requireContext(), null) // get DBHelper
            val profile =  vvDB.getSpecificProfile(profileName)

            // moving the cursor to first position
            if(profile!!.moveToFirst()){
                val switchDBProfile = (profile.getString(profile.getColumnIndex(DBHelper.SWITCH))).toBoolean()
                val percentage1 : TextView = view.findViewById(R.id.percentage_1)
                val percentage2 : TextView = view.findViewById(R.id.percentage_2)
                val percentage3 : TextView = view.findViewById(R.id.percentage_3)
                val percentage4 : TextView = view.findViewById(R.id.percentage_4)
                val percentage5 : TextView = view.findViewById(R.id.percentage_5)

                if(!switchDBProfile) { // if windows closed
                    windowSwitch.isChecked = false
                    slider1.progress = (profile.getString(profile.getColumnIndex(DBHelper.I1C))).toInt()
                    slider2.progress = (profile.getString(profile.getColumnIndex(DBHelper.I2C))).toInt()
                    slider3.progress = (profile.getString(profile.getColumnIndex(DBHelper.I3C))).toInt()
                    slider4.progress = (profile.getString(profile.getColumnIndex(DBHelper.I4C))).toInt()
                    slider5.progress = (profile.getString(profile.getColumnIndex(DBHelper.I5C))).toInt()
                    slider1Value = (profile.getString(profile.getColumnIndex(DBHelper.I1C))).toInt()
                    slider2Value = (profile.getString(profile.getColumnIndex(DBHelper.I2C))).toInt()
                    slider3Value = (profile.getString(profile.getColumnIndex(DBHelper.I3C))).toInt()
                    slider4Value = (profile.getString(profile.getColumnIndex(DBHelper.I4C))).toInt()
                    slider5Value = (profile.getString(profile.getColumnIndex(DBHelper.I5C))).toInt()
                    percentage1.text = (profile.getString(profile.getColumnIndex(DBHelper.I1C))) + "%"
                    percentage2.text = (profile.getString(profile.getColumnIndex(DBHelper.I2C))) + "%"
                    percentage3.text = (profile.getString(profile.getColumnIndex(DBHelper.I3C))) + "%"
                    percentage4.text = (profile.getString(profile.getColumnIndex(DBHelper.I4C))) + "%"
                    percentage5.text = (profile.getString(profile.getColumnIndex(DBHelper.I5C))) + "%"
                }
                else { // if windows opened
                    windowSwitch.isChecked = true
                    slider1.progress = (profile.getString(profile.getColumnIndex(DBHelper.I1O))).toInt()
                    slider2.progress = (profile.getString(profile.getColumnIndex(DBHelper.I2O))).toInt()
                    slider3.progress = (profile.getString(profile.getColumnIndex(DBHelper.I3O))).toInt()
                    slider4.progress = (profile.getString(profile.getColumnIndex(DBHelper.I4O))).toInt()
                    slider5.progress = (profile.getString(profile.getColumnIndex(DBHelper.I5O))).toInt()
                    slider1Value = (profile.getString(profile.getColumnIndex(DBHelper.I1O))).toInt()
                    slider2Value = (profile.getString(profile.getColumnIndex(DBHelper.I2O))).toInt()
                    slider3Value = (profile.getString(profile.getColumnIndex(DBHelper.I3O))).toInt()
                    slider4Value = (profile.getString(profile.getColumnIndex(DBHelper.I4O))).toInt()
                    slider5Value = (profile.getString(profile.getColumnIndex(DBHelper.I5O))).toInt()
                    percentage1.text = (profile.getString(profile.getColumnIndex(DBHelper.I1O))) + "%"
                    percentage2.text = (profile.getString(profile.getColumnIndex(DBHelper.I2O))) + "%"
                    percentage3.text = (profile.getString(profile.getColumnIndex(DBHelper.I3O))) + "%"
                    percentage4.text = (profile.getString(profile.getColumnIndex(DBHelper.I4O))) + "%"
                    percentage5.text = (profile.getString(profile.getColumnIndex(DBHelper.I5O))) + "%"
                }
                arraySlidersWindowClosed[0] = (profile.getString(profile.getColumnIndex(DBHelper.I1C))).toInt()
                arraySlidersWindowClosed[1] = (profile.getString(profile.getColumnIndex(DBHelper.I2C))).toInt()
                arraySlidersWindowClosed[2] = (profile.getString(profile.getColumnIndex(DBHelper.I3C))).toInt()
                arraySlidersWindowClosed[3] = (profile.getString(profile.getColumnIndex(DBHelper.I4C))).toInt()
                arraySlidersWindowClosed[4] = (profile.getString(profile.getColumnIndex(DBHelper.I5C))).toInt()

                arraySlidersWindowOpened[0] = (profile.getString(profile.getColumnIndex(DBHelper.I1O))).toInt()
                arraySlidersWindowOpened[1] = (profile.getString(profile.getColumnIndex(DBHelper.I2O))).toInt()
                arraySlidersWindowOpened[2] = (profile.getString(profile.getColumnIndex(DBHelper.I3O))).toInt()
                arraySlidersWindowOpened[3] = (profile.getString(profile.getColumnIndex(DBHelper.I4O))).toInt()
                arraySlidersWindowOpened[4] = (profile.getString(profile.getColumnIndex(DBHelper.I5O))).toInt()
            }
            vvDB.close()
        }

    }

    /**
     * function, which is used to request the authorization to have access to the GPS and to launch a regular automatic scan to obtain the GPS coordinates.
     */
    private fun getLocation() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager // Create instance of location Manager
        /* We use GPS_PROVIDER because :
            - uses GPS chip on device
            - very accurate (6 meters) but high power consumption
            - works without having Internet
        */
        if(isLocationEnabled()){
            Toast.makeText(activity,getString(R.string.Allow_GPS_accept),Toast.LENGTH_SHORT).show()
            view?.findViewById<FloatingActionButton?>(R.id.getLocation)
                ?.setImageResource(android.R.drawable.ic_media_pause)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this, null) // Request updates of location using locationListener
            started = true
        }
        else {
            Toast.makeText(activity,getString(R.string.Allow_GPS_unactivated),Toast.LENGTH_SHORT).show()
        }

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
        tvGpsLocation.text = getText(R.string.current_gps_speed_prefix).toString() + " " + speedInKmH.toInt() +  " km/h" // display the speed

        setAudioVolumeBySpeed(speedInKmH.toInt())
    }

    /**
     * function called to change the display title at the top of the screen, to display profile name
     * @param[title] The profile name
     */
    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    /**
     * function called to change the device volume with [percent] parameter
     * This function call 2 threads :
     * - The first thread increase the volume smoothly / progressively
     * - The second thread increase the displayed volume percentage
     * @param[percent] the number between 0 and 100 which is the new volume percent
     */
    private fun setAudioVolumeWithPercent(percent : Int) {
        threadExist = true

        Thread {
            var currentVolumeInPercent = (currentVolume.toFloat() * (100f / maxVolume.toFloat())).toInt()
            val nbSwitchMS = 2000 // number of ms to go from currentVolume to seventyVolume
            val diffVolumePercent = (currentVolumeInPercent - percent).absoluteValue
            val speedChangePercent: Int = if (diffVolumePercent != 0) {
                nbSwitchMS / diffVolumePercent // speed to increase volume
            } else {
                0 // speed to increase volume
            }
            while (currentVolumeInPercent != percent){

                if(currentVolumeInPercent > percent) {
                    currentVolumeInPercent--
                }
                else {
                    currentVolumeInPercent++
                }

                runOnUiThread{
                    val tvVolume: TextView =
                        requireView().findViewById(R.id.textView2) // The TextView where the volume where displayed // TextView to display the speed in km/h
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() + " " + currentVolumeInPercent + "%" // display the speed
                }
                sleep(speedChangePercent.toLong()) // number of ms to wait

            }
        }.start()

        Thread { // Thread to progressively increment volume
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val percentBetween0And1 = percent / 100f
            val seventyVolume = (maxVolume * percentBetween0And1).toInt()
            val nbSwitchMS = 2000 // number of ms to go from currentVolume to seventyVolume
            val diffVolume = (seventyVolume - currentVolume).absoluteValue
            val speedChange: Int = if (diffVolume != 0) {
                nbSwitchMS / diffVolume // speed to increase volume
            } else {
                0 // speed to increase volume
            }
            // Increment loop
            while (currentVolume != seventyVolume) {

                if(currentVolume > seventyVolume) {
                    currentVolume--
                }
                else {
                    currentVolume++
                }

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                sleep(speedChange.toLong()) // number of ms to wait
            }
            threadExist = false
        }.start()
    }

    /**
     * function to allow Thread to access view objects like textview
     * @param[action] The action that will be performed
     */
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    /**
     * function to set the volume using the [speed] parameter and call popup if user use the application when he drive
     * @param[speed] Actual speed of the car
     */
    private fun setAudioVolumeBySpeed(speed : Int) { // TODO : Optimize code !
        //Volume display initialization
        if(speedUnit == "km/h") {
            val tvVolume: TextView = requireView().findViewById(R.id.textView2) // The TextView where the volume where displayed // TextView to display the speed in km/h
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if(tvVolume.text.isEmpty()) {
                if(speed < 20 && (currentVolume == (slider1Value * maxVolume / 100))) {
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() +  " $slider1Value%" // display the speed
                }
                else if(speed in 20..39 && (currentVolume == (slider2Value * maxVolume / 100))) {
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() +  " $slider2Value%" // display the speed
                }
                else if(speed in 40 .. 59 && (currentVolume == (slider3Value * maxVolume / 100))) {
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() +  " $slider3Value%" // display the speed
                }
                else if(speed in 60 .. 99 && (currentVolume == (slider4Value * maxVolume / 100))) {
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() +  " $slider4Value%" // display the speed
                }
                else if(speed >= 100 && (currentVolume == (slider4Value * maxVolume / 100))) { // NOT ALWAYS 100
                    tvVolume.text = getText(R.string.current_media_volume_prefix).toString() +  " $slider5Value%" // display the speed
                }
            }
        }

        if(speedUnit == "km/h") {
            if(!threadExist) {
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if(speed < 20 && (currentVolume != (slider1Value * maxVolume / 100))) {
                    setAudioVolumeWithPercent(slider1Value)
                }
                else if(speed in 20..39 && (currentVolume != (slider2Value * maxVolume / 100))) {
                    setAudioVolumeWithPercent(slider2Value)
                }
                else if(speed in 40 .. 59 && (currentVolume != (slider3Value * maxVolume / 100))) {
                    setAudioVolumeWithPercent(slider3Value)
                }
                else if(speed in 60 .. 99 && (currentVolume != (slider4Value * maxVolume / 100))) {
                    setAudioVolumeWithPercent(slider4Value)
                }
                else if(speed >= 100 && (currentVolume != (slider5Value * maxVolume / 100))) { // NOT ALWAYS 100
                    setAudioVolumeWithPercent(slider5Value)
                }
            }

            // Block changes when speed > 5km/h to avoid changes while driving
            if(speed >= 5) {
                isUserExceedSpeedLimit = true
            }

            if(isUserExceedSpeedLimit) {
                if(!isUserPassenger) {
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

                if(!isPopupDisplayed){ // if user drive (default state) AND the popup is not displayed
                    val dm = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                    for (display in dm.displays) {
                        if (display.state == Display.STATE_ON) { // display popup only if the screen is ON
                            displayPopupToKnowIfUserDrive()
                        }
                    }
                }
            }
        }
    }

    /**
     * function called when the fragment stop displayed and disable the GPS
     */
    private fun stopGPS() {
        if(started){
            isPopupDisplayed = false
            isUserPassenger = false
            isUserExceedSpeedLimit = false
            locationManager.removeUpdates(this)
        }
    }

    /**
     * function who display a popup who tell if the user drive or if he is a passenger to block controls if he drive.
     * This function reduce car accidents
     */
    private fun displayPopupToKnowIfUserDrive() {
        isPopupDisplayed = true

        val dialog: AlertDialog = AlertDialog.Builder(this.context)
            .setTitle("Are you a passenger in the car?")
            .setMessage("")  // toast or message ?
            .setNegativeButton("No", null)
            .setPositiveButton("Yes", null)
            .create()

        dialog.setOnShowListener {
            val yesButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val noButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            yesButton.setOnClickListener {
                isUserPassenger = true
                val slider1: SeekBar? = view?.findViewById(R.id.slider_1)
                if (slider1 != null) {
                    slider1.isEnabled = true
                }
                val slider2: SeekBar? = view?.findViewById(R.id.slider_2)
                if (slider2 != null) {
                    slider2.isEnabled = true
                }
                val slider3: SeekBar? = view?.findViewById(R.id.slider_3)
                if (slider3 != null) {
                    slider3.isEnabled = true
                }
                val slider4: SeekBar? = view?.findViewById(R.id.slider_4)
                if (slider4 != null) {
                    slider4.isEnabled = true
                }
                val slider5: SeekBar? = view?.findViewById(R.id.slider_5)
                if (slider5 != null) {
                    slider5.isEnabled = true
                }
                dialog.dismiss()
            }
            noButton.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
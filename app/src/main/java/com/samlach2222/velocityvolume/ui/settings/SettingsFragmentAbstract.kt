package com.samlach2222.velocityvolume.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.samlach2222.velocityvolume.DBHelper
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Interface for the Settings fragment class which manages the interactivity of the Settings ui
 * @author samlach2222
 * @author mahtwo
 */
abstract class SettingsFragmentAbstract : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var db: DBHelper
    private lateinit var createFileResult: ActivityResultLauncher<Intent>
    private lateinit var openFileResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Called when the view is being created
     * Initialize some texts with the current settings values and interactivity of the interactable elements
     * @author mahtwo
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[SettingsViewModel::class.java]

        setActivityTitle(getString(R.string.settingsActionBar))

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize DB variable for getting and updating settings
        db = DBHelper(this.requireContext(), null)
        val settings = db.getSettings()

        // GENERAL SETTINGS

        // Unit of measurement
        when (settings.getString(settings.getColumnIndex(DBHelper.UOM))) {
            kilometersString -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
            milesString -> binding.tvUnitValue.text = resources.getString(R.string.miles)
            else -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
        }
        val unitLayout: ConstraintLayout = binding.clUnit
        unitLayout.setOnClickListener {
            showUnitDialog()
        }

        // Night mode
        when (settings.getString(settings.getColumnIndex(DBHelper.NM))) {
            systemString -> binding.tvNightModeValue.text = resources.getString(R.string.system)
            onString -> binding.tvNightModeValue.text = resources.getString(R.string.on)
            offString -> binding.tvNightModeValue.text = resources.getString(R.string.off)
            else -> binding.tvNightModeValue.text = resources.getString(R.string.system)
        }
        val nightModeLayout: ConstraintLayout = binding.clNightMode
        nightModeLayout.setOnClickListener {
            showNightModeDialog()
        }

        // GPS sensibility
        // The + 10 and - 10 are because the slider goes from 0 to 20
        val gpsSensibilitySeekbar: SeekBar = binding.sGpsSensibility
        val gpsSensibilityFromSavedSettings = settings.getInt(settings.getColumnIndex(DBHelper.GPSD)) + 10
        gpsSensibilitySeekbar.progress = gpsSensibilityFromSavedSettings
        binding.tvGpsSensibilityValue.text = (gpsSensibilityFromSavedSettings - 10).toString()
        gpsSensibilitySeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                requireView().findViewById<TextView>(R.id.tv_gpsSensibilityValue).text = (progress - 10).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onSeekbarGPSSensibilityStopTrackingTouch(seekBar)
            }
        })

        settings.close()  // We can close the cursor

        // SAVING

        // Import settings
        val importSettingsLayout: ConstraintLayout = binding.clImportSettings
        importSettingsLayout.setOnClickListener {
            importSettings()
        }
        openFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data!!.data!!
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                var importSuccessful = true

                // Settings variables
                var unit = ""
                var nightMode = ""
                var gpsSensibility: Int? = null
                var lastSelectedProfileId: Int? = null

                // Profiles variable
                data class Profile(val ID: Int, val NAME: String, val SWITCH: Int, val I1O: Int, val I1C: Int, val I2O: Int, val I2C: Int, val I3O: Int, val I3C: Int, val I4O: Int, val I4C: Int, val I5O: Int, val I5C: Int,)
                val profiles: MutableList<Profile> = mutableListOf()

                inputStream.use { istream ->
                    val inputStreamReader = BufferedReader(InputStreamReader(istream))

                    inputStreamReader.use { isr ->
                        // SETTINGS
                        val settingsISR = isr.readLine()?.split(',')
                        if (settingsISR != null && settingsISR.size == 4) {
                            unit = settingsISR[0]
                            nightMode = settingsISR[1]
                            gpsSensibility = settingsISR[2].toIntOrNull()
                            lastSelectedProfileId = settingsISR[3].toIntOrNull()

                            if (gpsSensibility == null || lastSelectedProfileId == null) {
                                importSuccessful = false
                            }
                        } else {
                            importSuccessful = false
                        }

                        // PROFILES
                        if (importSuccessful) {
                            // TODO : Import all the profiles
                        }
                    }
                }

                if (importSuccessful) {
                    // We update the database only after checking the whole file
                    db.updateUnitOfMeasurement(unit)
                    db.updateNightMode(nightMode)
                    db.updateGPSDifference(gpsSensibility!!)
                    db.updateLatestSelectedProfileId(lastSelectedProfileId!!)
                    for (profile in profiles) {
                        // TODO : Update the database with the profiles
                    }

                    val importSuccessfulString = resources.getString(R.string.data_import_successful, getFilename(uri))
                    Toast.makeText(this.context, importSuccessfulString, Toast.LENGTH_SHORT).show()

                    requireActivity().recreate()  // Import done, we can restart the app
                } else {
                    val importFailedString = resources.getString(R.string.data_import_failed, getFilename(uri))
                    Toast.makeText(this.context, importFailedString, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Export settings
        val exportSettingsLayout: ConstraintLayout = binding.clExportSettings
        exportSettingsLayout.setOnClickListener {
            exportSettings()
        }
        createFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data!!.data!!
                val outputStream = requireContext().contentResolver.openOutputStream(uri)

                outputStream.use { os ->
                    val outputStreamWriter = os!!.writer()

                    outputStreamWriter.use { osw ->
                        // SETTINGS
                        val settingsOSW = db.getSettings()
                        // Unit of measurement
                        osw.append(settingsOSW.getString(settingsOSW.getColumnIndex(DBHelper.UOM)))
                        osw.append(',')

                        // Night mode
                        osw.append(settingsOSW.getString(settingsOSW.getColumnIndex(DBHelper.NM)))
                        osw.append(',')

                        // GPS sensibility
                        osw.append(settingsOSW.getInt(settingsOSW.getColumnIndex(DBHelper.GPSD)).toString())
                        osw.append(',')

                        // Latest Selected Profile Id
                        osw.append(settingsOSW.getInt(settingsOSW.getColumnIndex(DBHelper.LSPI)).toString())

                        settingsOSW.close()

                        // PROFILES
                        val profiles = db.getProfilesNameAndId()
                        while (profiles.moveToNext()) {
                            osw.append('\n')  // New line between settings/profiles and for each profile

                            // ID
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.ID)).toString())
                            osw.append(',')
                            // Name
                            osw.append(profiles.getString(profiles.getColumnIndex(DBHelper.NAME)))
                            osw.append(',')
                            // Switch (window closed/opened)
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.SWITCH)).toString())
                            osw.append(',')
                            // Interval 1 open
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I1O)).toString())
                            osw.append(',')
                            // Interval 1 close
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I1C)).toString())
                            osw.append(',')
                            // Interval 2 open etc....
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I2O)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I2C)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I3O)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I3C)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I4O)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I4C)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I5O)).toString())
                            osw.append(',')
                            osw.append(profiles.getInt(profiles.getColumnIndex(DBHelper.I5C)).toString())
                        }
                        profiles.close()
                    }
                }

                val exportString = resources.getString(R.string.data_exported, getFilename(uri))
                Toast.makeText(this.context, exportString, Toast.LENGTH_SHORT).show()
            }
        }

        // ABOUT

        // GitHub
        val githubLayout: ConstraintLayout = binding.clGithubPage
        githubLayout.setOnClickListener {
            githubPage()
        }

        // Report a bug
        val reportBugLayout: ConstraintLayout = binding.clReportBugPage
        reportBugLayout.setOnClickListener {
            reportBugPage()
        }

        // [DEPENDS ON FLAVOR] Rate this app
        initialiseRateAppLayout(binding)

        // [DEPENDS ON FLAVOR] Update
        val updateLayout: ConstraintLayout = binding.clUpdate
        updateLayout.setOnClickListener {
            updateApp()
        }

        return root
    }

    /**
     * function each time the settings fragment will be shown to the user
     * Change a value in database to stop redirect reboot to settings when we don't change the theme of the application
     * @author samlach2222
     */
    override fun onStart() {
        super.onStart()
        // get if reboot from settings
        val vvDB1 = DBHelper(this.requireContext(), null) // get DBHelper
        var needToChangeTo2 = false
        var needToChangeTo0 = false
        val settings = vvDB1.getSettings()
        val rebootFromSettingsForThemeChange = (settings.getString(settings.getColumnIndex(DBHelper.RFSFTC))).toInt()
        if(rebootFromSettingsForThemeChange == 1) {
            needToChangeTo2 = true
        }
        else if(rebootFromSettingsForThemeChange == 2) {
            needToChangeTo0 = true
        }
        settings.close()

        if(needToChangeTo2){
            val vvDB = DBHelper(this.requireContext(), null) // get DBHelper
            vvDB.updateRebootFromSettingsForThemeChange(2)
            vvDB.close()
        }
        else if(needToChangeTo0){
            val vvDB = DBHelper(this.requireContext(), null) // get DBHelper
            vvDB.updateRebootFromSettingsForThemeChange(0)
            vvDB.close()
        }
    }

    /**
     * Called when the view is being destroyed
     * @author samlach2222
     * @author mahtwo
     */
    override fun onDestroyView() {
        (activity as ProfileDrawerActivity).lockDrawerLayout(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onDestroyView()
        _binding = null
        db.close()
    }

    /**
     * Change the title of the activity
     * @param[title] new title of the activity
     * @author samlach2222
     * @author mahtwo
     */
    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    /**
     * Get the filename from an uri
     * @param[uri] uri to get the filename from
     */
    private fun getFilename(uri: Uri): String {
        val lastPathSegment = uri.lastPathSegment!!
        return lastPathSegment.substring(lastPathSegment.lastIndexOf('/') + 1)
    }

    /**
     * Show an alert dialog to let the user choose the unit
     * @author mahtwo
     */
    private fun showUnitDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.unit_of_measurement))
        val items = resources.getStringArray(R.array.units_of_measurement)

        val settings = db.getSettings()
        val checkedItem = when(settings.getString(settings.getColumnIndex(DBHelper.UOM))) {
            kilometersString -> 0
            milesString -> 1
            else -> 0
        }
        settings.close()

        dialogBuilder.setSingleChoiceItems(
            items, checkedItem
        ) { dialog, which ->
            when (which) {
                0 -> unitChange(kilometersString)
                1 -> unitChange(milesString)
            }
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * Apply the unit selected
     * @param[selectedValue] selected value of the user (can be "km" or "miles")
     * @author mahtwo
     */
    private fun unitChange(selectedValue: String) {
        // Update subtext with the selected unit
        val textViewUnitCurrentValue = this.requireView().findViewById<TextView>(R.id.tv_unitValue)
        when (selectedValue) {
            kilometersString -> textViewUnitCurrentValue.text = resources.getString(R.string.kilometers)
            milesString -> textViewUnitCurrentValue.text = resources.getString(R.string.miles)
        }

        db.updateUnitOfMeasurement(selectedValue)
    }

    /**
     * Show an alert dialog to let the user choose the night mode
     * @author mahtwo
     */
    private fun showNightModeDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.night_mode))
        val items = resources.getStringArray(R.array.night_modes)

        val settings = db.getSettings()
        val checkedItem = when(settings.getString(settings.getColumnIndex(DBHelper.NM))) {
            systemString -> 0
            onString -> 1
            offString -> 2
            else -> 0
        }
        settings.close()

        dialogBuilder.setSingleChoiceItems(
            items, checkedItem
        ) { dialog, which ->
            when (which) {
                0 -> nightModeChange(systemString)
                1 -> nightModeChange(onString)
                2 -> nightModeChange(offString)
            }
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * Apply the night mode selected
     * @param[selectedValue] selected value of the user (can be "system", "on" or "off")
     * @author samlach2222
     * @author mahtwo
     */
    private fun nightModeChange(selectedValue: String) {
        // Update subtext with the selected night mode and change the setOnClickListener to make it restart the app
        val textViewNightModeCurrentValue = this.requireView().findViewById<TextView>(R.id.tv_nightModeValue)
        when (selectedValue) {
            systemString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.system)
            }
            onString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.on)
            }
            offString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.off)
            }
        }
        db.updateRebootFromSettingsForThemeChange(1)
        db.updateNightMode(selectedValue)

        // change value in DB

        // Set the night mode
        val settings = db.getSettings()
        when(settings.getString(settings.getColumnIndex(DBHelper.NM))) {
            systemString -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            onString -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES)
            offString -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO)
        }

        requireActivity().recreate()
    }

    /**
     * Apply the gps sensibility selected
     * @param[seekBar] the seekbar from we take the value
     * @author mahtwo
     */
    private fun onSeekbarGPSSensibilityStopTrackingTouch(seekBar: SeekBar) {
        val selectedValue = seekBar.progress - 10

        db.updateGPSDifference(selectedValue)
    }

    /**
     * Import the settings from a file
     * @author mahtwo
     */
    private fun importSettings() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        openFileResult.launch(intent)
    }

    /**
     * Export the settings to a file
     * @author mahtwo
     */
    private fun exportSettings() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "Velocity Volume.bak")
        }

        createFileResult.launch(intent)
    }

    /**
     * Go to the github page of the project
     * @author mahtwo
     */
    private fun githubPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume"))
        startActivity(browserIntent)
    }

    /**
     * Go the github page of the project for creating a new issue
     * @author mahtwo
     */
    private fun reportBugPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume/issues/new"))
        startActivity(browserIntent)
    }

    /**
     * Overridable function to initialise the layout for rating the app
     * @param[binding] this fragment
     * @author mahtwo
     */
    protected open fun initialiseRateAppLayout(binding: FragmentSettingsBinding) {

    }

    /**
     * Overridable function to update the app, if not overridden redirects to the f-droid page of this app
     * @author mahtwo
     */
    protected open fun updateApp() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.samlach2222.velocityvolume/"))
        startActivity(browserIntent)
    }

    /**
     * const values which correspond to the database enums
     * @author mahtwo
     */
    companion object DatabaseEnums {
        const val kilometersString = "km"
        const val milesString = "miles"
        const val systemString = "system"
        const val onString = "on"
        const val offString = "off"
    }
}
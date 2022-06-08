package com.samlach2222.velocityvolume.ui.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]

        setActivityTitle(getString(R.string.settingsActionBar))

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Units of measurements
        val unitGroup: RadioGroup = binding.rgUnits
        val unitFromSavedSettings = null  // TODO : Get the unit stored in the saved settings
        when (unitFromSavedSettings) {
            "miles" -> unitGroup.check(binding.rbMile.id)
            "kilometers" -> unitGroup.check(binding.rbKm.id)
            else -> unitGroup.check(binding.rbKm.id)
        }

        // Night mode
        val spinnerNightMode: Spinner = binding.sNightMode
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.night_modes,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerNightMode.adapter = adapter
            }
        }
        // Set night mode current value
        val nightModeFromSavedSettings = null  // TODO : Get the night mode stored in the saved settings
        when (unitFromSavedSettings) {
            "auto" -> spinnerNightMode.setSelection(0)
            "on" -> spinnerNightMode.setSelection(1)
            "off" -> spinnerNightMode.setSelection(2)
            else -> spinnerNightMode.setSelection(0)
        }

        // GPS sensibility
        val numberPickerGPSSensibility: NumberPicker = binding.npGpsSensibility
        numberPickerGPSSensibility.minValue = 0
        numberPickerGPSSensibility.maxValue = 20
        numberPickerGPSSensibility.wrapSelectorWheel = false
        val numberPickerGPSSensibilityValues = Array<String>(21) {(it - 10).toString()}
        numberPickerGPSSensibility.displayedValues = numberPickerGPSSensibilityValues
        val gpsSensibilityFromSavedSettings = 10  // TODO : Get the gps sensibility stored in the saved settings
        numberPickerGPSSensibility.value = gpsSensibilityFromSavedSettings

        return root
    }

    override fun onDestroyView() {
        (activity as ProfileDrawerActivity).lockDrawerLayout(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onDestroyView()
        _binding = null
    }

    fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }
}
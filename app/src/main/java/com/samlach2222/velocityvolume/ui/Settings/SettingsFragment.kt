package com.samlach2222.velocityvolume.ui.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
        val unitGroup: RadioGroup = binding.rgUnit
        val unitFromSavedSettings = null  // TODO : Get the unit stored in the saved settings
        when (unitFromSavedSettings) {
            "miles" -> unitGroup.check(binding.rbMile.id)
            "kilometers" -> unitGroup.check(binding.rbKm.id)
            else -> unitGroup.check(binding.rbKm.id)
        }

        // Night mode
        val nightModeLayout: ConstraintLayout = binding.clNightMode
        nightModeLayout.setOnClickListener {
            showNightModeDialog()
        }
        val nightModeCurrent: TextView = binding.tvNightModeCurrent
        val nightModeFromSavedSettings = null  // TODO : Get the night mode stored in the saved settings
        when (nightModeFromSavedSettings) {
            "system" -> nightModeCurrent.text = resources.getString(R.string.system)
            "on" -> nightModeCurrent.text = resources.getString(R.string.on)
            "off" -> nightModeCurrent.text = resources.getString(R.string.off)
            else -> nightModeCurrent.text = resources.getString(R.string.system)
        }

        // GPS sensibility
        val numberPickerGPSSensibility: NumberPicker = binding.npGpsSensibility
        numberPickerGPSSensibility.minValue = 0
        numberPickerGPSSensibility.maxValue = 20
        numberPickerGPSSensibility.wrapSelectorWheel = false
        val numberPickerGPSSensibilityValues = Array(21) {(it - 10).toString()}
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

    private fun showNightModeDialog() {
        // TODO : Show an alertDialog containing the different night modes, and get the selected value
        Toast.makeText(this.context,"Night Mode DIALOG", Toast.LENGTH_SHORT).show()
    }
}
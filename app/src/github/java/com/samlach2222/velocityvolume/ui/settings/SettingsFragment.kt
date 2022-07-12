package com.samlach2222.velocityvolume.ui.settings

import android.content.Intent
import android.net.Uri

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {
    override fun updateApp() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume/releases/latest"))
        startActivity(browserIntent)
    }
}
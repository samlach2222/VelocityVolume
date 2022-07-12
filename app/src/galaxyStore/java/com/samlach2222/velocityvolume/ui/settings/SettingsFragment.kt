package com.samlach2222.velocityvolume.ui.settings

import android.content.Intent
import android.net.Uri

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {

	/**
     * Redirects to the galaxy store page of this app
     * @author mahtwo
     */
    override fun updateApp() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxystore.samsung.com/detail/com.samlach2222.velocityvolume.samsung"))
        startActivity(browserIntent)
    }
}
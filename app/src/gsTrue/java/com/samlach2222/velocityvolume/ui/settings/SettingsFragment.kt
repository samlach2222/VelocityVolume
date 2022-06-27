package com.samlach2222.velocityvolume.ui.settings

import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.play.core.review.ReviewManagerFactory
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {

    /**
     * Initialise the layout for rating the app by adding an onClickListener
     */
    override fun initialiseRateAppLayout(binding: FragmentSettingsBinding) {
        val rateAppLayout: ConstraintLayout = binding.clRateApplication
        rateAppLayout.setOnClickListener {
            rateApp()
        }
    }

    /**
     * Show a dialog to rate the app on the google play store
     */
    private fun rateApp() {
        DEBUGToast("rateApp called")

        val manager = ReviewManagerFactory.create(this.requireContext())

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewInfo?.let { manager.launchReviewFlow(requireActivity(), it) }
                flow?.addOnCompleteListener { _ ->
                    DEBUGToast("App rated")
                }
            }
        }
    }
}
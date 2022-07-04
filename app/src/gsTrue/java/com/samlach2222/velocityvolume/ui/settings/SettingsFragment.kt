package com.samlach2222.velocityvolume.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.google.android.play.core.review.ReviewManagerFactory
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {
    private lateinit var pbUpdate: ProgressBar
    private var updateInProgress: Boolean = false
    private var updateListener: InstallStateUpdatedListener? = null

    /**
     * Set the variable pbUpdate
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pbUpdate = requireView().findViewById(R.id.pb_update)
    }

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

    /**
     * Check if there's an update and starts downloading it
     */
    override fun updateApp() {
        if (!updateInProgress) {
            DEBUGToast("updateApp called")

            updateInProgress = true

            val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            // Update available
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE  && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Unhide the download progress bar in the constraint layout for the update
                    pbUpdate.visibility = ProgressBar.VISIBLE

                    // Create a listener to update the progress bar and change the setOnClickListener of the constraint layout when the update has been downloaded
                    updateListener = InstallStateUpdatedListener { state ->
                        // When the update is being downloaded
                        if (state.installStatus() == InstallStatus.DOWNLOADING) {
                            val bytesDownloaded = state.bytesDownloaded()
                            val totalBytesToDownload = state.totalBytesToDownload()

                            pbUpdate.progress = (bytesDownloaded / totalBytesToDownload).toInt()
                        }

                        // When the update has been downloaded
                        if (state.installStatus() == InstallStatus.DOWNLOADED) {
                            onUpdateDownloaded(appUpdateManager)
                        }
                    }

                    appUpdateManager.registerListener(updateListener!!)

                    // An update is available, request the update
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, APP_UPDATE_REQUEST_CODE)  // Maybe this.requireActivity() instead of this

                    DEBUGToast("update requested")
                }
                else {
                    // No update available
                    resetUpdateEnvironment()
                }
            }

            // Update canceled
            appUpdateInfoTask.addOnCanceledListener {
                resetUpdateEnvironment()
            }

            // Update failed
            appUpdateInfoTask.addOnFailureListener {
                resetUpdateEnvironment()
            }
        }
    }

    /**
     * Show a toast if the update failed
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            // If the update is cancelled or fails, allows the user to try an new update
            if (resultCode != android.app.Activity.RESULT_OK) {
                val updateFailedString = resources.getString(R.string.update_failed)

                Toast.makeText(this.context, updateFailedString, Toast.LENGTH_SHORT).show()
                //DEBUGToast("$updateFailedString Result code: $resultCode")  //Show the result code in the toast

                resetUpdateEnvironment()
            }
        }
    }

    /**
     * Reset the various variables for updating and tell the user the update is downloaded
     */
    private fun onUpdateDownloaded(appUpdateManager: AppUpdateManager) {
        // Unregister the listener
        appUpdateManager.unregisterListener(updateListener!!)

        // Hides the progress bar
        resetUpdateEnvironment()

        // Change the text on the text view
        val tvUpdate = requireView().findViewById<TextView>(R.id.tv_update)
        val updateCompletedString = resources.getString(R.string.update) + ' ' + resources.getString(R.string.update_restart_needed)
        tvUpdate.text = updateCompletedString

        // Change the setOnClickListener to make it restart the app
        val updateLayout = requireView().findViewById<ConstraintLayout>(R.id.cl_update)
        updateLayout.setOnClickListener {
            appUpdateManager.completeUpdate()
        }
    }

    /**
     * Reset the update progress bar and allows to start a new update
     */
    private fun resetUpdateEnvironment() {
        updateInProgress = false
        pbUpdate.visibility = ProgressBar.GONE
        pbUpdate.progress = 0
    }

    private companion object {
        const val APP_UPDATE_REQUEST_CODE = 484  // v*v = 22*22 = 484
    }
}
package com.samlach2222.velocityvolume.ui.settings

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.jos.AppUpdateClient
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey


/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {

    /**
     * Checks if an update is available
     * @author samlach2222
     */
    override fun updateApp() {
        // Check Update
        val client = JosApps.getAppUpdateClient(requireContext())

        if(isHmsAvailable(requireContext())){ // Check if Huawei Mobile Services available
            client.checkAppUpdate(requireContext(), UpdateCallBack(requireContext()))
        }
        else {
            Toast.makeText(requireContext(), "HMS not available", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if Huawei Mobile Services are available
     * @param[context] Context for this function
     * @return true if HMS are available, otherwise false
     * @author samlach2222
     */
    private fun isHmsAvailable(context: Context?): Boolean {
        var isAvailable = false
        if (null != context) {
            val result =
                HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) // documentation : https://developer.huawei.com/consumer/en/doc/development/hmscore-common-References/huaweiapiavailability-0000001050121134
            isAvailable = ConnectionResult.SUCCESS == result
        }
        Log.i(TAG, "isHmsAvailable: $isAvailable")
        return isAvailable
    }
}

/**
 * Class for handling the response after checking if an update is available
 * @param[context] Context for this function
 * @return handler for the response after checking if an update is available
 * @author samlach2222
 */
private class UpdateCallBack(private var context: Context) : CheckUpdateCallBack {
    val client: AppUpdateClient = JosApps.getAppUpdateClient(context)

    /**
     * function for starting the update
     * @param[intent] Intent used by the update
     * @author samlach2222
     */
    override fun onUpdateInfo(intent: Intent?) {
        intent?.let {
            // Get the status
            val status = it.getIntExtra(UpdateKey.STATUS, -1)

            // Get error code and error message from extras
            val errorCode = it.getIntExtra(UpdateKey.FAIL_CODE, -1) // if you want to handle it
            val errorMessage = it.getStringExtra(UpdateKey.FAIL_REASON) // if you want to handle it

            // Get the info as serializable
            val info = it.getSerializableExtra(UpdateKey.INFO)

            // If info is an instance of ApkUpgradeInfo, there is an update available
            if (info is ApkUpgradeInfo) {

                //Show update dialog with force update type (true/false)
                client.showUpdateDialog(context, info, false)
            }
        }
    }

    override fun onMarketInstallInfo(intent: Intent?) {

    }

    override fun onMarketStoreError(errorCode: Int) {

    }

    override fun onUpdateStoreError(errorCode: Int) {

    }
}

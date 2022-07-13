package com.samlach2222.velocityvolume.ui.settings

import com.samlach2222.velocityvolume.HmsUpdateUtil
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.huawei.hms.jos.AppUpdateClient
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey
import com.samlach2222.velocityvolume.HmsUpdateUtil.isHmsAvailable
import com.samlach2222.velocityvolume.R

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

        if((getPhoneBrand()?.lowercase() ?: String) == "huawei" || (getPhoneBrand()?.lowercase() ?: String) == "honor") { // Check if phone brand is Huawei or Honor
            if(isHmsAvailable(requireContext()) == 0){ // Check if Huawei Mobile Services available (0 for available) --> Update HMS Core if not available
                client.checkAppUpdate(requireContext(), UpdateCallBack(requireContext()))
            }
            else {
                Toast.makeText(requireContext(), getString(R.string.HMS_Not_Available), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(requireContext(), getString(R.string.Phone_Brand_Is_Not_Huawei), Toast.LENGTH_LONG).show()
        }
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
            else {
                Toast.makeText(this.context, context.getString(R.string.No_Updates_available), Toast.LENGTH_SHORT).show()
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

private fun getPhoneBrand(): String? {
    return Build.BRAND
}

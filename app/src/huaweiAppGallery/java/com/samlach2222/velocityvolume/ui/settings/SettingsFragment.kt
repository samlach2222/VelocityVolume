package com.samlach2222.velocityvolume.ui.settings

import android.content.Context
import android.content.Intent
import com.huawei.hms.jos.AppUpdateClient
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : SettingsFragmentAbstract() {
    override fun updateApp() {
        // Check Update
        val client = JosApps.getAppUpdateClient(context)
        client.checkAppUpdate(context, UpdateCallBack(requireContext()))
    }
}

private class UpdateCallBack(private var context: Context) : CheckUpdateCallBack {
    val client: AppUpdateClient = JosApps.getAppUpdateClient(context)
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

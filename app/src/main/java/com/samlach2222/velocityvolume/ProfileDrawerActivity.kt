package com.samlach2222.velocityvolume

import android.app.AlertDialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.samlach2222.velocityvolume.databinding.ActivityProfileDrawerBinding
import com.samlach2222.velocityvolume.ui.settings.SettingsFragment
import com.samlach2222.velocityvolume.ui.settings.SettingsFragmentAbstract


/**
 * ProfileDrawerActivity manages the application and the links with Fragments
 */
class ProfileDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileDrawerBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private var profileNameTable = ArrayList<String>()
    private var currentProfileId = -1

    // id
    private var currentFreeId = 0
    private var listIdMenu : HashMap<String, Int> = HashMap()

    /**
     * function to create the main Activity
     * This function initialize all necessary variables and buttons bindings
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarProfileDrawer.toolbar)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_profile_drawer) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_volumemanager, R.id.nav_homepage
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem -> run {
            //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
            when (menuItem.itemId) {
                R.id.settings -> {
                    lockDrawerLayout(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    navController.navigate(R.id.nav_settings)
                }
                R.id.add_new_profile -> addProfile()
                R.id.delete_profiles -> deleteProfiles()
                else -> {
                    currentProfileId = menuItem.itemId

                    // Save latest ProfileId
                    val vvDB = DBHelper(this, null) // get DBHelper
                    vvDB.updateLatestSelectedProfileId(currentProfileId)
                    vvDB.close()

                    val bundle = bundleOf("id" to menuItem.toString())
                    navController.navigate(R.id.nav_volumemanager, bundle)
                }
            }
        }

            //This is for maintaining the behavior of the Navigation view
            NavigationUI.onNavDestinationSelected(menuItem, navController)
            //This is for closing the drawer after acting on it
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // if there is no profile created = First launch of app
        if(navView.menu.size == 3) {
            navController.navigate(R.id.nav_homepage)
        }

        // if no profiles when launched, don't display "Delete Profiles" button
        if(listIdMenu.isEmpty()){
            findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.delete_profiles).isVisible = false
            findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.delete_profiles).isEnabled = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val vvDB = DBHelper(this, null) // get DBHelper

        // Set the night mode
        val settings = vvDB.getSettings()
        when(settings.getString(settings.getColumnIndex(DBHelper.NM))) {
            SettingsFragmentAbstract.systemString -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            SettingsFragmentAbstract.onString -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SettingsFragmentAbstract.offString -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        // get profiles from DB
        val profiles = vvDB.getProfilesNameAndId()

        // moving the cursor to first position and
        // appending value in the text view

        if(profiles!!.moveToFirst()){
            var idDBProfile = (profiles.getString(profiles.getColumnIndex(DBHelper.ID))).toInt()
            var nameDBProfile = (profiles.getString(profiles.getColumnIndex(DBHelper.NAME)))
            addProfileToList(idDBProfile, nameDBProfile)

            // moving our cursor to next
            // position and appending values
            while(profiles.moveToNext()){
                idDBProfile = (profiles.getString(profiles.getColumnIndex(DBHelper.ID))).toInt()
                nameDBProfile = (profiles.getString(profiles.getColumnIndex(DBHelper.NAME)))
                addProfileToList(idDBProfile, nameDBProfile)
            }
        }
        vvDB.close()

        // if a profile exists, go to latest
        val vvDB2 = DBHelper(this, null) // get DBHelper
        val settingsCursor = vvDB2.getSettings()
        if(settingsCursor.moveToFirst()){
            val rebootFromSettingsForThemeChange = (settingsCursor.getString(settingsCursor.getColumnIndex(DBHelper.RFSFTC))).toInt()
            val latestProfileId = (settingsCursor.getString(settingsCursor.getColumnIndex(DBHelper.LSPI))).toInt()
            if(rebootFromSettingsForThemeChange == 1){
                navController.navigate(R.id.nav_settings)
            }
            else
            {
                if(latestProfileId != -1) {
                    val menu = findViewById<NavigationView>(R.id.nav_view).menu
                    val menuItem = menu.findItem(latestProfileId)
                    val bundle = bundleOf("id" to menuItem.toString())
                    navController.navigate(R.id.nav_volumemanager, bundle)
                }
            }
        }
        vvDB2.close()
    }

    private fun addProfileToList(id: Int, name: String) {
        val menu = findViewById<NavigationView>(R.id.nav_view).menu

        val newMenuItem = menu.add(R.id.group_profiles, id, 100, name)
        listIdMenu[name] = id // add new element in the list
        currentProfileId = id
        newMenuItem.setIcon(R.drawable.ic_menu_person)

        //Redirect to the newly created profile
        val bundle = bundleOf("id" to newMenuItem.toString())
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        navController.navigate(R.id.nav_volumemanager, bundle)
        profileNameTable.add(name)
        menu.findItem(R.id.delete_profiles).isVisible = true
        menu.findItem(R.id.delete_profiles).isEnabled = true
    }

    /**
     * function bind to the delete profile button
     * This function show a popup where we can delete profiles
     */
    private fun deleteProfiles() {

        val dialogBuilder = AlertDialog.Builder(this@ProfileDrawerActivity)
        dialogBuilder.setTitle(getString(R.string.Delete_profile_popup_label))

        val profileNameToDelete = ArrayList<String>()

        val arr: Array<String?> = arrayOfNulls(profileNameTable.size)
        for (i in 0 until profileNameTable.size) {
            arr[i] = profileNameTable[i]
        }

        dialogBuilder.setMultiChoiceItems(arr, null) { _, which, isChecked ->
            // The user checked or unchecked a box
            if(isChecked){
                profileNameToDelete.add(profileNameTable[which])
            }
            else{
                profileNameToDelete.remove(profileNameTable[which])
            }
        }
        dialogBuilder.setPositiveButton(getString(R.string.Dialog_delete_label)
        ) { _, _ ->
            // The user clicked Delete
            for (i in 0 until profileNameToDelete.size) {
                profileNameTable.remove(profileNameToDelete[i])
                // delete link
                val idToDelete = listIdMenu[profileNameToDelete[i]]
                if (idToDelete != null) {
                    // REMOVE FROM DB
                    val vvDB = DBHelper(this, null) // get DBHelper
                    vvDB.deleteProfile(idToDelete)
                    vvDB.close()

                    listIdMenu.remove(profileNameToDelete[i])
                    findViewById<NavigationView>(R.id.nav_view).menu.removeItem(idToDelete)
                }


            }
            // redirect if current is delete
            if (listIdMenu.isEmpty()) { // if no profile to redirect --> Go to Home
                findNavController(R.id.nav_host_fragment_content_profile_drawer).navigate(R.id.nav_homepage)
                findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.delete_profiles).isVisible =
                    false
                findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.delete_profiles).isEnabled =
                    false
            } else if (!listIdMenu.containsValue(currentProfileId)) { // if redirection possible
                val lastName = listIdMenu.keys.last()
                val bundle = bundleOf("id" to lastName)
                findNavController(R.id.nav_host_fragment_content_profile_drawer).navigate(
                    R.id.nav_volumemanager,
                    bundle
                )
            }
        }
        dialogBuilder.setNegativeButton(getString(R.string.Dialog_cancel_label), null)

        // Create and show the alert dialog
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * function linked with the hamburger button
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * function bind to the add profile button
     * This function show a popup where we can add a profile
     */
    fun addProfile() {
        val editTextField = EditText(this@ProfileDrawerActivity)
        editTextField.isSingleLine = true
        editTextField.gravity = Gravity.CENTER_HORIZONTAL

        val dialog: AlertDialog = AlertDialog.Builder(this@ProfileDrawerActivity)
            .setTitle(getString(R.string.Add_profile_popup_title))
            .setMessage("")
            .setView(editTextField)
            .setPositiveButton(getString(R.string.Dialog_OK_label), null)
            .setNegativeButton(getString(R.string.Dialog_cancel_label), null)
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val enteredText = editTextField.text.toString().trim()
                val enteredTextLowercase = enteredText.lowercase()

                // Get if the profileName already exists
                var profileAlreadyExist = false
                if(profileNameTable.contains(enteredTextLowercase)) {
                    profileAlreadyExist = true
                }


                if (!profileAlreadyExist) {
                    dialog.setMessage("")
                    val menu = findViewById<NavigationView>(R.id.nav_view).menu

                    val newMenuItem = menu.add(R.id.group_profiles, currentFreeId, 100, enteredText)
                    listIdMenu[enteredTextLowercase] = currentFreeId // add new element in the list
                    currentProfileId = currentFreeId
                    newMenuItem.setIcon(R.drawable.ic_menu_person)
                    currentFreeId++

                    //Redirect to the newly created profile
                    val bundle = bundleOf("id" to newMenuItem.toString())
                    val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
                    navController.navigate(R.id.nav_volumemanager, bundle)
                    dialog.dismiss()
                    profileNameTable.add(enteredTextLowercase)
                    menu.findItem(R.id.delete_profiles).isVisible = true
                    menu.findItem(R.id.delete_profiles).isEnabled = true

                    // ADD TO DB
                    val vvDB = DBHelper(this, null) // get DBHelper
                    vvDB.addProfile(enteredTextLowercase)
                    vvDB.close()

                } else {
                    val color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, null)

                    val txt = getString(R.string.Profile_already_exist_part_1) + " " + editTextField.text.toString() + " " + getString(R.string.Profile_already_exist_part_3)
                    val spannable2 = SpannableString(txt) // String for which you want to change the color
                    spannable2.setSpan(
                        ForegroundColorSpan(color), 0, txt.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    dialog.setMessage(spannable2)
                }
            }
        }
        dialog.show()
    }

    /**
     * function to block the drawer menu using [lockMode]
     */
    fun lockDrawerLayout(lockMode: Int){
        drawerLayout.setDrawerLockMode(lockMode)
    }

    /**
     * quit the app if the back button is pressed and the current fragment isn't the settings
     */
    override fun onBackPressed() {
        // Don't quit if the current fragment is the settings
        if (supportFragmentManager.fragments.last().childFragmentManager.fragments.last() !is SettingsFragment) {
            // TODO : Maybe check if nothing is being done in the background (GPS, settings saved etc.) before quitting and wait until those are done
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
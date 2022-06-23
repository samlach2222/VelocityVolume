package com.samlach2222.velocityvolume

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.samlach2222.velocityvolume.databinding.ActivityProfileDrawerBinding
import com.samlach2222.velocityvolume.ui.settings.SettingsFragment

/**
 * ProfileDrawerActivity manages the application and the links with Fragments
 */
class ProfileDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileDrawerBinding
    private lateinit var drawerLayout: DrawerLayout
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
        val navController = navHostFragment.navController
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

    /**
     * function bind to the delete profile button
     * This function show a popup where we can delete profiles
     */
    private fun deleteProfiles() {

        val dialogBuilder = AlertDialog.Builder(this@ProfileDrawerActivity)
        dialogBuilder.setTitle("Choose profiles")

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
        dialogBuilder.setPositiveButton("Delete"
        ) { _, _ ->
            // The user clicked Delete
            for (i in 0 until profileNameToDelete.size) {
                profileNameTable.remove(profileNameToDelete[i])
                // delete link
                val idToDelete = listIdMenu[profileNameToDelete[i]]
                if (idToDelete != null) {
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
        dialogBuilder.setNegativeButton("Cancel", null)

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
            .setTitle("Name of the new profile")
            .setMessage("")
            .setView(editTextField)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
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
                } else {
                    dialog.setMessage("A profile named " + editTextField.text.toString() + " already exist")
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
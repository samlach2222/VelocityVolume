package com.samlach2222.velocityvolume

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.samlach2222.velocityvolume.databinding.ActivityProfileDrawerBinding


class ProfileDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileDrawerBinding
    private lateinit var drawerLayout: DrawerLayout
    private var profileNameTable = ArrayList<String>()

    // id
    private var currentFreeId = 0
    private var listIdMenu : HashMap<String, Int> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarProfileDrawer.toolbar)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
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

        if(navView.menu.size == 3) { // if there is no profile created = First launch of app
            navController.navigate(R.id.nav_homepage)
        }
    }

    private fun deleteProfiles() {
        // TODO : Redirect first available profile when we delete current profile

        val dialogBuilder = AlertDialog.Builder(this@ProfileDrawerActivity)
        dialogBuilder.setTitle("Choose profiles")

        var profileNameToDelete = ArrayList<String>()

        val arr: Array<String?> = arrayOfNulls<String>(profileNameTable.size)
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
        dialogBuilder.setPositiveButton("Delete",
            DialogInterface.OnClickListener { _, _ ->
                // The user clicked Delete
                for(i in 0 until profileNameToDelete.size) {
                    profileNameTable.remove(profileNameToDelete[i])
                    // delete link
                    val idToDelete = listIdMenu[profileNameToDelete[i]]
                    val menu = findViewById<NavigationView>(R.id.nav_view).menu
                    if (idToDelete != null) {
                        menu.removeItem(idToDelete)
                    }
                }
            })
        dialogBuilder.setNegativeButton("Cancel", null)

        // Create and show the alert dialog
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

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
                    listIdMenu[enteredText] = currentFreeId // add new element in the list
                    newMenuItem.setIcon(R.drawable.ic_menu_person)
                    currentFreeId++

                    //Redirect to the newly created profile
                    val bundle = bundleOf("id" to newMenuItem.toString())
                    val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
                    navController.navigate(R.id.nav_volumemanager, bundle)
                    dialog.dismiss()
                    profileNameTable.add(enteredTextLowercase)
                } else {
                    dialog.setMessage("A profile named " + editTextField.text.toString() + " already exist")
                }
            }
        }

        dialog.show()
    }

    fun lockDrawerLayout(lockMode: Int){
        drawerLayout.setDrawerLockMode(lockMode)
    }
}
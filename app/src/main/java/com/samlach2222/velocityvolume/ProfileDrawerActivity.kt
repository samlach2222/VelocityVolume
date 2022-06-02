package com.samlach2222.velocityvolume

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
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

        if(navView.menu.size == 2) { // if there is no profile created = First launch of app
            navController.navigate(R.id.nav_homepage)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun addProfile() {
        val editTextField = EditText(this@ProfileDrawerActivity)
        editTextField.isSingleLine = true

        val dialog: AlertDialog = AlertDialog.Builder(this@ProfileDrawerActivity)
            .setTitle("Name of the new profile")
            .setMessage("")  // toast or message ?
            .setView(editTextField)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val enteredText = editTextField.text.toString().trim()
                val enteredTextLowercase = enteredText.lowercase()

                // TODO : Check if a profile with the same name doesn't already exist
                val profileAlreadyExist: Boolean = enteredTextLowercase == "profile 1"
                    || enteredTextLowercase == "profile 2" || enteredTextLowercase == "profile 3"
                if (!profileAlreadyExist) {
                    dialog.setMessage("")
                    val menu = findViewById<NavigationView>(R.id.nav_view).menu

                    val newMenuItem = menu.add(R.id.group_profiles, Menu.NONE, 100, enteredText)
                    newMenuItem.setIcon(R.drawable.ic_menu_person)

                    //Redirect to the newly created profile
                    val bundle = bundleOf("id" to newMenuItem.toString())
                    val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
                    navController.navigate(R.id.nav_volumemanager, bundle)
                    dialog.dismiss()
                } else {
                    // toast or message ?
                    dialog.setMessage("A profile named " + editTextField.text.toString() + " already exist")
                    Toast.makeText(this@ProfileDrawerActivity,"A profile named " + editTextField.text.toString() + " already exist", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    fun lockDrawerLayout(lockMode: Int){
        drawerLayout.setDrawerLockMode(lockMode)
    }
}
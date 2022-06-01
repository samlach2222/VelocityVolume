package com.samlach2222.velocityvolume

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.*
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.samlach2222.velocityvolume.databinding.ActivityProfileDrawerBinding

class ProfileDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarProfileDrawer.toolbar)

        binding.appBarProfileDrawer.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_home, R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
            when(menuItem.itemId) {
                R.id.settings -> navController.navigate(R.id.nav_slideshow) // TODO : Il faut passer les informations vers le Fragment pour savoir ce que l'utilisateur a sélectionné
            }
            //This is for maintaining the behavior of the Navigation view
            NavigationUI.onNavDestinationSelected(menuItem, navController)
            //This is for closing the drawer after acting on it
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun ChangeProfile(item: MenuItem) {
        //TODO: Implement
        val profileName: String = item.toString()
        Toast.makeText(this@ProfileDrawerActivity,profileName, Toast.LENGTH_SHORT).show()

    fun AddProfile() {
        val editTextField = EditText(this@ProfileDrawerActivity)

        fun onDialogButtonsClick() = OnClickListener { dialogInterface: DialogInterface, clickedButton: Int ->
            when (clickedButton) {
                // Cancel
                BUTTON_NEGATIVE ->
                    dialogInterface.dismiss()
                // Ok
                BUTTON_POSITIVE -> {
                    // TODO : Implement by adding a profile
                    Toast.makeText(this@ProfileDrawerActivity,editTextField.text.toString(), Toast.LENGTH_SHORT).show()

                    dialogInterface.dismiss()
                }
            }
        }

        val dialog: AlertDialog = AlertDialog.Builder(this@ProfileDrawerActivity)
            .setTitle("Name of the new profile")
            .setView(editTextField)
            .setPositiveButton("OK", onDialogButtonsClick())
            .setNegativeButton("Cancel", onDialogButtonsClick())
            .create()
        dialog.show()
    }
}
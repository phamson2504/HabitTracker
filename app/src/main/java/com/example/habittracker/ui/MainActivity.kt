package com.example.habittracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.habittracker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.content_frame) as NavHostFragment

        navController = navHostFragment.navController

        bottomNavView.setupWithNavController(navController)
        val colorStateList = ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        bottomNavView.itemIconTintList = colorStateList
        bottomNavView.itemTextColor = colorStateList

        bottomNavView.setOnNavigationItemSelectedListener(this)

        handleIntent(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.getStringExtra("ACTION")
        if (action == "NAVIGATE_TO_FRAGMENT") {
            navController.navigate(R.id.navigation_tasks)
        }
        else if (action == "NAVIGATE_TO_CALENDAR") {
            navController.navigate(R.id.navigation_calendar)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.content_frame)
        return navController.navigateUp(drawerLayout) || super.onSupportNavigateUp()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_tasks -> {
                navController.navigate(R.id.navigation_tasks)
                return true
            }
            R.id.navigation_calendar -> {
                navController.navigate(R.id.navigation_calendar)
                return true
            }
//            R.id.navigation_progress -> {
//                navController.navigate(R.id.navigation_progress)
//                return true
//            }
        }
        return false
    }
}
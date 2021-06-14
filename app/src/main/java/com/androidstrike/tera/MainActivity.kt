package com.androidstrike.tera

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        setSupportActionBar(findViewById(R.id.tool_bar))

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController


        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.menu_yesterday, R.id.menu_today, R.id.menu_tomorrow))

        setupActionBarWithNavController(navController, appBarConfiguration)
//        setupWithNavController(navController,appBarConfiguration)


        bottomNavigationView.setupWithNavController(navController)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == R.id.action_history) {
            Toast.makeText(this, "History Clicked", Toast.LENGTH_SHORT).show()

//            actionBar!!.hide()
//            bottomNavigationView.visibility = View.GONE
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragmentContainerView, History())
//            transaction.commit()
//

        }
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

}
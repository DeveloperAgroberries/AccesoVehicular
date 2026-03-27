package com.AgroberriesMX.accesovehicular.ui.home

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.AgroberriesMX.accesovehicular.R
import com.AgroberriesMX.accesovehicular.databinding.ActivityMainBinding
import com.AgroberriesMX.accesovehicular.ui.login.LoginActivity
import com.AgroberriesMX.accesovehicular.ui.privacypolicy.PrivacyPolicyActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var persistentPrefs: SharedPreferences
    private lateinit var sessionPrefs: SharedPreferences

    companion object {
        private const val PERSISTENT_PREFERENCES_KEY = "persistent_prefs"
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val POLICIES_SHOWN_KEY = "policies_shown"
        private const val LOGGED_IN_KEY = "logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        appRun()
        initListener()
        initNavigation()
    }

    private fun appRun() {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        val policiesShown = persistentPrefs.getBoolean(POLICIES_SHOWN_KEY, false)
        val loggedIn = sessionPrefs.getBoolean(LOGGED_IN_KEY, false)

        when {
            !policiesShown -> {
                val intent = Intent(this, PrivacyPolicyActivity::class.java)
                startActivity(intent)
                finish()
            }

            !loggedIn -> {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackFunction()
            }
        })
    }

    private fun initNavigation() {
        setSupportActionBar(binding.toolbar)

        val navHost =
            supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment
        navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navDriverData,
                R.id.navListOfRecords,
                R.id.navSync,
                R.id.navAbout,
                R.id.navRondin //Ricardo Dimas - 15/06/2026 Rondines
            ), binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navLogout -> {
                    showExitConfirmationData()
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun onBackFunction() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            showExitConfirmationData()
        }
    }

    private fun showExitConfirmationData() {
        AlertDialog.Builder(this)
            .setMessage("Quieres salir de la aplicacion?")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog, _ ->
                dialog.dismiss()
                handleLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun handleLogout() {
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        val editor = sessionPrefs.edit()
        editor.clear()
        editor.apply()

        navigateToLogin()
    }
}
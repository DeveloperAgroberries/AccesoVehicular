package com.AgroberriesMX.accesovehicular.ui.privacypolicy

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.AgroberriesMX.accesovehicular.databinding.ActivityPrivacyPolicyBinding
import com.AgroberriesMX.accesovehicular.ui.home.MainActivity

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var persistentPrefs: SharedPreferences

    companion object{
        private const val PERSISTENT_PREFERENCES_KEY = "persistent_prefs"
        private const val POLICIES_SHOWN_KEY = "policies_shown"
        private const val PERMISSIONS_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    //TODO: Add the synchronize catalogs method
    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.btnPermissions.setOnClickListener{
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        val permissionsToRequest = mutableListOf<String>()

        if(ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED){
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            navigateToMain()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSIONS_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults.all {it == PackageManager.PERMISSION_GRANTED}){
                    navigateToMain()
                } else {
                    finish()
                }
            }
        }
    }

    private fun navigateToMain(){
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        with(persistentPrefs.edit()){
            putBoolean(POLICIES_SHOWN_KEY, true)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
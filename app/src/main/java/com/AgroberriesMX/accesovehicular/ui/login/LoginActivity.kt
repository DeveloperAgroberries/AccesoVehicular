package com.AgroberriesMX.accesovehicular.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.AgroberriesMX.accesovehicular.databinding.ActivityLoginBinding
import com.AgroberriesMX.accesovehicular.ui.home.MainActivity
import com.AgroberriesMX.accesovehicular.ui.sync.SyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var sessionPrefs: SharedPreferences
    private lateinit var persistentPrefs: SharedPreferences
    private val loginViewModel: LoginViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    companion object{
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val PERSISTENT_PREFERENCIES_KEY = "persistent_prefs"
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
        private const val LOGGED_USER_KEY = "logged_user"
        private const val LOGGED_IN_KEY = "logged_in"
        private const val SYNCHRONIZED_CATALOGS_KEY = "synchronized_catalogs"
        private const val TAG = "LoginActivity"
        private const val REMIND_USERNAME_KEY = "Username"
        private const val REMIND_PASSWORD_KEY = "Password"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        observeViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initUI() {
        initListeners()
        loadUserDataIfExists()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListeners() {
        binding.btnLogin.setOnClickListener{
            val user = binding.etUser.text.toString().uppercase().trim()
            val password = binding.etPassword.text.toString().trim()

            if(user != "" || password != ""){
                lifecycleScope.launch {
                    if(binding.cbReminder.isChecked){
                        remindUser(user, password)
                    } else {
                        clearUserData()
                    }
                    loginViewModel.login(user, password, "1", "")
                }
            } else {
                Toast.makeText(this,
                    "El usuario o la contraseña estan vacios, vuelve a intentarlo, por favor.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadUserDataIfExists() {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCIES_KEY, MODE_PRIVATE)
        val savedUsername = persistentPrefs.getString(REMIND_USERNAME_KEY, null)
        val savedPassword = persistentPrefs.getString(REMIND_PASSWORD_KEY, null)

        if(savedUsername != null && savedPassword != null){
            binding.etUser.setText(savedUsername)
            binding.etPassword.setText(savedPassword)
            binding.cbReminder.isChecked = true
        }
    }

    private fun observeViewModel() {
        loginViewModel.state.observe(this, Observer{state ->
            when (state){
                 is LoginState.Waiting -> {
                     binding.pb.visibility = View.GONE
                 }

                is LoginState.Loading -> {
                    binding.pb.visibility = View.VISIBLE
                    binding.etUser.isEnabled = false
                    binding.etPassword.isEnabled = false
                    binding.btnLogin.isEnabled = false
                }

                /*is LoginState.Success -> {
                    binding.pb.visibility = View.GONE

                    if(state.success?.token != null){
                        val token = state.success.token
                        savedToken(token)
                        savedUser(binding.etUser.text.toString().trim())
                        lifecycleScope.launch {
                            try{
                                synchronizeCatalogs()
                                navigateToMainActivity()
                            }catch (e: Exception){
                                Toast.makeText(applicationContext, "Error en la sincronizacion: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        savedUser(binding.etUser.text.toString().trim())
                        Toast.makeText(applicationContext, "Acceso local exitoso", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                }*/
                is LoginState.Success -> {
                    binding.pb.visibility = View.GONE
                    binding.etUser.isEnabled = true // Habilitar campos y botón al finalizar carga
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true

                    // --- LÓGICA CLAVE PARA EL MENSAJE ONLINE/OFFLINE ---
                    if (state.isLocal) {
                        Toast.makeText(applicationContext, "Inicio de sesión: ¡Acceso offline!", Toast.LENGTH_LONG).show()
                        // Aquí no necesitas guardar el token ni sincronizar catálogos si es offline
                        // (el token ya debería estar guardado de una sesión online anterior)
                        // Si savedUser() es solo para recordar el nombre de usuario, puedes mantenerlo aquí
                        savedUser(binding.etUser.text.toString().trim())
                        navigateToMainActivity()
                    } else {
                        // Acceso online
                        val token = state.success?.token
                        if (token != null) {
                            Toast.makeText(applicationContext, "Inicio de sesión: ¡Acceso online!", Toast.LENGTH_LONG).show()
                            savedToken(token) // Guarda el token del servidor
                            savedUser(binding.etUser.text.toString().trim())
                            lifecycleScope.launch {
                                try {
                                    synchronizeCatalogs() // Intenta sincronizar porque hay conexión
                                    navigateToMainActivity()
                                } catch (e: Exception) {
                                    Toast.makeText(applicationContext, "Error en la sincronización: ${e.message}", Toast.LENGTH_LONG).show()
                                    // A pesar del error de sincronización, el usuario ya inició sesión
                                    navigateToMainActivity()
                                }
                            }
                        } else {
                            // Esto no debería ocurrir si el ViewModel maneja correctamente 'isLocal'
                            Toast.makeText(applicationContext, "Error: Token nulo en acceso online exitoso.", Toast.LENGTH_LONG).show()
                            // Considera qué hacer aquí: ¿volver a login, mostrar error crítico?
                            navigateToMainActivity() // Por ahora, navega para no bloquear al usuario
                        }
                    }
                    // --- FIN LÓGICA CLAVE ---
                }

                is LoginState.Error -> {
                    binding.pb.visibility = View.GONE
                    binding.etUser.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "${state.message}", Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        })
    }

    private fun savedToken(token: String){
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        with(sessionPrefs.edit()){
            putString(PRIVATE_ACCESS_TOKEN_KEY, "Bearer ${token}")
            apply()
        }
    }

    private fun savedUser(user: String){
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        with(sessionPrefs.edit()){
            putString(LOGGED_USER_KEY, user)
            apply()
        }
    }

    private fun remindUser(usr: String, pwd: String){
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCIES_KEY, MODE_PRIVATE)
        with(persistentPrefs.edit()){
            putString(REMIND_USERNAME_KEY, usr)
            putString(REMIND_PASSWORD_KEY, pwd)
            apply()
        }
    }

    private fun clearUserData(){
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCIES_KEY, MODE_PRIVATE)
        with(persistentPrefs.edit()){
            remove(REMIND_USERNAME_KEY)
            remove(REMIND_PASSWORD_KEY)
            apply()
        }
    }

    private suspend fun synchronizeCatalogs(){
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCIES_KEY, MODE_PRIVATE)

        if(!persistentPrefs.getBoolean(SYNCHRONIZED_CATALOGS_KEY, false)){
            sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
            val token = sessionPrefs.getString(PRIVATE_ACCESS_TOKEN_KEY, null)
            if(token != null){
                withContext(Dispatchers.IO){
                    try{
                        syncViewModel.sync(token)

                        with(persistentPrefs.edit()){
                            putBoolean(SYNCHRONIZED_CATALOGS_KEY, true)
                            apply()
                        }
                    }catch (e: Exception){
                        Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Hubo un error obteninedo e insertando los datos en la base de datos: ${e.message}")
                    }
                }
            }else{
                Toast.makeText(applicationContext, "No se encontró el token de acceso.", Toast.LENGTH_LONG).show()
                Log.e(TAG, "No se encontró el token de acceso.")
            }
        } else {
            Log.i(TAG, "Los catálogos ya han sido sincronizados anteriormente.")
        }
    }

    private fun navigateToMainActivity(){
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        with(sessionPrefs.edit()){
            putBoolean(LOGGED_IN_KEY, true)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
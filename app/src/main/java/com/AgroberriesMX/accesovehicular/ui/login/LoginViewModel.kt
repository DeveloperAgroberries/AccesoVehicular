package com.AgroberriesMX.accesovehicular.ui.login

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AgroberriesMX.accesovehicular.data.local.AccesoVehicularLocalDBService
import com.AgroberriesMX.accesovehicular.data.network.request.LoginRequest
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.TokenModel
import com.AgroberriesMX.accesovehicular.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getLoginUseCase: LoginUseCase,
    private val localDBService: AccesoVehicularLocalDBService,
    private val application: Application
    ) : ViewModel() {
    private var _state = MutableLiveData<LoginState>(LoginState.Waiting)
    val state: LiveData<LoginState> = _state

    private val context: Context get() = application.applicationContext
    private var authenticatedUser: LoginModel? = null

    @RequiresApi(Build.VERSION_CODES.M)
    fun login(userId: String, password: String, activeUser: String, creatorId: String){
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                if(isInternetAvailable(context)){
                    val loginRequest = LoginRequest(userId, password, activeUser, creatorId)
                    val response = getLoginUseCase(loginRequest)
                    if(response != null){
                        //_state.value = LoginState.Success(response)
                        val md5Hash = password.toMD5() // Hash de la contraseña ingresada

                        // Como TokenModel solo tiene 'token', no podemos obtener 'vNombreUsu' de 'response' directamente.
                        // Usaremos userId como nombre de usuario para el LoginModel local.
                        // Para controlLog, usamos 0L asumiendo que es autogenerado por Room o un valor por defecto.
                        val controlLogValue = 0L // Si controlLog es @PrimaryKey(autoGenerate = true) en LoginModel (Entidad Room)
                        val nombreUsuarioParaGuardar = userId // O alguna otra lógica si puedes derivar un nombre

                        val userToSaveLocally = LoginModel(
                            controlLog = controlLogValue,
                            vNombreUsu = nombreUsuarioParaGuardar, // Usamos el userId o un valor por defecto
                            cCodigoUsu = userId,                   // Este es el userId que ingresó el usuario
                            vPasswordUsu = md5Hash                 // Contraseña hasheada para verificación offline
                        )

                        localDBService.saveUser(userToSaveLocally) // Esto ahora ya no dará error de constructor si LoginModel se llenó bien

                        _state.value = LoginState.Success(response)
                    } else {
                        _state.value = LoginState.Error("Fallo el acceso")
                    }
                } else {
                    val md5Hash = password.toMD5()
                    val user = localDBService.getUserByCodeAndPassword(userId, md5Hash)
                    if (user != null) {
                        authenticatedUser = user
                        _state.value = LoginState.Success(null, isLocal = true)
                    } else {
                        _state.value = LoginState.Error("Credenciales incorrectas")
                    }
                }
            } catch (e: Exception){
                _state.value = LoginState.Error(e.message ?: "A ocurrido un error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        val isAvailable = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        Log.d("NetworkCheck", "Internet available: $isAvailable")
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun String.toMD5(): String {
        // Crear una instancia de MessageDigest para MD5
        val digest = MessageDigest.getInstance("MD5")
        // Calcular el hash y convertirlo a un arreglo de bytes
        val hashBytes = digest.digest(this.toByteArray())
        // Convertir los bytes a un formato hexadecimal
        return hashBytes.joinToString("") { String.format("%02x", it) }
    }
}
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
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
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
    private val recordsRepository: RecordsRepository, // <-- AGREGAR ESTA LÍNEA
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
                        val md5Hash = password.toMD5()

                        val controlLogValue = 0L
                        val nombreUsuarioParaGuardar = userId

                        val userToSaveLocally = LoginModel(
                            controlLog = controlLogValue,
                            vNombreUsu = nombreUsuarioParaGuardar,
                            cCodigoUsu = userId,
                            vPasswordUsu = md5Hash
                        )

                        localDBService.saveUser(userToSaveLocally)

                        // --- SINCRONIZACIÓN AUTOMÁTICA DE VEHÍCULOS DE LA EMPRESA ---
                        try {
                            Log.d("DEBUG_SQLITE", "Iniciando descarga de catálogo de vehículos...")
                            val vehicles = recordsRepository.getCompanyVehicles()
                            Log.d("DEBUG_SQLITE", "Catálogo obtenido. Registros descargados: ${vehicles?.size ?: 0}")
                        } catch (e: Exception) {
                            Log.e("DEBUG_SQLITE", "Error al sincronizar vehículos: ${e.message}")
                        }
                        // -------------------------------------------------------------

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
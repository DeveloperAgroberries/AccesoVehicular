package com.AgroberriesMX.accesovehicular.ui.sync

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AgroberriesMX.accesovehicular.data.local.AccesoVehicularLocalDBService
import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.usecase.LoginsUseCase
import com.AgroberriesMX.accesovehicular.domain.usecase.UploadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import android.util.Log
import com.AgroberriesMX.accesovehicular.data.network.request.RondinListWrapper
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import com.AgroberriesMX.accesovehicular.domain.usecase.UploadUseCaseRondin//RICARDO DIMAS

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val loginsUseCase: LoginsUseCase,
    private val uploadUseCase: UploadUseCase,
    private val UploadUseCaseRondin: UploadUseCaseRondin,
    private val databaseService: AccesoVehicularLocalDBService,
    private val repository: RecordsRepository
    ):
    ViewModel(){
    private var _state = MutableLiveData<SyncState>(SyncState.Waiting)
    val state: LiveData<SyncState> get() = _state

    private var _pendingRecords = MutableLiveData<List<RecordModel>>()
    val pendingRecords: LiveData<List<RecordModel>> get() = _pendingRecords

    //RICARDO DIMAS
    private var _pendingRondines = MutableLiveData<List<RondinModel>>()
    val pendingRondines: LiveData<List<RondinModel>> get() = _pendingRondines

    fun sync(token: String){
        viewModelScope.launch {
            _state.value = SyncState.Loading
            try {
                val syncRequest = SyncRequest(token, data = emptyList())
                val response: List<LoginModel>? = loginsUseCase(syncRequest)
                if(!response.isNullOrEmpty() && response.isNotEmpty()){
                    databaseService.deleteAllUsers()
                    val insertResults = databaseService.insertUsers(response)
                    if (insertResults.all { it != null }) {
                        _state.value = SyncState.Success(response)
                        _state.value = SyncState.Error("Sincronizado correctamente.")
                    } else {
                        _state.value = SyncState.Error("Fallo al insertar algunos usuarios.")
                    }
                } else {
                    _state.value = SyncState.Error("Fallo la sincronizacion de catalogos")
                }
            } catch (e:Exception) {
                _state.value = SyncState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun upload(){
        viewModelScope.launch{
            _state.value = SyncState.Loading
            try{
                val localData = repository.getUnsynchronizedRecords()

                if (localData != null) {
                    val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val hourFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a") // Formato para AM/PM
                    val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Formato de fecha

                    val transformedData: List<FormattedRecordsModel> = localData.map { register ->
                        // Parsear las cadenas a LocalDate
                        val ingresoDate = LocalDate.parse(register.dIngresoInv, inputDateFormatter)

                        // Parsear las horas a LocalTime
                        val hrIngresoTime = LocalTime.parse(register.dHringresoInv, hourFormatter)
                        //val hrSalidaTime = LocalTime.parse(register.dHrsalidaInv, hourFormatter)

                        // Guardar registro aunque no tenga fecha de salida, generar automaticamente Ricardo Dimas - 13/06/2025
                        // Lógica robusta para la hora de salida
                        val hrSalidaTime = when {
                            // Si es nulo, vacío o contiene la palabra "Now" por error
                            register.dHrsalidaInv.isNullOrEmpty() || register.dHrsalidaInv.equals("Now", ignoreCase = true) -> {
                                LocalTime.now()
                            }
                            else -> {
                                try {
                                    LocalTime.parse(register.dHrsalidaInv, hourFormatter)
                                } catch (e: Exception) {
                                    // Si falla el parseo por cualquier otro formato extraño, usa la hora actual
                                    LocalTime.now()
                                }
                            }
                        }

                        val cMovimientoInv = if (register.dHrsalidaInv.isNullOrEmpty() || register.dHrsalidaInv.equals("Now", ignoreCase = true)) {
                            "E"
                        } else {
                            "S"
                        }

                        FormattedRecordsModel(
                            dIngresoInv = ingresoDate.format(dayFormatter) + "T" + hrIngresoTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            dHringresoInv = ingresoDate.format(dayFormatter) + "T" + hrIngresoTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            dHrsalidaInv = ingresoDate.format(dayFormatter) + "T" + hrSalidaTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            vNombrechofInv = register.vNombrechofInv,
                            vAcompanianteInv = register.vAcompanianteInv,
                            vEmpresaInv = register.vEmpresaInv,
                            cPlacaInv = register.cPlacaInv,
                            vMotivoInv = register.vMotivoInv,
                            cCodigoUsu = register.cCodigoUsu,
                            dCreacionInv = LocalDateTime.now().toString(),
                            cMovimientoInv = cMovimientoInv
                        )
                    }
                    val response = uploadUseCase(transformedData)

                    if(response == "Ok") {
                        localData.forEach { record ->
                            record.isSynced = 1 // Marcar como sincronizado
                            repository.updateVehicle(record)
                        }
                        _state.value = SyncState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRecords()
                    } else {
                        if(response == "Unauthorized") {
                            _state.value = SyncState.Error("No cuentas con un token para enviar los datos, cierra e inicia sesion y vuelve a intentarlo, por favor.")
                        } else {
                            _state.value = SyncState.Error(response.toString())
                        }
                    }
                } else {
                    _state.value = SyncState.Error("No hay nada que enviar");
                }
            }catch (e: Exception){
                _state.value = SyncState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    fun loadPendingRecords() {
        viewModelScope.launch {
            val records = repository.listUnsynchronizedRecords()
            _pendingRecords.value = records ?: emptyList()
        }
    }

    //RICARDO DIMAS - Rondines 23/06/2025
    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadRondines(){
        viewModelScope.launch{
            _state.value = SyncState.Loading
            try{
                val localData = repository.listUnsynchronizedRondines()
                //Log.d("SyncViewModel", "Contenido de localData (desde DB): $localData") // <-- ¡Este log es CLAVE!

                if (localData != null) {
                    val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val hourFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a") // Formato para AM/PM
                    //val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Formato de fecha

                    val inputDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a") // Incluye fecha, hora, segundos y AM/PM
                    val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // <--- ¡AQUÍ ESTÁ!


                    val transformedData: List<FormattedRondinModel> = localData.map { register ->
                        // Parsear las cadenas a LocalDate
                        // Parsear la cadena completa de fecha y hora a LocalDateTime
                        val fechaRonDateTime = LocalDateTime.parse(register.fechaRon, inputDateTimeFormatter)

                        // Formatear la fecha para la API (yyyy-MM-dd)
                        val formattedDate = fechaRonDateTime.format(dayFormatter)
                        // Formatear la hora para la API (HH:mm:ss)
                        val formattedTime = fechaRonDateTime.format(outputTimeFormatter)

                        FormattedRondinModel(
                            dFecha = "${formattedDate}T${formattedTime}",
                            cCodigoUsu = register.codigoUsuRon,
                            vLatGps = register.latGpsRon,
                            vLonGps = register.longGpsRon,
                            vNomUbicacion = register.nomUbicacionRon,
                            cUsumod = register.usuModRon
                        )
                    }

                    // --- ¡Paso CRÍTICO: CREAR EL WRAPPER AQUÍ! ---
                    //val rondinWrapper = RondinListWrapper(objetos = transformedData)
                    //Log.d("SyncViewModel", "Contenido de rondinWrapper (desde DB): $rondinWrapper") // <-- ¡Este log es CLAVE!

                    val response = UploadUseCaseRondin(transformedData)
                    //Log.d("SyncViewModel", "Contenido de transformedData (desde DB): $transformedData") // <-- ¡Este log es CLAVE!

                    if(response == "Ok") {
                        localData.forEach { rondin ->
                            rondin.isSynced = 1 //Marcar como sincronizado
                            repository.updateRondines(rondin)
                        }
                        _state.value = SyncState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRondines()
                    } else {
                        if(response == "Unauthorized") {
                            _state.value = SyncState.Error("No cuentas con un token para enviar los datos, cierra e inicia sesion y vuelve a intentarlo, por favor.")
                        } else {
                            _state.value = SyncState.Error(response.toString())
                        }
                    }
                } else {
                    _state.value = SyncState.Error("No hay nada que enviar");
                }
            }catch (e: Exception){
                _state.value = SyncState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    fun loadPendingRondines() {
        viewModelScope.launch {
            val rondines = repository.listUnsynchronizedRondines()
            _pendingRondines.value = rondines ?: emptyList()
        }
    }

    fun clearState() {
        _state.value = SyncState.Waiting // O puedes usar un estado inicial si lo prefieres
    }
}
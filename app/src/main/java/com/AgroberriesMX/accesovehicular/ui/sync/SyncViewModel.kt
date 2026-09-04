package com.AgroberriesMX.accesovehicular.ui.sync

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AgroberriesMX.accesovehicular.data.local.AccesoVehicularLocalDBService
import com.AgroberriesMX.accesovehicular.data.local.DatabaseHelper
import com.AgroberriesMX.accesovehicular.data.mailer.AutoMailer
import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import com.AgroberriesMX.accesovehicular.domain.usecase.GetCompanyVehiclesUseCase
import com.AgroberriesMX.accesovehicular.domain.usecase.LoginsUseCase
import com.AgroberriesMX.accesovehicular.domain.usecase.UploadUseCase
import com.AgroberriesMX.accesovehicular.domain.usecase.UploadUseCaseRondin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val loginsUseCase: LoginsUseCase,
    private val companyVehiclesUseCase: GetCompanyVehiclesUseCase,
    private val uploadUseCase: UploadUseCase,
    private val UploadUseCaseRondin: UploadUseCaseRondin,
    private val databaseService: AccesoVehicularLocalDBService,
    private val repository: RecordsRepository,
    private val dbHelper: DatabaseHelper,
    private val autoMailer: AutoMailer
) : ViewModel() {

    private var _state = MutableLiveData<SyncState>(SyncState.Waiting)
    val state: LiveData<SyncState> get() = _state

    private var _pendingRecords = MutableLiveData<List<RecordModel>>()
    val pendingRecords: LiveData<List<RecordModel>> get() = _pendingRecords

    // RICARDO DIMAS
    private var _pendingRondines = MutableLiveData<List<RondinModel>>()
    val pendingRondines: LiveData<List<RondinModel>> get() = _pendingRondines

    fun sync(token: String) {
        viewModelScope.launch {
            _state.value = SyncState.Loading
            try {
                // 1. Sincronización de Usuarios
                val syncRequest = SyncRequest(token, data = emptyList())
                val loginsResponse: List<LoginModel>? = loginsUseCase(syncRequest)

                // 2. Sincronización de Vehículos de la Empresa
                val companyVehiclesResponse: List<CompanyVehicleModel>? = companyVehiclesUseCase()

                val loginsSuccess = if (!loginsResponse.isNullOrEmpty()) {
                    databaseService.deleteAllUsers()
                    val insertResults = databaseService.insertUsers(loginsResponse)
                    insertResults.all { it != null }
                } else {
                    false
                }

                val vehiclesSuccess = if (!companyVehiclesResponse.isNullOrEmpty()) {
                    databaseService.insertOrUpdateCompanyVehicles(companyVehiclesResponse)
                    true
                } else {
                    false
                }

                // 3. Evaluación global del estado
                if (loginsSuccess && vehiclesSuccess) {
                    _state.value = SyncState.Success(loginsResponse ?: emptyList())
                } else if (loginsSuccess) {
                    _state.value = SyncState.Error("Sincronizados usuarios, pero falló el catálogo de vehículos.")
                } else {
                    _state.value = SyncState.Error("Falló la sincronización de catálogos.")
                }

            } catch (e: Exception) {
                _state.value = SyncState.Error(e.message ?: "Ha ocurrido un error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun upload() {
        viewModelScope.launch {
            _state.value = SyncState.Loading
            try {
                val localData = repository.getUnsynchronizedRecords()

                if (localData != null) {
                    val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val hourFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
                    val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                    val transformedData: List<FormattedRecordsModel> = localData.map { register ->
                        val ingresoDate = LocalDate.parse(register.dIngresoInv, inputDateFormatter)
                        val hrIngresoTime = LocalTime.parse(register.dHringresoInv, hourFormatter)

                        val hrSalidaTime = when {
                            register.dHrsalidaInv.isNullOrEmpty() || register.dHrsalidaInv.equals("Now", ignoreCase = true) -> {
                                LocalTime.now()
                            }
                            else -> {
                                try {
                                    LocalTime.parse(register.dHrsalidaInv, hourFormatter)
                                } catch (e: Exception) {
                                    LocalTime.now()
                                }
                            }
                        }

                        val cMovimientoInv = if (register.dHrsalidaInv.isNullOrEmpty() || register.dHrsalidaInv.equals("Now", ignoreCase = true)) {
                            "E"
                        } else {
                            "S"
                        }

                        Log.d("SYNC_DEBUG", "Placa: ${register.cPlacaInv} | Km en SQLite: ${register.nKilometraje}")

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
                            cMovimientoInv = cMovimientoInv,
                            nKilometrajeInv = register.nKilometraje
                        )
                    }
                    val response = uploadUseCase(transformedData)

                    if (response == "Ok") {
                        localData.forEach { record ->
                            record.isSynced = 1
                            repository.updateVehicle(record)
                        }
                        _state.value = SyncState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRecords()
                    } else {
                        if (response == "Unauthorized") {
                            _state.value = SyncState.Error("No cuentas con un token para enviar los datos, cierra e inicia sesion y vuelve a intentarlo, por favor.")
                        } else {
                            _state.value = SyncState.Error(response.toString())
                        }
                    }
                } else {
                    _state.value = SyncState.Error("No hay nada que enviar")
                }
            } catch (e: Exception) {
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

    // RICARDO DIMAS - Rondines
    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadRondines() {
        viewModelScope.launch {
            _state.value = SyncState.Loading
            try {
                val localData = repository.listUnsynchronizedRondines()

                if (localData != null) {
                    val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val inputDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")
                    val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    val transformedData: List<FormattedRondinModel> = localData.map { register ->
                        val fechaRonDateTime = LocalDateTime.parse(register.fechaRon, inputDateTimeFormatter)
                        val formattedDate = fechaRonDateTime.format(dayFormatter)
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

                    val response = UploadUseCaseRondin(transformedData)

                    if (response == "Ok") {
                        localData.forEach { rondin ->
                            rondin.isSynced = 1
                            repository.updateRondines(rondin)
                        }
                        _state.value = SyncState.UploadSuccess("Datos enviados correctamente")
                        loadPendingRondines()
                    } else {
                        if (response == "Unauthorized") {
                            _state.value = SyncState.Error("No cuentas con un token para enviar los datos, cierra e inicia sesion y vuelve a intentarlo, por favor.")
                        } else {
                            _state.value = SyncState.Error(response.toString())
                        }
                    }
                } else {
                    _state.value = SyncState.Error("No hay nada que enviar")
                }
            } catch (e: Exception) {
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

    // ENVÍO DE LOGS DE ACCESO VEHICULAR (z_geningresovehiculo)
    fun enviarLogsDirectoPorCorreo(correoDestino: String = "programador@agroberries.mx") {
        val logsTexto = generarTextoLogsAccesoVehicular()
        val asunto = "Logs Acceso Vehicular - Dispositivo ${Build.MODEL}"

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nombreArchivo = "logs_z_geningresovehiculo_$timeStamp.txt"

        autoMailer.sendEmailWithAttachmentInBackground(
            toEmail = correoDestino,
            subject = asunto,
            bodyText = "Se adjunta el reporte de logs de la tabla z_geningresovehiculo generado desde el dispositivo ${Build.MODEL}.",
            attachmentContent = logsTexto,
            fileName = nombreArchivo
        )

        _state.postValue(SyncState.UploadSuccess("Logs enviados correctamente en archivo .txt."))
    }

    private fun generarTextoLogsAccesoVehicular(): String {
        val builder = StringBuilder()
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        builder.append("=== LOGS ACCESO VEHICULAR (z_geningresovehiculo) ===\n")
        builder.append("Fecha del reporte: $fechaActual\n")
        builder.append("Modelo dispositivo: ${Build.MODEL}\n\n")

        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM z_geningresovehiculo", null)

            if (cursor.moveToFirst()) {
                do {
                    val controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog"))
                    val dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv"))
                    val vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv"))
                    val vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv"))
                    val vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv"))
                    val cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv"))
                    val vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv"))
                    val dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv"))
                    val dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv"))
                    val cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu"))
                    val cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv"))
                    val isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))

                    val kmIdx = cursor.getColumnIndex("nKilometraje")
                    val nKilometraje = if (kmIdx != -1) cursor.getInt(kmIdx) else 0

                    builder.append("LogID: $controlLog | Placa: $cPlacaInv | Chofer: $vNombrechofInv | Empresa: $vEmpresaInv\n")
                    builder.append("Fecha Ingreso: $dIngresoInv | Hr Ingreso: $dHringresoInv | Hr Salida: $dHrsalidaInv\n")
                    builder.append("Acompañante: $vAcompanianteInv | Motivo: $vMotivoInv | Movimiento: $cMovimientoInv\n")
                    builder.append("Usuario: $cCodigoUsu | Sincronizado: $isSynced | Kilometraje: $nKilometraje\n")
                    builder.append("---------------------------------------------------\n")
                } while (cursor.moveToNext())
            } else {
                builder.append("No hay registros almacenados en z_geningresovehiculo.\n")
            }
            cursor.close()
        } catch (e: Exception) {
            builder.append("Error al leer la base de datos local: ${e.message}\n")
        }

        return builder.toString()
    }

    fun clearState() {
        _state.value = SyncState.Waiting
    }
}
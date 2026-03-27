package com.AgroberriesMX.accesovehicular.data.local

import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel

interface AccesoVehicularLocalDBService {
    suspend fun getAllVehicles(): List<RecordModel>
    suspend fun getVehiclesByDate(): List<RecordModel>?
    suspend fun getVehicleByPlate(cPlacaInv: String): List<RecordModel>?
    suspend fun getRecordByControlLog(controlLog: Long): RecordModel
    suspend fun getUnsynchronizedRecords(): List<RecordModel>?
    suspend fun listUnsynchronizedRecords(): List<RecordModel>?
    suspend fun insertVehicle( record: String,
                               vNombrechofInv: String,
                               vAcompanianteInv: String,
                               vEmpresaInv: String,
                               cPlacaInv: String,
                               vMotivoInv: String,
                               dHringresoInv: String,
                               dHrsalidaInv: String,
                               cCodigoUsu: String,
                               cMovimientoInv: String,
                               isSynced: Int): Long? // Cambia a un objeto si es posible
    suspend fun updateVehicle(controlLog: Long,
                              dIngresoInv: String,
                              vNombrechofInv: String,
                              vAcompanianteInv: String,
                              vEmpresaInv: String,
                              cPlacaInv: String,
                              vMotivoInv: String,
                              dHringresoInv: String,
                              dHrsalidaInv: String,
                              cCodigoUsu: String,
                              cMovimientoInv: String,
                              isSynced: Int): Int?

    //Credentials
    suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?
    suspend fun getAllUsers(): List<LoginModel>
    suspend fun insertUsers(users: List<LoginModel>): List<Long?>
    suspend fun deleteAllUsers()
    // --- ¡¡¡AÑADE ESTE MÉTODO A LA INTERFAZ!!! ---
    // Este es el que necesita tu LoginViewModel para guardar un solo LoginModel.
    // Usaremos el método insertOrUpdateLoginUser que te sugerí para DatabaseHelper.
    suspend fun saveUser(user: LoginModel): Long? // Devuelve el ID de fila o null/error

    //Rondines - Ricardo Dimas 23/06/2025
    suspend fun listUnsynchronizedRondines(): List<RondinModel>? //RICARDO DIMAS
    suspend fun updateRondines(idRondinRon: Long,
                               codigoUsuRon: String,
                               fechaRon: String,
                               latGpsRon: Double,
                               longGpsRon: Double,
                               nomUbicacionRon: String,
                               usuModRon: String,
                              isSynced: Int): Int?

}
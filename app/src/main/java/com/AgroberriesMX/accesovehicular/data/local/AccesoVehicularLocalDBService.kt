package com.AgroberriesMX.accesovehicular.data.local

import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
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

    suspend fun insertVehicle(
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
        isSynced: Int,
        nKilometraje: Int // <-- 12° PARÁMETRO AGREGADO
    ): Long?

    suspend fun updateVehicle(
        controlLog: Long,
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
        isSynced: Int,
        nKilometraje: Int // <-- PARÁMETRO AGREGADO
    ): Int?

    // Credentials
    suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?
    suspend fun getAllUsers(): List<LoginModel>
    suspend fun insertUsers(users: List<LoginModel>): List<Long?>
    suspend fun deleteAllUsers()
    suspend fun saveUser(user: LoginModel): Long?

    // Rondines
    suspend fun listUnsynchronizedRondines(): List<RondinModel>?
    suspend fun updateRondines(
        idRondinRon: Long,
        codigoUsuRon: String,
        fechaRon: String,
        latGpsRon: Double,
        longGpsRon: Double,
        nomUbicacionRon: String,
        usuModRon: String,
        isSynced: Int
    ): Int?

    // CATÁLOGO DE VEHÍCULOS DE LA EMPRESA
    suspend fun insertOrUpdateCompanyVehicles(vehicles: List<CompanyVehicleModel>)
    suspend fun getCompanyVehicleByNumEcon(numEcon: String): CompanyVehicleModel?
}
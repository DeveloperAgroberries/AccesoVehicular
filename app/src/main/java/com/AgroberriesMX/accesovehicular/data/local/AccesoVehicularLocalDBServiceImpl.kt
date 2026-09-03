package com.AgroberriesMX.accesovehicular.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import javax.inject.Inject

class AccesoVehicularLocalDBServiceImpl @Inject constructor(private val databaseHelper: DatabaseHelper) :
    AccesoVehicularLocalDBService {

    override suspend fun saveUser(user: LoginModel): Long? {
        return databaseHelper.insertOrUpdateLoginUser(user)
    }

    override suspend fun getAllVehicles(): List<RecordModel> {
        return databaseHelper.getRecords()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getVehiclesByDate(): List<RecordModel>? {
        return databaseHelper.getVehiclesByDate()
    }

    override suspend fun getVehicleByPlate(cPlacaInv: String): List<RecordModel>? {
        return databaseHelper.getVehicleByPlate(cPlacaInv)
    }

    override suspend fun getRecordByControlLog(controlLog: Long): RecordModel {
        return databaseHelper.getRecordByControlLog(controlLog)!!
    }

    override suspend fun listUnsynchronizedRecords(): List<RecordModel>? {
        return databaseHelper.listUnsynchronizedRecords()
    }

    override suspend fun getUnsynchronizedRecords(): List<RecordModel>? {
        return databaseHelper.getUnsynchronizedRecords()
    }

    override suspend fun insertVehicle(
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
    ): Long? {
        return databaseHelper.insertVehicle(
            dIngresoInv = dIngresoInv,
            vNombrechofInv = vNombrechofInv,
            vAcompanianteInv = vAcompanianteInv,
            vEmpresaInv = vEmpresaInv,
            cPlacaInv = cPlacaInv,
            vMotivoInv = vMotivoInv,
            dHringresoInv = dHringresoInv,
            dHrsalidaInv = dHrsalidaInv,
            cCodigoUsu = cCodigoUsu,
            cMovimientoInv = cMovimientoInv,
            isSynced = isSynced,
            nKilometraje = nKilometraje // <-- SE ENVÍA A DATABASEHELPER
        )
    }

    override suspend fun updateVehicle(
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
    ): Int? {
        return databaseHelper.updateVehicle(
            controlLog = controlLog,
            dIngresoInv = dIngresoInv,
            vNombreChofInv = vNombrechofInv,
            vAcompanianteInv = vAcompanianteInv,
            vEmpresaInv = vEmpresaInv,
            cPlacaInv = cPlacaInv,
            vMotivoInv = vMotivoInv,
            dHringresoInv = dHringresoInv,
            dHrsalidaInv = dHrsalidaInv,
            cCodigoUsu = cCodigoUsu,
            cMovimientoInv = cMovimientoInv,
            isSynced = isSynced,
            nKilometraje = nKilometraje // <-- SE ENVÍA A DATABASEHELPER
        )
    }

    // Credentials
    override suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel? {
        return databaseHelper.getUserByCodeAndPassword(cUsu, vPassword)
    }

    override suspend fun getAllUsers(): List<LoginModel> {
        return databaseHelper.getAllUsers()
    }

    override suspend fun insertUsers(users: List<LoginModel>): List<Long?> {
        return users.map { user ->
            databaseHelper.insertUser(
                vNombreUsu = user.vNombreUsu,
                cCodigoUsu = user.cCodigoUsu,
                vPasswordUsu = user.vPasswordUsu
            )
        }
    }

    override suspend fun deleteAllUsers() {
        return databaseHelper.deleteAllUsers()
    }

    // RICARDO DIMAS - Rondines 23/06/2025
    override suspend fun listUnsynchronizedRondines(): List<RondinModel>? {
        return databaseHelper.listUnsynchronizedRondines()
    }

    override suspend fun updateRondines(
        idRondinRon: Long,
        codigoUsuRon: String,
        fechaRon: String,
        latGpsRon: Double,
        longGpsRon: Double,
        nomUbicacionRon: String,
        usuModRon: String,
        isSynced: Int
    ): Int? {
        return databaseHelper.updateRondines(
            idRondinRon = idRondinRon,
            codigoUsuRon = codigoUsuRon,
            fechaRon = fechaRon,
            latGpsRon = latGpsRon,
            longGpsRon = longGpsRon,
            nomUbicacionRon = nomUbicacionRon,
            usuModRon = usuModRon,
            isSynced = isSynced
        )
    }

    // --- CATÁLOGO DE VEHÍCULOS DE LA EMPRESA ---
    override suspend fun insertOrUpdateCompanyVehicles(vehicles: List<CompanyVehicleModel>) {
        databaseHelper.insertOrUpdateCompanyVehicles(vehicles)
    }

    override suspend fun getCompanyVehicleByNumEcon(numEcon: String): CompanyVehicleModel? {
        return databaseHelper.getCompanyVehicleByNumEcon(numEcon)
    }
}
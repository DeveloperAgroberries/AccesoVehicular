package com.AgroberriesMX.accesovehicular.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import javax.inject.Inject

class AccesoVehicularLocalDBServiceImpl @Inject constructor(private val databaseHelper: DatabaseHelper) :
    AccesoVehicularLocalDBService {
    // --- ¡¡¡AÑADE ESTE MÉTODO A LA IMPLEMENTACIÓN!!! ---
    override suspend fun saveUser(user: LoginModel): Long? {
        // Llama al método que has añadido en DatabaseHelper para insertar/actualizar un usuario
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
            record: String,
            vNombrechofInv: String,
            vAcompanianteInv: String,
            vEmpresaInv: String,
            cPlacaInv: String,
            vMotivoInv: String,
            dHringresoInv: String,
            dHrsalidaInv: String,
            cCodigoUsu: String,
            cMovimientoInv: String,
            isSynced: Int
        ): Long? {
            return databaseHelper.insertVehicle(
                dIngresoInv = record,
                vNombrechofInv = vNombrechofInv,
                vAcompanianteInv = vAcompanianteInv,
                vEmpresaInv = vEmpresaInv,
                cPlacaInv = cPlacaInv,
                vMotivoInv = vMotivoInv,
                dHringresoInv = dHringresoInv,
                dHrsalidaInv = dHrsalidaInv,
                cCodigoUsu = cCodigoUsu,
                cMovimientoInv = cMovimientoInv,
                isSynced = isSynced
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
        isSynced: Int
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
                isSynced = isSynced
            )
    }

    //Credentials
    override suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?{
        return databaseHelper.getUserByCodeAndPassword(cUsu, vPassword)
    }

    override suspend fun getAllUsers(): List<LoginModel>{
        return databaseHelper.getAllUsers()
    }

    override suspend fun insertUsers(users: List<LoginModel>): List<Long?> {
        return users.map{
            user ->
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

    //RICARDO DIMAS - Rondines 23/06/2025
    override suspend fun listUnsynchronizedRondines(): List<RondinModel>? {
        return databaseHelper.listUnsynchronizedRondines()
    }

    // Implementación de updateRondines (el método que causaba el error)
    override suspend fun updateRondines(
        idRondinRon: Long,
        codigoUsuRon: String,
        fechaRon: String,
        latGpsRon: Double,   // ¡Debe ser Double aquí!
        longGpsRon: Double,  // ¡Debe ser Double aquí!
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
}
package com.AgroberriesMX.accesovehicular.data

import com.AgroberriesMX.accesovehicular.data.local.AccesoVehicularLocalDBService
import com.AgroberriesMX.accesovehicular.data.network.AccesoVehicularApiService
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import javax.inject.Inject

class RecordsRepositoryImpl @Inject constructor(
    private val localDBService: AccesoVehicularLocalDBService,
    private val apiService: AccesoVehicularApiService
) : RecordsRepository {

    // Metodos para la base de datos local (ROOM)
    override suspend fun getAllVehicles(): List<RecordModel> {
        return localDBService.getAllVehicles()
    }

    override suspend fun getVehiclesByDate(): List<RecordModel>? {
        return localDBService.getVehiclesByDate()
    }

    override suspend fun getVehicleByPlate(cPlaca: String): List<RecordModel>? {
        return localDBService.getVehicleByPlate(cPlaca)
    }

    override suspend fun getRecordByControlLog(controlLog: Long): RecordModel {
        return localDBService.getRecordByControlLog(controlLog)
    }

    override suspend fun listUnsynchronizedRecords(): List<RecordModel>? {
        return localDBService.listUnsynchronizedRecords()
    }

    override suspend fun getUnsynchronizedRecords(): List<RecordModel>? {
        return localDBService.getUnsynchronizedRecords()
    }

    override suspend fun insertVehicle(record: RecordModel): Long? {
        return localDBService.insertVehicle(
            record.dIngresoInv,
            record.vNombrechofInv,
            record.vAcompanianteInv,
            record.vEmpresaInv,
            record.cPlacaInv,
            record.vMotivoInv,
            record.dHringresoInv,
            record.dHrsalidaInv,
            record.cCodigoUsu,
            record.cMovimientoInv,
            record.isSynced,
            record.nKilometraje // <-- AGREGADO AQUÍ
        )
    }

    override suspend fun updateVehicle(record: RecordModel): Int? {
        return localDBService.updateVehicle(
            record.controlLog,
            record.dIngresoInv,
            record.vNombrechofInv,
            record.vAcompanianteInv,
            record.vEmpresaInv,
            record.cPlacaInv,
            record.vMotivoInv,
            record.dHringresoInv,
            record.dHrsalidaInv,
            record.cCodigoUsu,
            record.cMovimientoInv,
            record.isSynced,
            record.nKilometraje // <-- AGREGADO AQUÍ
        )
    }

    // RICARDO DIMAS
    override suspend fun listUnsynchronizedRondines(): List<RondinModel>? {
        return localDBService.listUnsynchronizedRondines()
    }

    override suspend fun updateRondines(rondin: RondinModel): Int? {
        return localDBService.updateRondines(
            rondin.idRondinRon,
            rondin.codigoUsuRon,
            rondin.fechaRon,
            rondin.latGpsRon,
            rondin.longGpsRon,
            rondin.nomUbicacionRon,
            rondin.usuModRon,
            rondin.isSynced
        )
    }

    override suspend fun getCompanyVehicles(): List<CompanyVehicleModel>? {
        return try {
            val response = apiService.listVehiculosEmpresa()
            if (response.isSuccessful && response.body() != null) {
                val vehiclesList = response.body()!!.vehicles.map { it.toDomain() }

                // GUARDA LA LISTA EN SQLITE AL DESCARGARLA
                if (vehiclesList.isNotEmpty()) {
                    localDBService.insertOrUpdateCompanyVehicles(vehiclesList)
                }

                vehiclesList
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCompanyVehicleByNumEcon(numEcon: String): CompanyVehicleModel? {
        return localDBService.getCompanyVehicleByNumEcon(numEcon)
    }
}
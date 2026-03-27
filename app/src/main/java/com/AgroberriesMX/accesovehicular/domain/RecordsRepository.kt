package com.AgroberriesMX.accesovehicular.domain

import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel

interface RecordsRepository {
    suspend fun getAllVehicles(): List<RecordModel>
    suspend fun getVehiclesByDate(): List<RecordModel>?
    suspend fun getVehicleByPlate(cPlaca: String): List<RecordModel>?
    suspend fun getRecordByControlLog(controlLog: Long): RecordModel

    suspend fun listUnsynchronizedRecords(): List<RecordModel>?
    suspend fun getUnsynchronizedRecords(): List<RecordModel>?
    suspend fun insertVehicle(record: RecordModel): Long?
    suspend fun updateVehicle(record: RecordModel): Int?

    //RICARDO DIMAS
    suspend fun listUnsynchronizedRondines(): List<RondinModel>?
    suspend fun updateRondines(record: RondinModel): Int?
    //suspend fun getUnsynchronizedRondines(): List<RondinModel>?
    //suspend fun uploadRondin(record: RondinModel): Int?
}
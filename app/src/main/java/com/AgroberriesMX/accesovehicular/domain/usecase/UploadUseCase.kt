package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.domain.Repository
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import javax.inject.Inject

class UploadUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke (records: List<FormattedRecordsModel>):String {
        val response = repository.uploadRecords(records)
        return when (response.second) {
            200 -> "Ok"
            401 -> "Unauthorized"
            else -> response.first ?: "Error desconocido"
        }
    }
}
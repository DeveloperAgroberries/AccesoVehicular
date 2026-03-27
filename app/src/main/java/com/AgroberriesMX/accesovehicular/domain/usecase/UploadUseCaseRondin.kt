package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.domain.Repository
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import javax.inject.Inject

class UploadUseCaseRondin @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke (rondines: List<FormattedRondinModel>):String {
        val response = repository.uploadRondin(rondines)
        return when (response.second) {
            200 -> "Ok"
            401 -> "Unauthorized"
            else -> response.first ?: "Error desconocido"
        }
    }
}
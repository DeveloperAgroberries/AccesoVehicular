package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
import javax.inject.Inject

class GetCompanyVehiclesUseCase @Inject constructor(
    private val repository: RecordsRepository
) {
    suspend operator fun invoke(): List<CompanyVehicleModel>? {
        return repository.getCompanyVehicles()
    }
}
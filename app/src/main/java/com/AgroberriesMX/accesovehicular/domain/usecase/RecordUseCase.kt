package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import javax.inject.Inject

class RecordUseCase @Inject constructor(private val recordsRepository: RecordsRepository){
    suspend operator fun invoke(controlLog: Long) = recordsRepository.getRecordByControlLog(controlLog)
}
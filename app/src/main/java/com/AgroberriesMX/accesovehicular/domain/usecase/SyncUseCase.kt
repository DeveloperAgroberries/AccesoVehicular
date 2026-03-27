package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.domain.Repository
import javax.inject.Inject

class SyncUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(
        syncRequest: SyncRequest
    ) = repository.getLogins(syncRequest)
}
package com.AgroberriesMX.accesovehicular.ui.sync

import com.AgroberriesMX.accesovehicular.domain.model.LoginModel


sealed class SyncState {
    data object Loading: SyncState()
    data object Waiting: SyncState()

    data class Error(val message: String): SyncState()
    data class Success(val success: List<LoginModel>): SyncState()
    data class UploadSuccess(val message: String): SyncState()
}
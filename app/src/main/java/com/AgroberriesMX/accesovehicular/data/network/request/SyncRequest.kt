package com.AgroberriesMX.accesovehicular.data.network.request

import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel

class SyncRequest(
    val token:String,
    val data: List<FormattedRecordsModel>
)
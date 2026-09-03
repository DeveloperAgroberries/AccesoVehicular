package com.AgroberriesMX.accesovehicular.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompanyVehicleModel(
    val vNombreAfc: String? = "",
    val vNumlicenciaAfc: String? = "",
    val cNumeconAfi: String,
    val vPlacasAfi: String? = ""
) : Parcelable
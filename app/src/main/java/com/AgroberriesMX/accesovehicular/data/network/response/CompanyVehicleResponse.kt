package com.AgroberriesMX.accesovehicular.data.network.response

import com.AgroberriesMX.accesovehicular.domain.model.CompanyVehicleModel
import com.google.gson.annotations.SerializedName

data class CompanyVehicleResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val vehicles: List<CompanyVehicleResponseItem>
)

data class CompanyVehicleResponseItem(
    @SerializedName("vNombreAfc") val vNombreAfc: String? = null,
    @SerializedName("vNumlicenciaAfc") val vNumlicenciaAfc: String? = null,
    @SerializedName("cNumeconAfi") val cNumeconAfi: String,
    @SerializedName("vPlacasAfi") val vPlacasAfi: String? = null
) {
    fun toDomain(): CompanyVehicleModel {
        return CompanyVehicleModel(
            vNombreAfc = vNombreAfc?.trim() ?: "",
            vNumlicenciaAfc = vNumlicenciaAfc?.trim() ?: "",
            cNumeconAfi = cNumeconAfi.trim(),
            vPlacasAfi = vPlacasAfi?.trim() ?: ""
        )
    }
}
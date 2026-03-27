package com.AgroberriesMX.accesovehicular.data.network.response

import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.google.gson.annotations.SerializedName

data class LoginsResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val logins: List<LoginResponseItem>
)

data class LoginResponseItem(
    @SerializedName("controlLog") val controlLog:Long?=null,
    @SerializedName("vNombreUsu") val vNombreUsu:String,
    @SerializedName("cCodigoUsu") val cCodigoUsu:String,
    @SerializedName("vPasswordUsu") val vPasswordUsu:String
) {
    fun toDomain(controlLog: Long): LoginModel {
        return LoginModel(
            controlLog = controlLog,
            vNombreUsu = vNombreUsu,
            cCodigoUsu = cCodigoUsu.trim(),
            vPasswordUsu = vPasswordUsu
        )
    }
}
package com.AgroberriesMX.accesovehicular.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FormattedRecordsModel(
    var dIngresoInv: String,
    val vNombrechofInv: String,
    val vAcompanianteInv: String,
    val vEmpresaInv: String,
    val cPlacaInv: String,
    val vMotivoInv: String,
    var dHringresoInv: String,
    var dHrsalidaInv: String,
    val cCodigoUsu: String,
    val dCreacionInv: String,
    val cMovimientoInv: String,
    @SerializedName("NKilometrajeInv") // <-- Obliga a enviar 'NKilometrajeInv' con 'N' mayúscula
    val nKilometrajeInv: Int = 0 // <-- NUEVO CAMPO PARA SINCRONIZACIÓN
) : Parcelable
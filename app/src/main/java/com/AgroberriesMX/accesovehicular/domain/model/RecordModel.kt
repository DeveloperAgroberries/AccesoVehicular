package com.AgroberriesMX.accesovehicular.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordModel(
    val controlLog: Long,
    var dIngresoInv: String,
    val vNombrechofInv: String,
    val vAcompanianteInv: String,
    val vEmpresaInv: String,
    val cPlacaInv: String,
    val vMotivoInv: String,
    var dHringresoInv: String,
    var dHrsalidaInv: String,
    val cCodigoUsu: String,
    val cMovimientoInv: String,
    var isSynced: Int
) : Parcelable
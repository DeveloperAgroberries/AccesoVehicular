package com.AgroberriesMX.accesovehicular.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FormattedRondinModel(
    val cCodigoUsu: String,
    val dFecha: String, // Mantener como String, pero formatearla correctamente
    val vLatGps: Double, // ¡Cambiar a String!
    val vLonGps: Double, // ¡Cambiar a String!
    val vNomUbicacion: String,
    val cUsumod: String
) : Parcelable
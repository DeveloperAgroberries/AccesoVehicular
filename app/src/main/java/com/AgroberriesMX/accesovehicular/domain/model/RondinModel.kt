package com.AgroberriesMX.accesovehicular.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RondinModel(
    val idRondinRon: Long,
    val codigoUsuRon: String,
    val fechaRon: String,
    val latGpsRon: Double, // ¡Debe ser Double!
    val longGpsRon: Double, // ¡Debe ser Double!
    val nomUbicacionRon: String,
    val usuModRon: String,
    var isSynced: Int // ¡Debe ser Int!
) : Parcelable
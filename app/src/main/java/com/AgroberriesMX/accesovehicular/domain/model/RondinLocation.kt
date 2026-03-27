// Archivo: com/AgroberriesMX/accesovehicular/domain/model/RondinLocation.kt
package com.AgroberriesMX.accesovehicular.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RondinLocation(
    val idRondinRon: Int = 0,
    val codigoUsuRon: String,
    val fechaRon: String, // La mantendremos como String para la BD
    val latGpsRon: Double,
    val longGpsRon: Double,
    val nomUbicacionRon: String,
    val usuModRon: String,
    val isSynced: Int = 0 // ¡¡¡ CAMBIO CLAVE: AÑADIDO EL CAMPO isSynced CON VALOR POR DEFECTO !!!
) {
    // Constructor secundario que omite idRondinRon e isSynced (asumiendo 0 para ambos al crear)
    constructor(
        codigoUsuRon: String,
        latGpsRon: Double,
        longGpsRon: Double,
        nomUbicacionRon: String,
        usuModRon: String
    ) : this(
        idRondinRon = 0, // Valor por defecto para ID
        codigoUsuRon = codigoUsuRon,
        fechaRon = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
        latGpsRon = latGpsRon,
        longGpsRon = longGpsRon,
        nomUbicacionRon = nomUbicacionRon,
        usuModRon = usuModRon,
        isSynced = 0 // Valor por defecto para isSynced en el constructor secundario
    )
}
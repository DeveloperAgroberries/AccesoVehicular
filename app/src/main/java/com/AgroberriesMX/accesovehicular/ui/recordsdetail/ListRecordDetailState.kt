package com.AgroberriesMX.accesovehicular.ui.recordsdetail

sealed class ListRecordDetailState {
    data object Loading : ListRecordDetailState()
    data class Error(val error: String) : ListRecordDetailState()
    data class Success(
        val noRegistro: Long,
        val Fecha: String,
        val NombreChofer: String,
        val NombreAcompañante: String,
        val Empresa: String,
        val Placa: String,
        val MotivoVisita: String,
        val Entrada: String,
        val Salida: String,
        val Usuario: String,
        val Sincronizado: Int
    ) : ListRecordDetailState()
}
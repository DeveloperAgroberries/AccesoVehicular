package com.AgroberriesMX.accesovehicular.ui.recordsdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AgroberriesMX.accesovehicular.domain.usecase.RecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ListRecordDetailViewModel @Inject constructor(private val getRecordUseCase: RecordUseCase) :
    ViewModel() {
        private val _state = MutableLiveData<ListRecordDetailState>()
        val state: LiveData<ListRecordDetailState> = _state

        fun getRecord(controlLog: Long){
            viewModelScope.launch {
                _state.value = ListRecordDetailState.Loading

                val result = withContext(Dispatchers.IO){
                    getRecordUseCase(controlLog)
                }
                if(result != null){
                    _state.value = ListRecordDetailState.Success(
                        result.controlLog,
                        result.dIngresoInv,
                        result.vNombrechofInv,
                        result.vAcompanianteInv,
                        result.vEmpresaInv,
                        result.cPlacaInv,
                        result.vMotivoInv,
                        result.dHringresoInv,
                        result.dHrsalidaInv,
                        result.cCodigoUsu,
                        result.isSynced
                    )
                } else {
                    _state.value = ListRecordDetailState.Error("Ha ocurrido un error cargando los datos, intentalo de nuevo.")
                }
            }
        }
    }
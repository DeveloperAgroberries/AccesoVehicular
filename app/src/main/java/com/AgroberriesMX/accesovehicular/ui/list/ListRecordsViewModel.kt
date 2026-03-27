package com.AgroberriesMX.accesovehicular.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListRecordsViewModel @Inject constructor(private val repository: RecordsRepository) : ViewModel() {
    private val _todayRecords = MutableLiveData<List<RecordModel>?>(emptyList())
    val todayRecords: MutableLiveData<List<RecordModel>?> = _todayRecords

    private val _allRecords = MutableLiveData<List<RecordModel>?>(emptyList())
    val allRecords: MutableLiveData<List<RecordModel>?> = _allRecords

    private val _filteredRecords = MutableLiveData<List<RecordModel>?>(emptyList())
    val filteredRecords: LiveData<List<RecordModel>?> = _filteredRecords

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadTodayRecords()
        loadAllRecords()
    }

    fun loadTodayRecords() {
        viewModelScope.launch {
            try {
                // Cargar los vehículos desde el repositorio
                _todayRecords.value = repository.getVehiclesByDate()
                _filteredRecords.value = _todayRecords.value
            } catch (e: Exception) {
                _error.value = "Error al cargar los registros: ${e.message}"
            }
        }
    }

    fun loadAllRecords() {
        viewModelScope.launch {
            try {
                // Cargar los vehículos desde el repositorio
                _allRecords.value = repository.getAllVehicles()
            } catch (e: Exception) {
                _error.value = "Error al cargar los registros: ${e.message}"
            }
        }
    }

    fun getVehicleByPlate(plate: String) {
        viewModelScope.launch{
            try {
                val filteredRecords = repository.getVehicleByPlate(plate)
                _allRecords.value = filteredRecords
            } catch (e: Exception) {
                _error.value = "Error al cargar los registros: ${e.message}"
            }
        }
    }

    fun searchRecords(query: String) {
        viewModelScope.launch {
            val currentRecords = if(query.isBlank()){
                _todayRecords.value ?: emptyList()
            } else {
                _allRecords.value ?: emptyList()
            }

            val filtered = currentRecords.filter { record ->
                record.cPlacaInv.contains(query, ignoreCase = true) || record.vNombrechofInv.contains(query, ignoreCase = true) || record.dIngresoInv.contains(query, ignoreCase = true)
            }
            _filteredRecords.value = filtered
        }
    }
}
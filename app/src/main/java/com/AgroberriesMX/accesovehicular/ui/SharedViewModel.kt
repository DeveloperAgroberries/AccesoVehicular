package com.AgroberriesMX.accesovehicular.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SharedViewModel @Inject constructor() : ViewModel() {
    private val _recordAdded = MutableLiveData<Boolean>()
    val recordAdded: LiveData<Boolean> get() = _recordAdded

    fun addRecord() {
        _recordAdded.value = true
    }

    fun resetRecordAdded() {
        _recordAdded.value = false
    }
}
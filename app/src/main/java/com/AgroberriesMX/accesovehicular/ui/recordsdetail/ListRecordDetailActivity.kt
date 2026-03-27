package com.AgroberriesMX.accesovehicular.ui.recordsdetail

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.AgroberriesMX.accesovehicular.databinding.ActivityListRecordDetailBinding
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
//import java.time.LocalDateTime // Cambiado de LocalTime a LocalDateTime

@AndroidEntryPoint
class ListRecordDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListRecordDetailBinding
    private val listRecordDetailViewModel: ListRecordDetailViewModel by viewModels()

    @Inject lateinit var recordsRepository: RecordsRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- ¡AQUÍ ES DONDE AÑADES EL CÓDIGO PARA ESTABLECER LA HORA POR DEFECTO! ---
        /*if (binding.tvTimeOut.text.isBlank() || binding.tvTimeOut.text.toString().equals("Now", ignoreCase = true)) {
            val currentDateTime = LocalDateTime.now()
            // Formato deseado: "YYYY-MM-DD HH:MM:SS.000"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            binding.tvTimeOut.text = currentDateTime.format(formatter)
        }*/

        initUI()

        val controlLog = intent.getLongExtra("controlLog",-1L)
        if(controlLog != -1L) {
            listRecordDetailViewModel.getRecord(controlLog)
        }
        observeViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUI() {
        initListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.btnCheckOut.setOnClickListener {
            binding.tvTimeOut.text = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        }

        binding.btnSave.setOnClickListener {
            /*if(
                binding.tvTimeOut.text.equals("Now") ||
                binding.tvTimeOut.text.equals("")
            ){
                Toast.makeText(this,
                    "Revisa que el campo de Hora de salida contenga datos, por favor.",
                    Toast.LENGTH_LONG
                ).show()
            } else {*/
                if (binding.rdCheckOut.text.count() > 4){
                    Toast.makeText(this,
                        "No puedes volver a guardar una hora de salida en este registro",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btnSave.isEnabled = false
                } else {
                    saveDataDetail()
                    val intent = Intent()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            //}
        }
    }

    private fun saveDataDetail() {
        // Datos a insertar
        val controlLog = binding.rdNoRegister.text.toString().trim()
        val dIngresoInv = binding.rdDateToday.text.toString().trim()
        val vNombreChofInv = binding.rdDriverName.text.toString().trim()
        val vAcompanianteInv = binding.rdCompanion.text.toString().trim()
        val vEmpresaInv = binding.rdCompany.text.toString().trim()
        val cPlacaInv = binding.rdPlate.text.toString().trim()
        val vMotivoInv = binding.rdReason.text.toString().trim()
        val dHringresoInv = binding.rdCheckIn.text.toString().trim()
        val dHrsalidaInv = binding.tvTimeOut.text.toString().trim()
        val cCodigoUsu = binding.rdUser.text.toString().trim()
        val cMovimientoInv = "E"
        val isSynced =  if(binding.rdIsSynced.text.toString().trim() == "No") 0 else 1

        // Inserta el vehículo
        val vehicleRecord = RecordModel(
            controlLog.toLong(),
            dIngresoInv,
            vNombreChofInv,
            vAcompanianteInv,
            vEmpresaInv,
            cPlacaInv,
            vMotivoInv,
            dHringresoInv,
            dHrsalidaInv,
            cCodigoUsu,
            cMovimientoInv,
            isSynced.toInt()
        )

        lifecycleScope.launch {
            try {
                recordsRepository.updateVehicle(vehicleRecord)
            }catch (e: Exception){
                Toast.makeText(this@ListRecordDetailActivity, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeViewModel() {
        listRecordDetailViewModel.state.observe(this) { state ->
            when (state) {
                is ListRecordDetailState.Loading -> {
                    // Muestra un indicador de carga
                }
                is ListRecordDetailState.Success -> {
                    binding.rdNoRegister.text = state.noRegistro.toString()
                    binding.rdDateToday.text = state.Fecha
                    binding.rdDriverName.text = state.NombreChofer
                    binding.rdCompanion.text = state.NombreAcompañante
                    binding.rdCompany.text = state.Empresa
                    binding.rdPlate.text = state.Placa
                    binding.rdReason.text = state.MotivoVisita
                    binding.rdCheckIn.text = state.Entrada
                    binding.rdCheckOut.text = state.Salida
                    binding.rdUser.text = state.Usuario
                    binding.rdIsSynced.text = if(state.Sincronizado == 0) "No" else "Si"
                    /*if(state.Salida.length > 4){
                        binding.btnCheckOut.isEnabled = false
                        binding.btnSave.isEnabled = false
                    }*/
                }
                is ListRecordDetailState.Error -> {
                    // Muestra el mensaje de error
                    Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }
}
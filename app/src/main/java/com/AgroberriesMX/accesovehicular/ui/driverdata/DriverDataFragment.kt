package com.AgroberriesMX.accesovehicular.ui.driverdata

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.AgroberriesMX.accesovehicular.R
import com.AgroberriesMX.accesovehicular.databinding.FragmentDriverDataBinding
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.ui.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DriverDataFragment : Fragment() {
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var _binding: FragmentDriverDataBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var recordsRepository: RecordsRepository
    private lateinit var sessionPrefs: SharedPreferences

    companion object {
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val LOGGED_USER_KEY = "logged_user"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverDataBinding.inflate(layoutInflater, container, false)
        return binding.root


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents()
        initUI()
        initListeners()
        applyFilters()
        initSpinner()
    }

    private fun initSpinner() {
        val razones = listOf(
            "Selecciona el motivo de la visita",
            "Trabajo oficina",
            "Trabajo campo",
            "Trabajo cooler",
            "Visita",
            "Entrevista",
            "Entregar/recoger material",
            "Otro"
        )
        val empresas =
            listOf("Selecciona una empresa", "Agroberries", "NexGen", "Exportadora", "Otra")

        val adapterReason =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, razones)
        adapterReason.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReason.adapter = adapterReason
        binding.spinnerReason.setSelection(0)

        val adapterCompany =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, empresas)
        adapterCompany.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCompany.adapter = adapterCompany
        binding.spinnerCompany.setSelection(0)
    }

    private fun applyFilters() {
        val noSpecialCharsFilter = InputFilter { source, start, end, dest, dstart, dend ->
            // Expresión regular para aceptar solo letras y números
            val regex = "^[a-zA-Z0-9]*$"
            if (source.matches(regex.toRegex())) {
                null // Acepta la entrada
            } else {
                "" // Rechaza la entrada
            }
        }

        val lengthFilter = InputFilter.LengthFilter(9)

        binding.etPlate.filters = arrayOf(noSpecialCharsFilter, lengthFilter)
    }

    private fun initUI() {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.etDriverName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etCompanionName.requestFocus()
                true
            } else {
                false
            }
        }

        binding.etCompanionName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.spinnerCompany.requestFocus()
                true
            } else {
                false
            }
        }

        binding.spinnerCompany.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position).toString()

                    // Verifica si la opción seleccionada requiere mostrar etOtherReason
                    if (selectedItem == "Otra") { // Cambia "Otro" si es otra opción en tu caso
                        binding.etOtherCompanyLayout.visibility = View.VISIBLE
                    } else {
                        binding.etOtherCompanyLayout.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.etOtherCompanyLayout.visibility = View.GONE
                }
            }

        binding.etPlate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val upperCaseText = s.toString().uppercase(Locale.getDefault())
                if (upperCaseText != s.toString()) {
                    binding.etPlate.setText(upperCaseText)
                    binding.etPlate.setSelection(upperCaseText.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPlate.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.spinnerReason.requestFocus()
                true
            } else {
                false
            }
        }

        binding.spinnerReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Verifica si la opción seleccionada requiere mostrar etOtherReason
                if (selectedItem == "Otro") { // Cambia "Otro" si es otra opción en tu caso
                    binding.etOtherReasonLayout.visibility = View.VISIBLE
                } else {
                    binding.etOtherReasonLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.etOtherReasonLayout.visibility = View.GONE
            }
        }

        binding.btnCheckIn.setOnClickListener {
            binding.tvTimeIn.text =
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
        }

        binding.btnConfirm.setOnClickListener {
            val todayDate = binding.tvDateToday.text.toString().trim()
            val driverName = binding.etDriverName.text.toString().trim()
            val companionName = binding.etCompanionName.text.toString().trim()
            val companyName = if (!binding.etOtherCompanyLayout.isVisible) {
                binding.spinnerCompany.selectedItem.toString().trim()
            } else {
                binding.etOtherCompany.text.toString().trim()
            }
            val plate = binding.etPlate.text.toString().trim()
            val reason = if (!binding.etOtherReasonLayout.isVisible) {
                binding.spinnerReason.selectedItem.toString().trim()
            } else {
                binding.etOtherReason.text.toString().trim()
            }
            val hrIngreso = binding.tvTimeIn.text.toString().trim()

            val cMovimientoInv = "E" // <--- ¡DEFINIDO AQUÍ!

            if (
                driverName.isEmpty() ||
                companionName.isEmpty() ||
                companyName.isEmpty() ||
                plate.isEmpty() ||
                reason.isEmpty() ||
                hrIngreso == "Now" ||
                hrIngreso == "" ||
                cMovimientoInv.isEmpty()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Revisa que todos los campos contengan datos, por favor.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (plate.length < 5) {
                Toast.makeText(
                    requireContext(),
                    "La placa debe de tener un minimo de 5 elementos.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!plate.matches("^[a-zA-Z0-9]*$".toRegex())) {
                Toast.makeText(
                    requireContext(),
                    "No se permiten caracteres especiales como - o .",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                saveData(
                    todayDate,
                    driverName,
                    companionName,
                    companyName,
                    plate,
                    reason,
                    hrIngreso,
                    cMovimientoInv,
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initComponents() {
        val currentDate = LocalDate.now()
        val formatter =
            DateTimeFormatter.ofPattern(getString(R.string.date_format_driver_data_fragment))
        binding.tvDateToday.text = currentDate.format(formatter)
    }

    private fun getLoggedUser(): String {
        return sessionPrefs.getString(LOGGED_USER_KEY, "FCASTELLANOS") ?: "usuario_desconocido"
    }

    @RequiresApi(Build.VERSION_CODES.O) // <--- ¡Añade esta línea aquí! Ricardo Dimas - 12/06/2025
    private fun saveData(
        dIngresoInv: String,
        vNombreChofInv: String,
        vAcompanianteInv: String,
        vEmpresaInv: String,
        cPlacaInv: String,
        vMotivoInv: String,
        dHringresoInv: String,
        cMovimientoInv: String
    ) {
        sessionPrefs = requireActivity().getSharedPreferences(
            SESSION_PREFERENCES_KEY,
            AppCompatActivity.MODE_PRIVATE
        )
        val user = getLoggedUser()

        val selectedReason = if (!binding.etOtherReasonLayout.isVisible) {
            binding.spinnerReason.selectedItem.toString().trim()
        } else {
            binding.etOtherReason.text.toString().trim()
        }

        val selectedCompany = if (!binding.etOtherCompanyLayout.isVisible) {
            binding.spinnerCompany.selectedItem.toString().trim()
        } else {
            binding.etOtherCompany.text.toString().trim()
        }

        if (selectedReason == "Selecciona el motivo de la visita"
            || selectedCompany == "Selecciona una empresa"
        ) {
            Toast.makeText(
                requireContext(),
                "Por favor, seleciona un motivo valido y una empresa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Datos a insertar
        val controlLog = 0L
        val dHrsalidaInv = ""
        //val dHrsalidaInv: String = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")) //Quitar para que deje insertar Ricardo Dimas - 12/06/2025
        val cCodigoUsu = user.toString().trim().uppercase()
        val isSynced = 0

        // Inserta el vehículo
        val vehicleRecord = RecordModel(
            controlLog,
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
            isSynced
        )

        lifecycleScope.launch {
            try {
                recordsRepository.insertVehicle(vehicleRecord)
                Toast.makeText(
                    requireContext(),
                    "Registro guardado correctamente",
                    Toast.LENGTH_LONG
                ).show()
                sharedViewModel.addRecord()
                binding.etDriverName.text!!.clear()
                binding.etCompanionName.text!!.clear()
                if (binding.etOtherCompany.text.toString().trim().isNotEmpty()) {
                    binding.etOtherCompany.text!!.clear()
                    binding.spinnerCompany.setSelection(0)
                } else {
                    binding.spinnerCompany.setSelection(0)
                }
                binding.etPlate.text!!.clear()
                if (binding.etOtherReason.text.toString().trim().isNotEmpty()) {
                    binding.etOtherReason.text!!.clear()
                    binding.spinnerReason.setSelection(0)
                } else {
                    binding.spinnerReason.setSelection(0)
                }
                binding.tvTimeIn.text = null
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.AgroberriesMX.accesovehicular.ui.driverdata

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
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

    private var isQrScanned = false

    companion object {
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val LOGGED_USER_KEY = "logged_user"
    }

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            parseAndFillQrData(result.contents)
        } else {
            Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
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
        val noSpecialCharsFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = "^[a-zA-Z0-9]*$"
            if (source.matches(regex.toRegex())) {
                null
            } else {
                ""
            }
        }

        val lengthFilter = InputFilter.LengthFilter(9)
        binding.etPlate.filters = arrayOf(noSpecialCharsFilter, lengthFilter)
    }

    private fun initUI() {}

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.btnScanQR.setOnClickListener {
            startQrScanner()
        }

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

                    if (selectedItem == "Otra") {
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

                if (selectedItem == "Otro") {
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

            // LECTURA DIRECTA Y EXPLICITA DEL CAMPO DE TEXTO
            val rawMileageText = binding.etMileage.text?.toString()?.trim() ?: ""

            val reason = if (!binding.etOtherReasonLayout.isVisible) {
                binding.spinnerReason.selectedItem.toString().trim()
            } else {
                binding.etOtherReason.text.toString().trim()
            }
            val hrIngreso = binding.tvTimeIn.text.toString().trim()
            val cMovimientoInv = "E"

            // Si el layout es visible, se exige que el kilometraje no esté vacío
            val isMileageFieldVisible = binding.tilMileageLayout.isVisible
            val isMileageInvalid = isMileageFieldVisible && rawMileageText.isEmpty()

            Log.d("DRIVER_DATA_DEBUG", "Visibilidad Layout: $isMileageFieldVisible | Texto Input: '$rawMileageText'")

            if (
                driverName.isEmpty() ||
                companionName.isEmpty() ||
                companyName.isEmpty() ||
                plate.isEmpty() ||
                reason.isEmpty() ||
                hrIngreso == "Now" ||
                hrIngreso.isEmpty() ||
                cMovimientoInv.isEmpty() ||
                isMileageInvalid
            ) {
                Toast.makeText(
                    requireContext(),
                    "Por favor llena todos los campos requeridos (incluyendo el kilometraje).",
                    Toast.LENGTH_LONG
                ).show()
            } else if (plate.length < 5) {
                Toast.makeText(
                    requireContext(),
                    "La placa debe tener un mínimo de 5 caracteres.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!plate.matches("^[a-zA-Z0-9]*$".toRegex())) {
                Toast.makeText(
                    requireContext(),
                    "No se permiten caracteres especiales en la placa.",
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
                    rawMileageText
                )

                isQrScanned = false
            }
        }
    }

    private fun startQrScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Escanea el código QR del vehículo")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(true)
        }
        qrLauncher.launch(options)
    }

    private fun parseAndFillQrData(qrContent: String) {
        val cleanContent = qrContent.trim()

        if (cleanContent.startsWith("{") && cleanContent.endsWith("}")) {
            try {
                val json = JSONObject(cleanContent)
                val driverName = json.optString("chofer", json.optString("driver", ""))
                val companyName = json.optString("empresa", "Agroberries")
                val plate = json.optString("placas", json.optString("placa", ""))
                val numEcon = json.optString("numEcon", json.optString("economico", ""))

                if (driverName.isNotEmpty() || plate.isNotEmpty()) {
                    isQrScanned = true
                    fillDataFields(driverName, companyName, plate)
                    return
                } else if (numEcon.isNotEmpty()) {
                    searchCompanyVehicleInDB(numEcon)
                    return
                }
            } catch (e: Exception) {
                Log.e("DriverDataFragment", "Error al procesar JSON del QR: ${e.message}")
            }
        }

        val parts = cleanContent.split(Regex("[,;|]"))
        if (parts.size >= 3) {
            isQrScanned = true
            fillDataFields(
                driverName = parts[0].trim(),
                companyName = if (parts[1].trim().isNotEmpty()) parts[1].trim() else "Agroberries",
                plate = parts[2].trim()
            )
            return
        }

        if (cleanContent.isNotEmpty()) {
            searchCompanyVehicleInDB(cleanContent)
        } else {
            Toast.makeText(requireContext(), "El código QR está vacío", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchCompanyVehicleInDB(numEcon: String) {
        lifecycleScope.launch {
            try {
                val vehicle = recordsRepository.getCompanyVehicleByNumEcon(numEcon)

                if (vehicle != null) {
                    val driverName = vehicle.vNombreAfc ?: ""
                    val companyName = "Agroberries"
                    val plate = vehicle.vPlacasAfi ?: ""

                    isQrScanned = true

                    fillDataFields(
                        driverName = driverName,
                        companyName = companyName,
                        plate = plate
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "El activo $numEcon no se encuentra registrado en el catálogo local.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al consultar el catálogo local: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun fillDataFields(driverName: String, companyName: String, plate: String) {
        if (driverName.isNotEmpty()) binding.etDriverName.setText(driverName)
        if (plate.isNotEmpty()) binding.etPlate.setText(plate)

        binding.etCompanionName.setText("N/A")

        // Habilita visibilidad y prepara el campo de kilometraje
        if (isQrScanned) {
            binding.tilMileageLayout.visibility = View.VISIBLE
            binding.etMileage.setText("") // Limpia valores residuales
            binding.etMileage.requestFocus()
        }

        if (companyName.isNotEmpty()) {
            val adapter = binding.spinnerCompany.adapter as? ArrayAdapter<String>
            if (adapter != null) {
                var pos = -1
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).equals(companyName, ignoreCase = true)) {
                        pos = i
                        break
                    }
                }

                if (pos >= 0) {
                    binding.spinnerCompany.setSelection(pos)
                } else {
                    val otherPos = adapter.getPosition("Otra")
                    if (otherPos >= 0) {
                        binding.spinnerCompany.setSelection(otherPos)
                    }
                    binding.etOtherCompanyLayout.visibility = View.VISIBLE
                    binding.etOtherCompany.setText(companyName)
                }
            }
        }

        Toast.makeText(requireContext(), "Datos autocompletados. Ingresa el kilometraje.", Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData(
        dIngresoInv: String,
        vNombreChofInv: String,
        vAcompanianteInv: String,
        vEmpresaInv: String,
        cPlacaInv: String,
        vMotivoInv: String,
        dHringresoInv: String,
        cMovimientoInv: String,
        mileageText: String
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
                "Por favor, selecciona un motivo válido y una empresa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val controlLog = 0L
        val dHrsalidaInv = ""
        val cCodigoUsu = user.trim().uppercase()
        val isSynced = 0

        // Parseo explícito
        val nKilometraje = mileageText.toIntOrNull() ?: 0
        Log.d("DRIVER_DATA_DEBUG", "Texto capturado de etMileage: '$mileageText' -> Parseado a Int: $nKilometraje")

        val vehicleRecord = RecordModel(
            controlLog,
            dIngresoInv,
            vNombreChofInv,
            vAcompanianteInv,
            selectedCompany,
            cPlacaInv,
            selectedReason,
            dHringresoInv,
            dHrsalidaInv,
            cCodigoUsu,
            cMovimientoInv,
            isSynced,
            nKilometraje
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

                binding.etDriverName.text?.clear()
                binding.etCompanionName.text?.clear()
                if (binding.etOtherCompany.text.toString().trim().isNotEmpty()) {
                    binding.etOtherCompany.text?.clear()
                    binding.spinnerCompany.setSelection(0)
                } else {
                    binding.spinnerCompany.setSelection(0)
                }
                binding.etPlate.text?.clear()

                binding.etMileage.text?.clear()
                binding.tilMileageLayout.visibility = View.GONE

                if (binding.etOtherReason.text.toString().trim().isNotEmpty()) {
                    binding.etOtherReason.text?.clear()
                    binding.spinnerReason.setSelection(0)
                } else {
                    binding.spinnerReason.setSelection(0)
                }
                binding.tvTimeIn.text = null
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al guardar en la BD: ${e.message}",
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
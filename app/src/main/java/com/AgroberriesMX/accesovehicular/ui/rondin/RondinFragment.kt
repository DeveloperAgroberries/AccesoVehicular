package com.AgroberriesMX.accesovehicular.ui.rondin

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.ActivityInfo // Importar para la orientación de la cámara
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AgroberriesMX.accesovehicular.R
import com.AgroberriesMX.accesovehicular.data.local.DatabaseHelper
import com.AgroberriesMX.accesovehicular.domain.model.RondinLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices // Corrected import

// NUEVAS IMPORTACIONES PARA LA UBICACIÓN ACTUALIZADA
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority // Para la versión moderna de LocationRequest
import java.util.concurrent.TimeUnit // Para definir tiempos en LocationRequest

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject // Aunque ahora no leemos JSON del QR de ubicación, la importación se mantiene
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ... otras importaciones ...
import com.journeyapps.barcodescanner.ScanContract // <-- Asegúrate de tener esta
import com.journeyapps.barcodescanner.ScanOptions // <-- Y esta

@AndroidEntryPoint
class RondinFragment : Fragment() {

    private lateinit var tvDateToday: TextView
    private lateinit var etRondinUser: EditText
    private lateinit var btnScanQr: ImageButton // Este botón para escanear el usuario (si aplica)
    private lateinit var btnIniRondin: Button // Este botón inicia el rondín/escanea siguiente ubicación
    private lateinit var btnFinRondin: Button
    private lateinit var recyclerViewRondinLocations: RecyclerView
    private lateinit var rondinAdapter: RondinLocationAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: DatabaseHelper

    private val currentRondinLocations = mutableListOf<RondinLocation>()
    private var isRondinActive: Boolean = false
    private var currentRondinUserCode: String? = null
    private var scannedLocationName: String? = null
    private var isFinishingRondinFlag: Boolean = false

    // --- NUEVOS OBJETOS PARA ACTUALIZACIONES DE UBICACIÓN ---
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isRequestingLocationUpdates = false // Bandera para controlar si ya estamos pidiendo updates


    // Lanzador para permisos de ubicación
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(requireContext(), "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
            // Si los permisos se concedieron, ahora sí, intentamos obtener la ubicación más reciente
            // Usamos la bandera temporal para saber si es el fin de rondín.
            val nameToUse = if (isFinishingRondinFlag) "Fin de Rondín" else scannedLocationName
            nameToUse?.let { name ->
                // NOTA: 'getLocationAndSave' se ha transformado en 'startLocationUpdates' que es llamada por 'checkLocationPermissionsAndGetLocation'
                // Aquí solo necesitamos iniciar la cadena de obtención de ubicación.
                startLocationUpdates() // <-- Inicia la solicitud de ubicación
            } ?: run {
                Toast.makeText(requireContext(), "Error: Nombre de ubicación no disponible tras conceder permiso.", Toast.LENGTH_LONG).show()
                updateRondinUIState()
            }
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado. No se puede obtener el GPS.", Toast.LENGTH_LONG).show()
            scannedLocationName = null
            isFinishingRondinFlag = false
            Toast.makeText(requireContext(), "No se pudo registrar la ubicación sin permiso de GPS.", Toast.LENGTH_LONG).show()
            if (!isRondinActive) {
                updateRondinUIState()
            }
        }
    }

    // Lanzador para permisos de cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Este lanzador de permisos de cámara se usa para AMBOS tipos de escaneo (usuario y ubicación).
            // Necesitamos saber cuál se solicitó.
            // Una forma simple es verificar si el rondín está activo o qué botón se clicó.
            // Para simplicidad, asumo que solo se usa para el escaneo de ubicación aquí.
            // Si también lo usas para el escaneo de usuario, tendrías que refinar la lógica.
            startQrScannerForLocation() // Asumiendo que es para ubicación después de permisos
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado. No se puede escanear QR.", Toast.LENGTH_LONG).show()
            if (!isRondinActive) {
                updateRondinUIState()
            }
        }
    }

    // Lanzador para el escáner de QR (USUARIO INICIAL) - Puedes mantenerlo para un botón específico de "escanear usuario"
    private val barcodeLauncherForUser = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo de QR de usuario cancelado", Toast.LENGTH_SHORT).show()
        } else {
            etRondinUser.setText(result.contents)
            Toast.makeText(requireContext(), "Código QR de usuario escaneado: " + result.contents, Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador para el escáner de QR (UBICACIÓN/PUNTO DE CONTROL)
    private val barcodeLauncherForLocation = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Escaneo de QR de ubicación cancelado", Toast.LENGTH_SHORT).show()
            scannedLocationName = null
            if (!isRondinActive) {
                updateRondinUIState()
            }
        } else {
            val qrContent = result.contents // <-- Aquí obtienes la cadena directamente
            Log.d("QR_SCAN", "Contenido del QR escaneado: $qrContent")
           // Toast.makeText(requireContext(), "QR: $qrContent", Toast.LENGTH_LONG).show() // Para depuración

            // --- CAMBIO CLAVE AQUÍ ---
            // Ya NO intentamos parsear un JSON.
            // Asumimos que qrContent es directamente el nombre del lugar.
            scannedLocationName = qrContent // <--- Asigna el contenido del QR directamente como nombre del lugar

            isFinishingRondinFlag = false

            // Si es el primer escaneo de ubicación y el rondín no está activo, activarlo
            if (!isRondinActive) {
                val userCode = etRondinUser.text.toString().trim()
                if (userCode.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor, ingresa o escanea el Nombre de Usuario para el Rondín.", Toast.LENGTH_LONG).show()
                    updateRondinUIState()
                    return@registerForActivityResult
                }
                currentRondinUserCode = userCode
                isRondinActive = true
                Toast.makeText(requireContext(), getString(R.string.rondin_iniciado), Toast.LENGTH_SHORT).show()
                currentRondinLocations.clear()
                rondinAdapter.updateData(currentRondinLocations)
                updateRondinUIState()
            }

            // Ahora, solicitamos permisos de ubicación y obtenemos el GPS para guardar el punto.
            checkLocationPermissionsAndGetLocation() // <-- Esto ahora iniciará las actualizaciones de ubicación
        }
    }

    // Se ha añadido el método onCreate para inicializar LocationRequest y LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configura el LocationRequest aquí
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(5)) // Intervalo cada 5 segundos
            .setMinUpdateDistanceMeters(3f) // Opcional: solo si te has movido al menos 3 metros
            .setDurationMillis(TimeUnit.SECONDS.toMillis(10)) // Detenerse después de 10 segundos si no hay una buena lectura
            .build()

        // Configura el LocationCallback aquí
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Una vez que obtenemos una ubicación, la usamos y detenemos las actualizaciones.
                    if (isRequestingLocationUpdates) { // Asegura que solo se procese si estamos esperando una actualización
                        val nameToSave = if (isFinishingRondinFlag) "Fin de Rondín" else scannedLocationName
                        nameToSave?.let { name ->
                            // Solo guardamos si tenemos un nombre válido y los permisos
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                currentRondinUserCode?.let { userCode ->
                                    val loggedUser = getLoggedUserCode()
                                    saveLocationToDatabase(
                                        codigoUsuRon = userCode,
                                        latGpsRon = location.latitude,
                                        longGpsRon = location.longitude,
                                        nomUbicacionRon = name,
                                        usuModRon = loggedUser,
                                        isFinishingRondin = isFinishingRondinFlag
                                    )
                                    // Muestra el Toast de "Ubicación registrada" aquí, después de guardar con éxito.
                                    if (!isFinishingRondinFlag) {
                                        //Toast.makeText(requireContext(), "Ubicación '$name' registrada con GPS.", Toast.LENGTH_SHORT).show()
                                        Toast.makeText(requireContext(), "Ubicación '$name' registrada correctamente.", Toast.LENGTH_SHORT).show()
                                    }
                                } ?: run {
                                    Toast.makeText(requireContext(), "Error: Usuario del rondín no definido. No se pudo guardar.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Permiso de ubicación no concedido. No se puede guardar el punto GPS.", Toast.LENGTH_LONG).show()
                            }
                        }

                        // Limpiar y detener la solicitud después de obtener una ubicación válida
                        stopLocationUpdates()
                        scannedLocationName = null // Limpiar después de usar
                        isFinishingRondinFlag = false // Resetear la bandera
                    }
                } ?: run {
                    // Si onLocationResult se llama pero no hay última ubicación, intentar de nuevo o manejar el error
                    Log.w("RondinFragment", "LocationResult sin última ubicación. Esperando otra actualización.")
                    // Podrías añadir un contador o timeout aquí para evitar esperas infinitas.
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rondin, container, false)

        tvDateToday = view.findViewById(R.id.tvDateToday)
        etRondinUser = view.findViewById(R.id.etRondinUser)
        btnScanQr = view.findViewById(R.id.btnScanQr)
        btnIniRondin = view.findViewById(R.id.btnIniRondin)
        btnFinRondin = view.findViewById(R.id.btnFinRondin)
        recyclerViewRondinLocations = view.findViewById(R.id.recyclerViewRondinLocations)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        dbHelper = DatabaseHelper(requireContext())

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDateToday.text = dateFormat.format(Date())

        rondinAdapter = RondinLocationAdapter(currentRondinLocations)
        recyclerViewRondinLocations.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRondinLocations.adapter = rondinAdapter

        // Este botón (btnScanQr) lo puedes usar si quieres un escaneo de usuario separado
        btnScanQr.setOnClickListener {
            checkCameraPermissionAndStartScannerForUser()
        }

        // --- Lógica del botón "Iniciar Rondín" / "Escanear Siguiente Ubicación" ---
        btnIniRondin.setOnClickListener {
            if (!isRondinActive) {
                val rondinUserCode = etRondinUser.text.toString().trim()
                if (rondinUserCode.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor, ingresa o escanea el Nombre de Usuario para el Rondín.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                checkCameraPermissionAndStartScannerForLocation() // Abre la cámara para el primer QR de ubicación
            } else {
                checkCameraPermissionAndStartScannerForLocation() // Abre la cámara para la siguiente ubicación
            }
        }

        btnFinRondin.setOnClickListener {
            if (isRondinActive && currentRondinUserCode != null) {
                scannedLocationName = "Fin de Rondín"
                isFinishingRondinFlag = true
                checkLocationPermissionsAndGetLocation() // <-- Esto ahora iniciará las actualizaciones de ubicación para el fin de rondín
            } else {
                Toast.makeText(requireContext(), getString(R.string.rondin_no_iniciado), Toast.LENGTH_SHORT).show()
            }
        }

        updateRondinUIState() // Asegura que la UI esté en el estado inicial correcto
        return view
    }

    // Asegúrate de detener las actualizaciones de ubicación cuando el fragmento no está activo
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Asegúrate de detener las actualizaciones si el fragmento se destruye
        stopLocationUpdates()
    }

    private fun updateRondinUIState() {
        if (isRondinActive) {
            btnIniRondin.text = getString(R.string.scanNextLocation) // "Escanear Siguiente Ubicación"
            btnFinRondin.isEnabled = true
            etRondinUser.isEnabled = false // Deshabilita el EditText
            btnScanQr.isEnabled = false // Deshabilita el botón de escanear usuario
        } else {
            btnIniRondin.text = getString(R.string.iniciarRondin) // "Iniciar Rondín"
            btnFinRondin.isEnabled = false
            etRondinUser.isEnabled = true // Habilita el EditText
            btnScanQr.isEnabled = true // Habilita el botón de escanear usuario
            etRondinUser.text?.clear() // Limpia el texto del usuario al finalizar el rondín
            currentRondinUserCode = null // Restablece el usuario si el rondín termina
            etRondinUser.isCursorVisible = true
        }
    }

    private fun checkCameraPermissionAndStartScannerForUser() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQrScannerForUser()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startQrScannerForUser() {
        val options = ScanOptions()
        options.setPrompt("Escanea el código QR del usuario")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.setBarcodeImageEnabled(true)
        barcodeLauncherForUser.launch(options)
    }

    private fun checkCameraPermissionAndStartScannerForLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQrScannerForLocation()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startQrScannerForLocation() {
        val options = ScanOptions()
        options.setPrompt("Escanea el código QR del punto de control")
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(true)
        //options.setDesiredOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) // Puedes descomentar si prefieres orientación horizontal fija
        barcodeLauncherForLocation.launch(options)
    }

    // --- FUNCIONES DE UBICACIÓN ACTUALIZADAS ---
    // Esta función ahora solo verifica permisos y, si están OK, inicia las actualizaciones
    private fun checkLocationPermissionsAndGetLocation() {
        val hasFineLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            startLocationUpdates() // <-- Inicia la solicitud de ubicación
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Esta función inicia las actualizaciones de ubicación activas
    private fun startLocationUpdates() {
        // Solo inicia si no estamos ya pidiendo actualizaciones
        if (!isRequestingLocationUpdates) {
            // Re-verifica permisos justo antes de la solicitud (buena práctica)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permisos de ubicación insuficientes para iniciar actualizaciones.", Toast.LENGTH_SHORT).show()
                return
            }

            // Aquí es donde se solicita la ubicación activa
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                .addOnSuccessListener {
                    isRequestingLocationUpdates = true
                    //Toast.makeText(requireContext(), "Obteniendo ubicación más reciente...", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("RondinFragment", "Error al iniciar requestLocationUpdates: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error al iniciar búsqueda de GPS: ${e.message}", Toast.LENGTH_LONG).show()
                    isRequestingLocationUpdates = false
                    // Si falla el inicio de updates, revertir la UI si el rondín no estaba activo
                    if (!isRondinActive) {
                        updateRondinUIState()
                    }
                }
        }
    }

    // Esta función detiene las actualizaciones de ubicación
    private fun stopLocationUpdates() {
        if (isRequestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnSuccessListener {
                    isRequestingLocationUpdates = false
                    Log.d("RondinFragment", "Actualizaciones de ubicación detenidas.")
                }
                .addOnFailureListener { e ->
                    Log.e("RondinFragment", "Error al detener requestLocationUpdates: ${e.message}", e)
                }
        }
    }

    // `saveLocationToDatabase` ahora es llamada EXCLUSIVAMENTE desde `locationCallback.onLocationResult`
    // (a través de la verificación de `isRequestingLocationUpdates`) Ricardo Dimas 23/06/2025
    private fun saveLocationToDatabase(
        codigoUsuRon: String,
        latGpsRon: Double,
        longGpsRon: Double,
        nomUbicacionRon: String,
        usuModRon: String,
        isFinishingRondin: Boolean
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
                val currentDateTime = dateFormat.format(Date())

                val newLocation = RondinLocation(
                    codigoUsuRon = codigoUsuRon,
                    fechaRon = currentDateTime,
                    latGpsRon = latGpsRon,
                    longGpsRon = longGpsRon,
                    nomUbicacionRon = nomUbicacionRon,
                    usuModRon = usuModRon
                )
                val insertedId = dbHelper.insertRondinLocation(newLocation)

                withContext(Dispatchers.Main) {
                    if (insertedId > -1) {
                        // --- ¡AQUÍ ESTÁ EL LOG PARA VER LOS DATOS INSERTADOS! ---
                        //Log.d("RondinDB", "Rondín guardado localmente (ID: $insertedId): $newLocation")
                        // -----------------------------------------------------

                        if (!isFinishingRondin) {
                            // El Toast de "Ubicación registrada" ahora se muestra en el LocationCallback
                            currentRondinLocations.add(0, newLocation.copy(idRondinRon = insertedId.toInt()))
                            rondinAdapter.updateData(currentRondinLocations)
                            recyclerViewRondinLocations.scrollToPosition(0)
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.rondin_terminado_exito), Toast.LENGTH_LONG).show()
                            currentRondinLocations.clear()
                            rondinAdapter.updateData(currentRondinLocations)
                            isRondinActive = false
                            updateRondinUIState()
                        }

                    } else {
                        Toast.makeText(requireContext(), "Error al guardar ubicación en BD", Toast.LENGTH_SHORT).show()
                        updateRondinUIState() // Si falla al guardar, asegúrate de revertir el estado de la UI si el rondín no estaba activo.
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Excepción al guardar ubicación: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("RondinFragment", "Error al guardar ubicación", e)
                    updateRondinUIState() // Si hay una excepción al guardar, asegúrate de revertir el estado de la UI.
                }
            }
        }
    }

    private fun getLoggedUserCode(): String {
        val SESSION_PREFERENCES_KEY = "session_prefs"
        val LOGGED_USER_KEY = "logged_user"
        val sessionPrefs = requireActivity().getSharedPreferences(SESSION_PREFERENCES_KEY, AppCompatActivity.MODE_PRIVATE)
        return sessionPrefs.getString(LOGGED_USER_KEY, "APP_USER") ?: "APP_USER"
    }
}
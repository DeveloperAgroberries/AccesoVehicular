package com.AgroberriesMX.accesovehicular.ui.sync

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.AgroberriesMX.accesovehicular.databinding.FragmentSyncBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SyncFragment : Fragment() {

    private val syncViewModel by viewModels<SyncViewModel>()

    private var _binding: FragmentSyncBinding? = null

    private val binding get() = _binding!!
    private lateinit var sessionPrefs: SharedPreferences

    companion object{
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        initListeners()
        syncViewModel.loadPendingRecords()
        syncViewModel.loadPendingRondines()
    }

    private fun observeViewModel() {
        syncViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SyncState.Loading -> {
                    binding.pbs.visibility = View.VISIBLE
                }

                is SyncState.UploadSuccess -> {
                    binding.pbs.visibility = View.GONE
                    Toast.makeText(context, "${state.message}", Toast.LENGTH_LONG).show()
                    syncViewModel.clearState()
                }

                is SyncState.Error -> {
                    binding.pbs.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    syncViewModel.clearState()
                }

                else -> {
                    binding.pbs.visibility = View.GONE
                }
            }
        }

        syncViewModel.pendingRecords.observe(viewLifecycleOwner) { records ->
            val pendingCount = records.size
            binding.tvPending.text = if (pendingCount > 0) {
                pendingCount.toString()
            } else {
                "0"
            }
        }

        //RICARDO DIMAS
        syncViewModel.pendingRondines.observe(viewLifecycleOwner) { rondines ->
            val pendingCount = rondines.size
            binding.tvPendingRondinLocations.text = if (pendingCount > 0) {
                pendingCount.toString()
            } else {
                "0"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.cvDownload.setOnClickListener {
            binding.cvDownload.isEnabled = false
            binding.cvUpload.isEnabled = false
            binding.cvSyncRondin.isEnabled = false
            syncData()
        }

        binding.cvUpload.setOnClickListener {
            binding.cvDownload.isEnabled = false
            binding.cvUpload.isEnabled = false
            binding.cvSyncRondin.isEnabled = false
            uploadData()
        }

        //Ricardo Dimas - Rondines 23/06/2025
        binding.cvSyncRondin.setOnClickListener {
            binding.cvDownload.isEnabled = false
            binding.cvUpload.isEnabled = false
            binding.cvSyncRondin.isEnabled = false
            uploadDataRondines()
        }
    }

    private fun syncData() {
        lifecycleScope.launch {
            try {
                val response = getToken()
                if(response != null) {
                    syncViewModel.sync(response)
                } else {
                    Toast.makeText(context, "No puedes descargar tus datos, cierra sesion y vuelve a intentarlo", Toast.LENGTH_LONG).show()
                }

            } finally {
                binding.cvDownload.isEnabled = true
                binding.cvUpload.isEnabled = true
                binding.cvSyncRondin.isEnabled = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadData() {
        lifecycleScope.launch {
            try {
                val response = getToken()
                if(response != null){
                    syncViewModel.upload()
                } else {
                    Toast.makeText(context, "No puedes enviar tus datos, cierra sesion y vuelve a intentarlo", Toast.LENGTH_LONG).show()
                }
            } finally {
                binding.cvDownload.isEnabled = true
                binding.cvUpload.isEnabled = true
                binding.cvSyncRondin.isEnabled = true
            }
        }
    }

    //RICARDO DIMAS
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadDataRondines() {
        lifecycleScope.launch {
            try {
                val response = getToken()
                if(response != null){
                    syncViewModel.uploadRondines()
                } else {
                    Toast.makeText(context, "No puedes enviar tus datos, cierra sesion y vuelve a intentarlo", Toast.LENGTH_LONG).show()
                }
            } finally {
                binding.cvDownload.isEnabled = true
                binding.cvUpload.isEnabled = true
                binding.cvSyncRondin.isEnabled = true
            }
        }
    }

    private fun getToken(): String? {
        sessionPrefs = requireActivity().getSharedPreferences(
            SESSION_PREFERENCES_KEY, MODE_PRIVATE
        )
        return sessionPrefs.getString(PRIVATE_ACCESS_TOKEN_KEY, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
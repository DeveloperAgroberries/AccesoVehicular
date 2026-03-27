package com.AgroberriesMX.accesovehicular.ui.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.AgroberriesMX.accesovehicular.databinding.FragmentListRecordsBinding
import com.AgroberriesMX.accesovehicular.ui.SharedViewModel
import com.AgroberriesMX.accesovehicular.ui.list.adapter.ListRecordsAdapter
import com.AgroberriesMX.accesovehicular.ui.recordsdetail.ListRecordDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListRecordsFragment : Fragment() {
    private var _binding: FragmentListRecordsBinding? = null
    private val binding get() = _binding!!
    private val listRecordsViewModel by viewModels<ListRecordsViewModel>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private lateinit var recordsAdapter: ListRecordsAdapter
    private lateinit var searchView: SearchView

    private val getRecordDetailResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )   { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Llama al método para cargar los registros nuevamente
                listRecordsViewModel.loadTodayRecords() // O actualiza el RecyclerView directamente aquí
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initObservers()
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView = binding.svRecords

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    //listRecordsViewModel.loadTodayRecords() //Recarga solo los registros de hoy
                    listRecordsViewModel.loadAllRecords() // Actualiza la lista de todos los registros
                } else {
                    filterRecords(newText.trim().uppercase())
                }
                return true
            }
        })
    }

    private fun filterRecords(query: String) {
        listRecordsViewModel.searchRecords(query)
    }

    private fun initObservers() {
        listRecordsViewModel.filteredRecords.observe(viewLifecycleOwner) { records ->
            if(records != null){
                recordsAdapter.updateList(records)
            }
        }

        sharedViewModel.recordAdded.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                listRecordsViewModel.loadTodayRecords() //Recarga solo los registros de hoy
                listRecordsViewModel.loadAllRecords() // Actualiza la lista de todos los registros
                sharedViewModel.resetRecordAdded() // Reinicia el estado
            }
        }
    }

    private fun initUI() {
        initList()
        initUIState()
    }

    private fun initList() {
        // Inicializamos la RecyclerView
        recordsAdapter = ListRecordsAdapter(onItemSelected = { record ->
            // Cuando se selecciona un ítem, se navega a la actividad de detalles
            val intent = Intent(requireContext(), ListRecordDetailActivity::class.java)
            intent.putExtra("controlLog", record.controlLog) // Pasamos el numero de registro como extra
            getRecordDetailResult.launch(intent)
        })

        // Configuramos la RecyclerView
        binding.rvRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }


    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                listRecordsViewModel.todayRecords.observe(viewLifecycleOwner){todayRecords ->
                    if(todayRecords != null) {
                        recordsAdapter.updateList(todayRecords)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

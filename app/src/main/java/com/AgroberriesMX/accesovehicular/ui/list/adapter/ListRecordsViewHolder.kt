package com.AgroberriesMX.accesovehicular.ui.list.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.AgroberriesMX.accesovehicular.databinding.ItemEntryRecordBinding
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel

class ListRecordsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemEntryRecordBinding.bind(itemView)

    fun bind(record: RecordModel, onItemSelected: (RecordModel) -> Unit) {
        binding.tvPlate.text = record.cPlacaInv
        binding.tvEmpresa.text = record.vEmpresaInv
        binding.tvFecha.text = record.dIngresoInv
        binding.tvCheckIn.text = record.dHringresoInv
        binding.tvCheckOut.text = record.dHrsalidaInv
        binding.root.setOnClickListener{
            onItemSelected(record)
        }
    }
}
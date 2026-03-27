// Archivo: com/AgroberriesMX/accesovehicular/ui/rondin/RondinLocationAdapter.kt
package com.AgroberriesMX.accesovehicular.ui.rondin // ASEGÚRATE QUE ESTE PAQUETE SEA EL CORRECTO

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AgroberriesMX.accesovehicular.R // Asegúrate que esta importación sea correcta para tu proyecto
import com.AgroberriesMX.accesovehicular.domain.model.RondinLocation // Asegúrate que esta importación sea correcta
import java.util.Locale // ** AÑADE ESTA LÍNEA AQUÍ**

class RondinLocationAdapter(private var locations: List<RondinLocation>) :
    RecyclerView.Adapter<RondinLocationAdapter.RondinLocationViewHolder>() {

    // Clase interna que define las vistas de cada elemento de la lista (fila)
    class RondinLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvCoordinates: TextView = itemView.findViewById(R.id.tvCoordinates)
        val tvUser: TextView = itemView.findViewById(R.id.tvUser)

        fun bind(location: RondinLocation) {
            tvLocationName.text = location.nomUbicacionRon
            tvDateTime.text = "Fecha: ${location.fechaRon}" // Asumiendo que fechaRon ya incluye fecha y hora si es necesario
            tvCoordinates.text = String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", location.latGpsRon, location.longGpsRon)
            tvUser.text = "Usuario: ${location.codigoUsuRon}"
        }
    }

    // Crea nuevos ViewHolders (se llama pocas veces, solo cuando se necesita una nueva fila en pantalla)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RondinLocationViewHolder {
        // Infla el layout XML de un solo elemento (item_rondin_location.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rondin_location, parent, false)
        return RondinLocationViewHolder(view)
    }

    // Vincula los datos de un elemento a un ViewHolder existente (se llama muchas veces al hacer scroll)
    override fun onBindViewHolder(holder: RondinLocationViewHolder, position: Int) {
        // Obtiene el objeto RondinLocation de la lista en la posición actual
        val location = locations[position]
        // Llama al método bind del ViewHolder para actualizar las vistas con los datos
        holder.bind(location)
    }

    // Retorna el número total de elementos en la lista de datos
    override fun getItemCount(): Int {
        return locations.size
    }

    // Método para actualizar los datos del adaptador y notificar al RecyclerView que cambie la UI
    fun updateData(newLocations: List<RondinLocation>) {
        this.locations = newLocations
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado y debe redibujarse
    }
}
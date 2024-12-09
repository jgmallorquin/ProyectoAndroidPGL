package com.proyectopgl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TareaAdapter(
    private val tareas: MutableList<Tarea>,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Tarea) -> Unit,
    private val onComplete: (Tarea) -> Unit
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTareaTitulo: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        val textViewTareaDescripcion: TextView = itemView.findViewById(R.id.textViewTaskDescription)
        val textViewTareaDificultad: TextView = itemView.findViewById(R.id.textViewTaskDifficulty)
        val textViewTareaMonedas: TextView = itemView.findViewById(R.id.textViewTaskCoins)
        val btnCompletarTarea: ImageButton = itemView.findViewById(R.id.btn_completarTarea)
        val btnNoCompletarTarea: ImageButton = itemView.findViewById(R.id.btn_noCompletarTarea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.textViewTareaTitulo.text = tarea.nombre
        holder.textViewTareaDescripcion.text = tarea.descripcion
        holder.textViewTareaDificultad.text = tarea.dificultad
        holder.textViewTareaMonedas.text = tarea.monedas.toString()

        holder.itemView.setOnClickListener {
            onEdit(tarea)
        }

        holder.btnCompletarTarea.setOnClickListener {
            onComplete(tarea)
        }

        holder.btnNoCompletarTarea.setOnClickListener {
            (holder.itemView.context as TareasActivity).noCompletarTarea(tarea)
        }
    }

    override fun getItemCount() = tareas.size

    fun removeItem(position: Int) {
        tareas.removeAt(position)
        notifyItemRemoved(position)
    }
}

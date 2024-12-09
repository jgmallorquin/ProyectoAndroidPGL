package com.proyectopgl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyectopgl.utils.Sprite

class InventarioDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_inventario, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.listaInventario)
        recyclerView.layoutManager = GridLayoutManager(context, 3) // 3 columnas en la cuadrícula
        recyclerView.adapter = InventarioAdapter(
            Inventario.obtenerArticulos().toMutableList(), // Convertir a lista mutable
            Sprite(resources, R.drawable.img_articulos, 5, 5, 2f),
            requireActivity(),
            parentFragmentManager
        )
        return view
    }
}


package com.proyectopgl

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.proyectopgl.utils.Sprite
import kotlin.random.Random

class InventarioAdapter(
    private val articulos: MutableList<Articulo>,
    private val sprite: Sprite,
    private val activity: Activity,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    class InventarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemName: TextView = itemView.findViewById(R.id.item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario, parent, false)
        return InventarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.itemImage.setImageBitmap(sprite.obtenerSprite(articulo.fila, articulo.columna))
        holder.itemName.text = articulo.nombre

        holder.itemView.setOnClickListener {
            val sprite = articulo.sprite.obtenerSprite(articulo.fila, articulo.columna)
            val imageView = ImageView(activity)
            imageView.setImageBitmap(sprite)
            imageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Obtener la referencia a zonaMascota
                val zonaMascota = activity.findViewById<FrameLayout>(R.id.zonaMascota)
                // Generar posiciones aleatorias dentro de zonaMascota
                leftMargin = Random.nextInt(zonaMascota.width - sprite.width)
                topMargin = Random.nextInt(zonaMascota.height - sprite.height)
            }
            val zonaMascota = activity.findViewById<FrameLayout>(R.id.zonaMascota)
            zonaMascota.addView(imageView, 0)
            Log.d("InventarioAdapter", "ImageView added to zonaMascota")

            // Eliminar el artículo del inventario
            removeArticulo(position)
        }
    }

    override fun getItemCount() = articulos.size

    private fun removeArticulo(position: Int) {
        articulos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, articulos.size)
    }
}
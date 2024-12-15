package com.proyectopgl

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.proyectopgl.utils.Sprite

class TiendaAdapter(private val articulos: List<Articulo>, private val sprite: Sprite, private val mostrarBotonCompra: Boolean) :
    RecyclerView.Adapter<TiendaAdapter.TiendaViewHolder>() {

    class TiendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemName: TextView = itemView.findViewById(R.id.item_name)
        val buyButton: Button = itemView.findViewById(R.id.buy_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiendaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tienda, parent, false)
        return TiendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiendaViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.itemImage.setImageBitmap(sprite.obtenerSprite(articulo.fila, articulo.columna))
        holder.itemName.text = articulo.nombre

        if (mostrarBotonCompra) {
            holder.buyButton.text = articulo.precio

            // Ajustar el tamaño del icono y situarlo a la izquierda
            val icono = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_moneda)
            icono?.setBounds(0, 0, 32, 32)
            holder.buyButton.setCompoundDrawables(icono, null, null, null)
            holder.buyButton.compoundDrawablePadding = 4

            holder.buyButton.setOnClickListener {
                // Mostrar diálogo de confirmación
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Confirmación de compra")
                builder.setMessage("¿Deseas comprar ${articulo.nombre} por ${articulo.precio}?")
                builder.setPositiveButton("Sí") { dialog, _ ->
                    val monedasTextView = holder.itemView.rootView.findViewById<TextView>(R.id.monedas_cantidad)
                    val monedasActuales = monedasTextView.text.toString().toInt()
                    val precioArticulo = articulo.precio.toInt()

                    if (monedasActuales >= precioArticulo) {
                        val nuevasMonedas = monedasActuales - precioArticulo
                        monedasTextView.text = nuevasMonedas.toString()
                        Inventario.agregarArticulo(articulo)

                        // Obtener el nombre de usuario desde el Intent
                        val intent = (holder.itemView.context as Activity).intent
                        val nombreUsuario = intent.getStringExtra("usuario")

                        // Actualizar la base de datos
                        val dbHelper = DBHelper(holder.itemView.context)
                        if (nombreUsuario != null) {
                            dbHelper.actualizarMonedasUsuario(nombreUsuario, nuevasMonedas)
                        }
                    } else {
                        // Mostrar mensaje de error si no hay suficientes monedas
                        Toast.makeText(holder.itemView.context, "No tienes suficientes monedas", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        } else {
            holder.buyButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = articulos.size
}

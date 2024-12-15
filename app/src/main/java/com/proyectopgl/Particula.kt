package com.proyectopgl

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.proyectopgl.utils.Sprite

class Particula(
    private val frameLayout: FrameLayout,
    spriteSheetResId: Int,
    numColumnas: Int,
    numFilas: Int, private val escala: Float
) {

    private val sprite: Sprite = Sprite(frameLayout.resources, spriteSheetResId, numColumnas, numFilas)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var particulasImageView: ImageView

    fun inicializar() {
        if (sprite.obtenerNumColumnas() <= 0 || sprite.obtenerNumFilas() <= 0) {
            throw IllegalArgumentException("numColumnas y numFilas deben ser mayores que cero")
        }
        crearImageView()
    }

    private fun crearImageView() {
        particulasImageView = ImageView(frameLayout.context)
        val layoutParams = FrameLayout.LayoutParams(
            (sprite.obtenerAncho() * escala).toInt(),
            (sprite.obtenerAlto() * escala).toInt()
        )
        // Añadir la vista en la posición 0 para que tenga una prioridad inferior al resto de imágenes
        frameLayout.addView(particulasImageView, 0, layoutParams)
    }

    fun lanzarAnimacion(x: Float, y: Float, duracion: Long) {
        val centroX = x - (sprite.obtenerAncho() * escala) / 2
        val centroY = y - (sprite.obtenerAlto() * escala) / 2

        particulasImageView.x = centroX
        particulasImageView.y = centroY

        var fila = 0
        var columna = 0
        handler.post(object : Runnable {
            override fun run() {
                val nuevoSprite = sprite.obtenerSprite(fila, columna)
                particulasImageView.setImageBitmap(nuevoSprite)
                particulasImageView.scaleType = ImageView.ScaleType.FIT_XY
                particulasImageView.drawable.isFilterBitmap = false

                columna++
                if (columna >= sprite.obtenerNumColumnas()) {
                    columna = 0
                    fila++
                    if (fila >= sprite.obtenerNumFilas()) {
                        fila = 0
                    }
                }

                // Cambia el sprite cada 100 milisegundos
                handler.postDelayed(this, 100)
            }
        })

        // Detener la animación después de la duración especificada
        handler.postDelayed({
            particulasImageView.visibility = View.INVISIBLE
            particulasImageView.alpha = 1f // Reiniciar la opacidad para la próxima animación
        }, duracion)
    }
}


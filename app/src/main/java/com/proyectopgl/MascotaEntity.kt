package com.proyectopgl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.proyectopgl.utils.Sprite
import android.content.res.Resources
import kotlin.random.Random

class MascotaEntity(
    val nombre: String,
    val vida: Int,
    val hambre: Int,
    val experiencia: Int,
    var nivel: Int,
    resources: Resources,
    spriteSheetResId: Int,
    private val imagenMascota: ImageView,
    private val zonaMascota: FrameLayout,
    private val layoutMascota: LinearLayout,
    private val imagenExpresion: ImageView,
    private val escala: Float = 2.0f
) {
    private var estadoActual: EstadoMascota = EstadoMascota.PASEAR
    val sprite: Sprite = Sprite(resources, spriteSheetResId, 5, 5, escala)
    val spriteExpresiones: Sprite = Sprite(resources, R.drawable.img_expresiones, 5, 5, escala)
    private val handler = Handler(Looper.getMainLooper())
    private var frame = 0
    private var filaAnimacion = 0
    private val velocidad: Long = 1500

    private fun mostrarSprite(fila: Int, columna: Int) {
        val spriteBitmap = sprite.obtenerSprite(fila, columna)
        imagenMascota.setImageBitmap(spriteBitmap)
    }

    fun cambiarEstado(nuevoEstado: EstadoMascota) {
        estadoActual = nuevoEstado
        handler.removeCallbacksAndMessages(null)
        when (nuevoEstado) {
            EstadoMascota.PASEAR -> pasear()
            EstadoMascota.JUGAR -> jugar()
            EstadoMascota.COMER -> comer()
            EstadoMascota.ATACAR -> atacar()
            EstadoMascota.DEFENDER -> defender()
            EstadoMascota.SEGUIR -> Log.d("Mascota", "Estado cambiado a SEGUIR")
            EstadoMascota.FELIZ -> feliz()
        }
    }

    private fun pasear() {
        println("$nombre está paseando.")
        setAnimacion(1)
        moverAleatoriamente()
    }

    private fun jugar() {
        println("$nombre está jugando.")
        setAnimacion(2)
    }

    private fun comer() {
        println("$nombre está comiendo.")
        setAnimacion(3)
    }

    private fun atacar() {
        println("$nombre está atacando.")
        setAnimacion(4)
    }

    private fun defender() {
        println("$nombre está defendiendo.")
        setAnimacion(5)
    }

    private fun feliz() {
        setAnimacion(3)
        setExpresion(0, 0, 3000)
        handler.postDelayed({ cambiarEstado(EstadoMascota.PASEAR) }, 5000)
    }

    fun setAnimacion(fila: Int) {
        filaAnimacion = fila
        frame = 0
        handler.removeCallbacksAndMessages(null)
        iniciarAnimacion()
        println("$nombre tiene la animación número $fila.")
    }

    private fun iniciarAnimacion() {
        handler.post(object : Runnable {
            override fun run() {
                mostrarSprite(filaAnimacion, frame)
                frame = (frame + 1) % 5
                handler.postDelayed(this, 100)
            }
        })
    }

    fun seguir(x: Float, y: Float) {
        cambiarEstado(EstadoMascota.SEGUIR)
        setExpresion(0, 3, 3000)
        Log.d("Mascota", "Moviendo a: ($x, $y)")

        handler.post {
            val animX = ObjectAnimator.ofFloat(layoutMascota, "x", layoutMascota.x, x)
            val animY = ObjectAnimator.ofFloat(layoutMascota, "y", layoutMascota.y, y)

            animX.duration = velocidad
            animY.duration = velocidad

            animX.start()
            animY.start()

            setAnimacion(2)

            if (x > layoutMascota.x) {
                layoutMascota.scaleX = 1f
            } else {
                layoutMascota.scaleX = -1f
            }

            // Esperar 5 segundos y luego volver al estado PASEAR
            handler.postDelayed({
                Log.d("Mascota", "Volviendo al estado PASEAR")
                cambiarEstado(EstadoMascota.PASEAR)
            }, 5000)
        }
    }

    private fun moverAleatoriamente() {
        handler.postDelayed({
            val maxX = zonaMascota.width - layoutMascota.width
            val maxY = zonaMascota.height - layoutMascota.height

            if (maxX > 0 && maxY > 0) {
                val nuevaX = Random.nextInt(0, maxX)
                val nuevaY = Random.nextInt(0, maxY)

                val animX = ObjectAnimator.ofFloat(layoutMascota, "x", layoutMascota.x, nuevaX.toFloat())
                val animY = ObjectAnimator.ofFloat(layoutMascota, "y", layoutMascota.y, nuevaY.toFloat())

                animX.duration = velocidad
                animY.duration = velocidad

                animX.start()
                animY.start()

                if (nuevaX > layoutMascota.x) {
                    layoutMascota.scaleX = 1f
                } else {
                    layoutMascota.scaleX = -1f
                }
            }

            moverAleatoriamente()
        }, velocidad)
    }

    fun setExpresion(numFila: Int, numColumna: Int, tiempo: Long) {
        val expresionBitmap = spriteExpresiones.obtenerSprite(numFila, numColumna)
        imagenExpresion.setImageBitmap(expresionBitmap)
        imagenExpresion.visibility = View.VISIBLE
        Log.d("Mascota", "Expresión cambiada a: fila $numFila, columna $numColumna")

        val animator = ObjectAnimator.ofFloat(imagenExpresion, "alpha", 1f, 0f)
        animator.duration = tiempo
        animator.start()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imagenExpresion.visibility = View.INVISIBLE
                Log.d("Mascota", "Expresión vuelta a invisible")
            }
        })
    }

    fun obtenerTamanioMascota(): Pair<Int, Int> {
        return Pair(layoutMascota.width, layoutMascota.height)
    }

    fun obtenerEscala(): Float {
        return escala
    }
}

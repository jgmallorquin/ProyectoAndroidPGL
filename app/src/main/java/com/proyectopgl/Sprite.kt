package com.proyectopgl.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.res.Resources

class Sprite(
    private val resources: Resources,
    private val spriteSheetResId: Int,
    private val numColumnas: Int,
    private val numFilas: Int,
    private val escala: Float = 1.0f
) {

    private lateinit var spriteSheet: Bitmap
    private var spriteWidth: Int = 0
    private var spriteHeight: Int = 0

    init {
        inicializarSpriteSheet()
    }

    private fun inicializarSpriteSheet() {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        spriteSheet = BitmapFactory.decodeResource(resources, spriteSheetResId, options)
        spriteWidth = spriteSheet.width / numColumnas
        spriteHeight = spriteSheet.height / numFilas
    }

    fun obtenerSprite(fila: Int, columna: Int): Bitmap {
        val sprite = Bitmap.createBitmap(spriteSheet, columna * spriteWidth, fila * spriteHeight, spriteWidth, spriteHeight)
        return Bitmap.createScaledBitmap(sprite, (spriteWidth * escala).toInt(), (spriteHeight * escala).toInt(), false)
    }

    fun obtenerAncho(): Int {
        return (spriteWidth * escala).toInt()
    }

    fun obtenerAlto(): Int {
        return (spriteHeight * escala).toInt()
    }

    fun obtenerNumColumnas(): Int {
        return numColumnas
    }

    fun obtenerNumFilas(): Int {
        return numFilas
    }
}

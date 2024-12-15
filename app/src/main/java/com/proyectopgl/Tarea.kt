package com.proyectopgl

data class Tarea(
    val id: Int,
    val usuario: String,
    val nombre: String,
    val descripcion: String,
    val dificultad: String,
    val duracionHoras: Int,
    val duracionMinutos: Int,
    val monedas: Int
)

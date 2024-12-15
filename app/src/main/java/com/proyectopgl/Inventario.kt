package com.proyectopgl

object Inventario {
    private val articulos = mutableListOf<Articulo>()

    fun agregarArticulo(articulo: Articulo) {
        articulos.add(articulo)
    }

    fun obtenerArticulos(): List<Articulo> {
        return articulos
    }
}

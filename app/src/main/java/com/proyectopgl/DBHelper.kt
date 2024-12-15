package com.proyectopgl

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, BD_NOMBRE, null, BD_VERSION) {

    companion object {
        private const val BD_NOMBRE = "baseDatos.db"
        private const val BD_VERSION = 1

        private const val TABLA_USUARIOS = "tabla_usuarios"
        private const val TU_COLUMNA_USUARIO = "usuario"
        private const val TU_COLUMNA_CLAVE = "clave"
        private const val TU_COLUMNA_MONEDAS = "monedas"

        private const val TABLA_MASCOTAS = "tabla_mascotas"
        private const val TM_COLUMNA_USUARIO = "usuario"
        private const val TM_COLUMNA_NOMBRE = "nombre"
        private const val TM_COLUMNA_VIDA = "vida"
        private const val TM_COLUMNA_HAMBRE = "hambre"
        private const val TM_COLUMNA_EXPERIENCIA = "experiencia"
        private const val TM_COLUMNA_NIVEL = "nivel"

        private const val TABLA_TAREAS = "tabla_tareas"
        private const val TT_COLUMNA_ID = "id"
        private const val TT_COLUMNA_USUARIO = "usuario"
        private const val TT_COLUMNA_NOMBRE = "nombre"
        private const val TT_COLUMNA_DESCRIPCION = "descripcion"
        private const val TT_COLUMNA_DIFICULTAD = "dificultad"
        private const val TT_COLUMNA_DURACION_HORAS = "duracion_horas"
        private const val TT_COLUMNA_DURACION_MINUTOS = "duracion_minutos"
        private const val TT_COLUMNA_MONEDAS = "monedas"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableUsuarios = ("CREATE TABLE $TABLA_USUARIOS ("
                + "$TU_COLUMNA_USUARIO TEXT PRIMARY KEY, "
                + "$TU_COLUMNA_CLAVE TEXT, "
                + "$TU_COLUMNA_MONEDAS INTEGER DEFAULT 0)")
        db.execSQL(createTableUsuarios)

        val createTableMascotas = ("CREATE TABLE $TABLA_MASCOTAS ("
                + "$TM_COLUMNA_USUARIO TEXT NOT NULL, "
                + "$TM_COLUMNA_NOMBRE TEXT NOT NULL, "
                + "$TM_COLUMNA_VIDA INTEGER NOT NULL, "
                + "$TM_COLUMNA_HAMBRE INTEGER NOT NULL, "
                + "$TM_COLUMNA_EXPERIENCIA INTEGER NOT NULL, "
                + "$TM_COLUMNA_NIVEL INTEGER NOT NULL)")
        db.execSQL(createTableMascotas)

        val createTableTareas = ("CREATE TABLE $TABLA_TAREAS ("
                + "$TT_COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$TT_COLUMNA_USUARIO TEXT, "
                + "$TT_COLUMNA_NOMBRE TEXT, "
                + "$TT_COLUMNA_DESCRIPCION TEXT, "
                + "$TT_COLUMNA_DIFICULTAD TEXT, "
                + "$TT_COLUMNA_DURACION_HORAS INTEGER, "
                + "$TT_COLUMNA_DURACION_MINUTOS INTEGER, "
                + "$TT_COLUMNA_MONEDAS INTEGER)")
        db.execSQL(createTableTareas)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLA_USUARIOS ADD COLUMN $TU_COLUMNA_MONEDAS INTEGER DEFAULT 0")
        }
    }

    fun actualizarMonedasUsuario(usuario: String, monedas: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(TU_COLUMNA_MONEDAS, monedas)
        }
        db.update(TABLA_USUARIOS, contentValues, "$TU_COLUMNA_USUARIO = ?", arrayOf(usuario))
    }

    fun insertarUsuario(usuario: String, clave: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(TU_COLUMNA_USUARIO, usuario)
            put(TU_COLUMNA_CLAVE, clave)
            put(TU_COLUMNA_MONEDAS, 0)
        }
        return db.insert(TABLA_USUARIOS, null, values)
    }

    fun existeUsuario(usuario: String, clave: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_USUARIOS,
            arrayOf(TU_COLUMNA_USUARIO),
            "$TU_COLUMNA_USUARIO = ? AND $TU_COLUMNA_CLAVE = ?",
            arrayOf(usuario, clave),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun insertarTarea(tarea: Tarea, usuario: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(TT_COLUMNA_USUARIO, usuario)
            put(TT_COLUMNA_NOMBRE, tarea.nombre)
            put(TT_COLUMNA_DESCRIPCION, tarea.descripcion)
            put(TT_COLUMNA_DIFICULTAD, tarea.dificultad)
            put(TT_COLUMNA_DURACION_HORAS, tarea.duracionHoras)
            put(TT_COLUMNA_DURACION_MINUTOS, tarea.duracionMinutos)
            put(TT_COLUMNA_MONEDAS, tarea.monedas)
        }
        return db.insert(TABLA_TAREAS, null, values)
    }

    fun obtenerTareas(usuario: String): List<Tarea> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_TAREAS,
            null,
            "$TT_COLUMNA_USUARIO = ?",
            arrayOf(usuario),
            null,
            null,
            null
        )
        val tareas = mutableListOf<Tarea>()
        if (cursor.moveToFirst()) {
            do {
                val tarea = Tarea(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(TT_COLUMNA_ID)),
                    usuario = cursor.getString(cursor.getColumnIndexOrThrow(TT_COLUMNA_USUARIO)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(TT_COLUMNA_NOMBRE)),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow(TT_COLUMNA_DESCRIPCION)),
                    dificultad = cursor.getString(cursor.getColumnIndexOrThrow(TT_COLUMNA_DIFICULTAD)),
                    duracionHoras = cursor.getInt(cursor.getColumnIndexOrThrow(TT_COLUMNA_DURACION_HORAS)),
                    duracionMinutos = cursor.getInt(cursor.getColumnIndexOrThrow(TT_COLUMNA_DURACION_MINUTOS)),
                    monedas = cursor.getInt(cursor.getColumnIndexOrThrow(TT_COLUMNA_MONEDAS))
                )
                tareas.add(tarea)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tareas
    }

    fun eliminarTarea(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_TAREAS, "$TT_COLUMNA_ID = ?", arrayOf(id.toString()))
    }

    fun insertarMascota(mascota: Mascota): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(TM_COLUMNA_USUARIO, mascota.usuario)
            put(TM_COLUMNA_NOMBRE, mascota.nombre)
            put(TM_COLUMNA_VIDA, mascota.vida)
            put(TM_COLUMNA_HAMBRE, mascota.hambre)
            put(TM_COLUMNA_EXPERIENCIA, mascota.experiencia)
            put(TM_COLUMNA_NIVEL, mascota.nivel)
        }
        return db.insert(TABLA_MASCOTAS, null, values)
    }

    fun actualizarMascota(mascota: Mascota) {
        val db = this.writableDatabase

        // Verificar si la mascota ya existe
        val cursor = db.query(
            TABLA_MASCOTAS,
            arrayOf(TM_COLUMNA_USUARIO),
            "$TM_COLUMNA_USUARIO = ?",
            arrayOf(mascota.usuario),
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()

        if (exists) {
            // Si la mascota existe, actualizarla
            val contentValues = ContentValues().apply {
                put(TM_COLUMNA_NOMBRE, mascota.nombre)
                put(TM_COLUMNA_VIDA, mascota.vida)
                put(TM_COLUMNA_HAMBRE, mascota.hambre)
                put(TM_COLUMNA_EXPERIENCIA, mascota.experiencia)
                put(TM_COLUMNA_NIVEL, mascota.nivel)
            }
            db.update(TABLA_MASCOTAS, contentValues, "$TM_COLUMNA_USUARIO = ?", arrayOf(mascota.usuario))
        } else {
            // Si la mascota no existe, insertarla
            val contentValues = ContentValues().apply {
                put(TM_COLUMNA_USUARIO, mascota.usuario)
                put(TM_COLUMNA_NOMBRE, mascota.nombre)
                put(TM_COLUMNA_VIDA, mascota.vida)
                put(TM_COLUMNA_HAMBRE, mascota.hambre)
                put(TM_COLUMNA_EXPERIENCIA, mascota.experiencia)
                put(TM_COLUMNA_NIVEL, mascota.nivel)
            }
            db.insert(TABLA_MASCOTAS, null, contentValues)
        }

        db.close()
    }

    fun obtenerMascota(usuario: String): Mascota? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_MASCOTAS,
            null,
            "$TM_COLUMNA_USUARIO = ?",
            arrayOf(usuario),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(TM_COLUMNA_NOMBRE))
            val vida = cursor.getInt(cursor.getColumnIndexOrThrow(TM_COLUMNA_VIDA))
            val hambre = cursor.getInt(cursor.getColumnIndexOrThrow(TM_COLUMNA_HAMBRE))
            val experiencia = cursor.getInt(cursor.getColumnIndexOrThrow(TM_COLUMNA_EXPERIENCIA))
            val nivel = cursor.getInt(cursor.getColumnIndexOrThrow(TM_COLUMNA_NIVEL))
            cursor.close()
            return Mascota(usuario, nombre, vida, hambre, experiencia, nivel)
        }
        cursor.close()
        return null
    }

    fun obtenerMonedasUsuario(usuario: String): Int {
        val db = this.readableDatabase
        val cursor = db.query( TABLA_USUARIOS, arrayOf(TU_COLUMNA_MONEDAS), "$TU_COLUMNA_USUARIO = ?", arrayOf(usuario), null, null, null )
        var monedas = 0
        if (cursor.moveToFirst()) {
            monedas = cursor.getInt(cursor.getColumnIndexOrThrow(TU_COLUMNA_MONEDAS))
        }
        cursor.close()
        return monedas
    }
}



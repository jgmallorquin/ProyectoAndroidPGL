package com.proyectopgl

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TareasActivity : AppCompatActivity() {
    private val tareas = mutableListOf<Tarea>()
    private var tareaIdCounter = 0
    private lateinit var tareaAdapter: TareaAdapter
    private lateinit var dbHelper: DBHelper
    private var tareaActual: Tarea? = null
    private lateinit var usuarioActual: String
    private var monedasTotales = 0
    private var experienciaTotal = 0
    private var nivelActual = 1
    private val niveles =
        listOf("Novato", "Principiante", "Intermedio", "Experto", "Maestro", "Dios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)

        dbHelper = DBHelper(this)
        usuarioActual = intent.getStringExtra("usuario") ?: ""

        cargarDatos()
        configurarRecyclerView()
        configurarBotones()
        configurarSeekBars()
        configurarDificultadButtons()
        menu()

        var nombreMascota = findViewById<TextView>(R.id.nombreMascota)
        if (nombreMascota.text.isEmpty() || nombreMascota.text.isBlank()) {
            nombreMascota.text = usuarioActual
        }

        findViewById<ImageButton>(R.id.botonSkinMascota).setOnClickListener {
            mostrarFormularioSkins()
        }

        findViewById<TextView>(R.id.nombreMascota).setOnClickListener {
            mostrarDialogoEditarNombre()
        }
    }

    private fun cargarDatos() {
        val mascota = dbHelper.obtenerMascota(usuarioActual)
        mascota?.let {
            findViewById<TextView>(R.id.nombreMascota).text = it.nombre
            nivelActual = it.nivel
            experienciaTotal = it.experiencia
            findViewById<TextView>(R.id.nivel_tv).text = "Lv $nivelActual - ${niveles.getOrElse(nivelActual - 1) { "Dios" }}"
            findViewById<ProgressBar>(R.id.barraExperiencia).progress = experienciaTotal
            findViewById<TextView>(R.id.cantidadExperiencia).text = "$experienciaTotal/1000"
            findViewById<ProgressBar>(R.id.barraVida).progress = it.vida
            findViewById<TextView>(R.id.cantidadVida).text = "${it.vida}/100"
            findViewById<ProgressBar>(R.id.barraHambre).progress = it.hambre
            findViewById<TextView>(R.id.cantidadHambre).text = "${it.hambre}/100"
        } ?: run {
            // Si no hay datos previos, establecer las barras de vida y hambre al 100%
            findViewById<ProgressBar>(R.id.barraVida).progress = 100
            findViewById<TextView>(R.id.cantidadVida).text = "100/100"
            findViewById<ProgressBar>(R.id.barraHambre).progress = 100
            findViewById<TextView>(R.id.cantidadHambre).text = "100/100"
        }

        // Obtener las monedas del usuario desde la base de datos
        monedasTotales = dbHelper.obtenerMonedasUsuario(usuarioActual)
        findViewById<TextView>(R.id.monedas_tv).text = monedasTotales.toString()

        tareas.addAll(dbHelper.obtenerTareas(usuarioActual))
        tareaIdCounter = tareas.size
    }

    private fun configurarRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.listaTareas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tareaAdapter = TareaAdapter(
            tareas,
            { position -> mostrarDialogoConfirmacion(position) },
            { tarea -> editarTarea(tarea) },
            { tarea -> completarTarea(tarea) })
        recyclerView.adapter = tareaAdapter

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
                    mostrarDialogoConfirmacion(viewHolder.adapterPosition)
            }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private fun configurarBotones() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            tareaActual = null
            findViewById<View>(R.id.formulario_tarea).visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btn_guardar_tarea).setOnClickListener { guardarTarea() }
    }

    private fun configurarSeekBars() {
        val dificultadTextView: TextView = findViewById(R.id.tv_dificultad_seleccionada)
        val duracionHorasSeekBar: SeekBar = findViewById(R.id.sb_duracion_horas)
        val duracionMinutosSeekBar: SeekBar = findViewById(R.id.sb_duracion_minutos)
        val duracionTotalTextView: TextView = findViewById(R.id.tv_duracion_total)

        val calcularMonedas = {
            val monedas = calcularMonedas(
                dificultadTextView.text.toString(),
                duracionHorasSeekBar.progress,
                duracionMinutosSeekBar.progress
            )
            findViewById<TextView>(R.id.tv_monedas_obtenidas).text = monedas.toString()
        }

        val actualizarDuracionTotal = {
            duracionTotalTextView.text = String.format(
                "%02d:%02d",
                duracionHorasSeekBar.progress,
                duracionMinutosSeekBar.progress
            )
        }

        dificultadTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calcularMonedas()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        duracionHorasSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                calcularMonedas(); actualizarDuracionTotal()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        duracionMinutosSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                calcularMonedas(); actualizarDuracionTotal()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun configurarDificultadButtons() {
        val dificultadTextView: TextView = findViewById(R.id.tv_dificultad_seleccionada)
        val actualizarDificultad = { dificultad: String -> dificultadTextView.text = dificultad }

        findViewById<ImageButton>(R.id.btn_dificultad_muy_facil).setOnClickListener {
            actualizarDificultad(
                "Muy Fácil"
            )
        }
        findViewById<ImageButton>(R.id.btn_dificultad_facil).setOnClickListener {
            actualizarDificultad(
                "Fácil"
            )
        }
        findViewById<ImageButton>(R.id.btn_dificultad_normal).setOnClickListener {
            actualizarDificultad(
                "Normal"
            )
        }
        findViewById<ImageButton>(R.id.btn_dificultad_dificil).setOnClickListener {
            actualizarDificultad(
                "Difícil"
            )
        }
        findViewById<ImageButton>(R.id.btn_dificultad_muy_dificil).setOnClickListener {
            actualizarDificultad(
                "Muy Difícil"
            )
        }
    }

    private fun menu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tareas
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mascota -> {
                    val intent = Intent(this, MascotaActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(intent)
                    true
                }
                R.id.tienda -> {
                    val intent = Intent(this, TiendaActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(intent)
                    true
                }
                R.id.salir -> {
                    guardarDatosMascota()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun guardarDatosMascota() {
        val nombreMascota = findViewById<TextView>(R.id.nombreMascota).text.toString()
        val vida = findViewById<ProgressBar>(R.id.barraVida).progress
        val hambre = findViewById<ProgressBar>(R.id.barraHambre).progress
        val mascota =
            Mascota(usuarioActual, nombreMascota, vida, hambre, experienciaTotal, nivelActual)
        dbHelper.actualizarMascota(mascota)
        dbHelper.actualizarMonedasUsuario(usuarioActual, monedasTotales)
    }

    private fun calcularMonedas(dificultad: String, duracionHoras: Int, duracionMinutos: Int): Int {
        val factorDificultad = when (dificultad) {
            "Muy Fácil" -> 1
            "Fácil" -> 2
            "Normal" -> 3
            "Difícil" -> 4
            "Muy Difícil" -> 5
            else -> 0
        }
        return (duracionHoras * 60 + duracionMinutos) * factorDificultad
    }

    private fun calcularExperiencia(dificultad: String): Int {
        return when (dificultad) {
            "Muy Fácil" -> 10
            "Fácil" -> 20
            "Normal" -> 30
            "Difícil" -> 40
            "Muy Difícil" -> 50
            else -> 0
        }
    }

    private fun guardarTarea() {
        val nombre = findViewById<EditText>(R.id.et_tarea_nombre).text.toString()
        val descripcion = findViewById<EditText>(R.id.et_tarea_descripcion).text.toString()
        val dificultad = findViewById<TextView>(R.id.tv_dificultad_seleccionada).text.toString()
        val duracionHoras = findViewById<SeekBar>(R.id.sb_duracion_horas).progress
        val duracionMinutos = findViewById<SeekBar>(R.id.sb_duracion_minutos).progress
        val monedas = findViewById<TextView>(R.id.tv_monedas_obtenidas).text.toString().toInt()

        if (nombre.isEmpty() || descripcion.isEmpty() || dificultad.isEmpty() || duracionHoras == 0 && duracionMinutos == 0) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (tareaActual == null) {
            val nuevaTarea = Tarea(
                id = tareaIdCounter++,
                usuario = usuarioActual,
                nombre = nombre,
                descripcion = descripcion,
                dificultad = dificultad,
                duracionHoras = duracionHoras,
                duracionMinutos = duracionMinutos,
                monedas = monedas
            )

            tareas.add(nuevaTarea)
            dbHelper.insertarTarea(nuevaTarea, usuarioActual)
            tareaAdapter.notifyItemInserted(tareas.size - 1)
        } else {
            val tareaEditada = tareaActual!!.copy(
                nombre = nombre,
                descripcion = descripcion,
                dificultad = dificultad,
                duracionHoras = duracionHoras,
                duracionMinutos = duracionMinutos,
                monedas = monedas
            )

            val index = tareas.indexOfFirst { it.id == tareaActual!!.id }
            tareas[index] = tareaEditada
            dbHelper.eliminarTarea(tareaActual!!.id)
            dbHelper.insertarTarea(tareaEditada, usuarioActual)
            tareaAdapter.notifyItemChanged(index)
        }

        findViewById<View>(R.id.formulario_tarea).visibility = View.GONE
    }

    private fun editarTarea(tarea: Tarea) {
        tareaActual = tarea
        findViewById<EditText>(R.id.et_tarea_nombre).setText(tarea.nombre)
        findViewById<EditText>(R.id.et_tarea_descripcion).setText(tarea.descripcion)
        findViewById<TextView>(R.id.tv_dificultad_seleccionada).text = tarea.dificultad
        findViewById<SeekBar>(R.id.sb_duracion_horas).progress = tarea.duracionHoras
        findViewById<SeekBar>(R.id.sb_duracion_minutos).progress = tarea.duracionMinutos
        findViewById<TextView>(R.id.tv_monedas_obtenidas).text = tarea.monedas.toString()
        findViewById<TextView>(R.id.tv_duracion_total).text = String.format("%02d:%02d", tarea.duracionHoras, tarea.duracionMinutos)
        findViewById<View>(R.id.formulario_tarea).visibility = View.VISIBLE
    }

    private fun mostrarDialogoConfirmacion(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
            .setPositiveButton("Sí") { dialog, _ ->
                val tarea = tareas[position]
                dbHelper.eliminarTarea(tarea.id)
                tareaAdapter.removeItem(position)
                noCompletarTarea(tarea) // Llamar al método para quitar experiencia
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                tareaAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun completarTarea(tarea: Tarea) {
        val monedasObtenidas = tarea.monedas
        monedasTotales += monedasObtenidas
        findViewById<TextView>(R.id.monedas_tv).text = monedasTotales.toString()

        val experienciaObtenida = calcularExperiencia(tarea.dificultad)
        experienciaTotal += experienciaObtenida
        val barraExperiencia: ProgressBar = findViewById(R.id.barraExperiencia)
        barraExperiencia.progress = experienciaTotal
        findViewById<TextView>(R.id.cantidadExperiencia).text = "$experienciaTotal/1000"

        if (experienciaTotal >= barraExperiencia.max) {
            experienciaTotal = 0
            nivelActual++
            val nivelTextView: TextView = findViewById(R.id.nivel_tv)
            val notacionNivel = niveles.getOrElse(nivelActual - 1) { "Dios" }
            nivelTextView.text = "Lv $nivelActual - $notacionNivel"
        }

        val nombreMascota = findViewById<TextView>(R.id.nombreMascota).text.toString()
        val vida = findViewById<ProgressBar>(R.id.barraVida).progress
        val hambre = findViewById<ProgressBar>(R.id.barraHambre).progress
        val mascota =
            Mascota(usuarioActual, nombreMascota, vida, hambre, experienciaTotal, nivelActual)
        dbHelper.actualizarMascota(mascota)

        dbHelper.actualizarMonedasUsuario(usuarioActual, monedasTotales)

        Toast.makeText(
            this,
            "¡Tarea completada! Has ganado $monedasObtenidas monedas y $experienciaObtenida puntos de experiencia.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()
        guardarDatosMascota()
    }

    fun noCompletarTarea(tarea: Tarea) {
        val experienciaPerdida = calcularExperiencia(tarea.dificultad)
        experienciaTotal -= experienciaPerdida
        if (experienciaTotal < 0) experienciaTotal = 0

        val barraExperiencia: ProgressBar = findViewById(R.id.barraExperiencia)
        barraExperiencia.progress = experienciaTotal
        findViewById<TextView>(R.id.cantidadExperiencia).text = "$experienciaTotal/1000"

        // Actualizar los datos de la mascota en la base de datos
        val nombreMascota = findViewById<TextView>(R.id.nombreMascota).text.toString()
        val vida = findViewById<ProgressBar>(R.id.barraVida).progress
        val hambre = findViewById<ProgressBar>(R.id.barraHambre).progress
        val mascota =
            Mascota(usuarioActual, nombreMascota, vida, hambre, experienciaTotal, nivelActual)
        dbHelper.actualizarMascota(mascota)

        Toast.makeText(
            this,
            "Has perdido $experienciaPerdida puntos de experiencia.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun mostrarFormularioSkins() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.formulario_skins, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        // Configurar los ImageButtons para seleccionar skins
        dialogView.findViewById<ImageButton>(R.id.skin1).setOnClickListener {
            seleccionarSkin(1)
            alertDialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.skin2).setOnClickListener {
            seleccionarSkin(2)
            alertDialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.skin3).setOnClickListener {
            seleccionarSkin(3)
            alertDialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.skin4).setOnClickListener {
            seleccionarSkin(4)
            alertDialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.skin5).setOnClickListener {
            seleccionarSkin(5)
            alertDialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.skin6).setOnClickListener {
            seleccionarSkin(6)
            alertDialog.dismiss()
        }
    }

    private fun seleccionarSkin(skinId: Int) {
        val botonSkinMascota: ImageButton = findViewById(R.id.botonSkinMascota)

        val spriteSheetResId = when (skinId) {
            1 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota1)
                R.drawable.img_mascota_caja
            }

            2 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota2)
                R.drawable.img_mascota_fantasma
            }

            3 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota3)
                R.drawable.img_mascota_manzana
            }

            4 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota4)
                R.drawable.img_mascota_seta
            }

            5 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota5)
                R.drawable.img_mascota5
            }

            6 -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota6)
                R.drawable.img_mascota6
            }

            else -> {
                botonSkinMascota.setImageResource(R.drawable.img_mascota1)
                R.drawable.img_mascota_caja
            }
        }

        val intent = Intent(this, MascotaActivity::class.java)
        intent.putExtra("usuario", usuarioActual)
        intent.putExtra("monedas", monedasTotales)
        intent.putExtra("spriteSheetResId", spriteSheetResId)
        startActivity(intent)
    }

    private fun mostrarDialogoEditarNombre() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Nombre de la Mascota")

        // Configurar el EditText para ingresar el nuevo nombre
        val input = EditText(this)
        input.hint = "Nuevo nombre"
        builder.setView(input)

        // Configurar los botones del diálogo
        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nuevoNombre = input.text.toString()
            if (nuevoNombre.isNotEmpty()) {
                val nombreMascotaTextView = findViewById<TextView>(R.id.nombreMascota)
                nombreMascotaTextView.text = nuevoNombre

                // Actualizar el nombre de la mascota en la base de datos
                val vida = findViewById<ProgressBar>(R.id.barraVida).progress
                val hambre = findViewById<ProgressBar>(R.id.barraHambre).progress
                val mascota = Mascota(usuarioActual, nuevoNombre, vida, hambre, experienciaTotal, nivelActual)
                dbHelper.actualizarMascota(mascota)
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

}

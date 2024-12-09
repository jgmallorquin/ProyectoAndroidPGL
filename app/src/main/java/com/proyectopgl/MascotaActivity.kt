package com.proyectopgl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyectopgl.utils.Sprite
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MascotaActivity : AppCompatActivity() {
    private lateinit var mascota: MascotaEntity
    private lateinit var gestureDetector: GestureDetector
    private lateinit var fondo: ImageView
    private lateinit var fondoSprite: Sprite
    private lateinit var particula: Particula
    private lateinit var audioManager: AudioManager

    private lateinit var usuarioActual: String
    private var monedasTotales: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascota)

        usuarioActual = intent.getStringExtra("usuario")!!
        monedasTotales = intent.getIntExtra("monedas", 0)

        audioManager = AudioManager(this)

        setupMenu()
        setupFondo()
        setupMascota()
        setupParticulas()
        setupZonaMascota()
        setupGestureDetector()
        setupCariciaDetector()
    }

    private fun setupMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.mascota
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tareas -> {
                    val intent = Intent(this, TareasActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(intent)
                    true
                }

                R.id.tienda -> {
                    val intent = Intent(this, TiendaActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(Intent(intent))
                    true
                }

                R.id.salir -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }

                else -> false
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val inventarioDialog = InventarioDialogFragment()
            inventarioDialog.show(supportFragmentManager, "InventarioDialog")
        }
    }

    private fun setupMascota() {
        val resources: Resources = this.resources
        val spriteSheetResId = intent.getIntExtra("spriteSheetResId", R.drawable.img_mascota_caja)

        val imagenMascota = findViewById<ImageView>(R.id.imagenMascota)
        val imagenExpresion = findViewById<ImageView>(R.id.imagenExpresion)
        val zonaMascota = findViewById<FrameLayout>(R.id.zonaMascota)
        val layoutMascota = findViewById<LinearLayout>(R.id.mascota)
        mascota = MascotaEntity(
            nombre = "Firulais",
            vida = 100,
            hambre = 100,
            experiencia = 100,
            nivel = 1,
            resources,
            spriteSheetResId,
            imagenMascota,
            zonaMascota,
            layoutMascota,
            imagenExpresion,
            escala = 2f
        )
        mascota.cambiarEstado(EstadoMascota.PASEAR)
    }

    private fun setupFondo() {
        fondo = findViewById(R.id.fondo)
        fondoSprite = Sprite(resources, R.drawable.img_fondos, 2, 1)
        cambiarFondo(0, 0)

        val fondoSiguiente: ImageButton = findViewById(R.id.btn_fondoSiguiente)
        val fondoAnterior: ImageButton = findViewById(R.id.btn_fondoAnterior)

        var fondoActual: Int = 0
        fondoSiguiente.setOnClickListener{
            if (fondoActual < fondoSprite.obtenerNumColumnas() - 1){
                fondoActual++
            }else{
                fondoActual = 0
            }
            cambiarFondo(fondoActual, 0)
        }

        fondoAnterior.setOnClickListener{
            if (fondoActual > 0){
                fondoActual--
            }else{
                fondoActual = fondoSprite.obtenerNumColumnas() - 1
            }
            cambiarFondo(fondoActual, 0)
        }
    }

    private fun cambiarFondo(columna: Int, fila: Int) {
        if (columna in 0 until fondoSprite.obtenerAncho() && fila in 0 until fondoSprite.obtenerAlto()) {
            val nuevoFondo = fondoSprite.obtenerSprite(fila, columna)
            fondo.setImageBitmap(nuevoFondo)
            fondo.scaleType = ImageView.ScaleType.FIT_XY
            fondo.drawable.isFilterBitmap = false
        }
    }

    private fun setupParticulas() {
        val zonaMascota = findViewById<FrameLayout>(R.id.zonaMascota)
        particula = Particula(zonaMascota, R.drawable.img_particulas, 5, 1, 3.0f)
        particula.inicializar()
    }

    private fun setupZonaMascota() {
        val zonaMascota = findViewById<FrameLayout>(R.id.zonaMascota)
        zonaMascota.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.setBackgroundColor(Color.LTGRAY)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    view.setBackgroundColor(Color.TRANSPARENT)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    val item = dragEvent.localState as? Articulo
                    item?.let {
                        val sprite = it.sprite.obtenerSprite(it.fila, it.columna)
                        if (sprite.width > 0 && sprite.height > 0) {
                            val imageView = ImageView(this)
                            imageView.setImageBitmap(sprite)
                            imageView.layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                leftMargin = dragEvent.x.toInt() - (sprite.width / 2)
                                topMargin = dragEvent.y.toInt() - (sprite.height / 2)
                            }
                            zonaMascota.addView(imageView)
                            Log.d("DragEvent", "ImageView added to zonaMascota")
                        } else {
                            Log.e("DragEvent", "Sprite size is zero")
                        }
                    }
                    view.setBackgroundColor(Color.TRANSPARENT)
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    view.setBackgroundColor(Color.TRANSPARENT)
                    Log.d("DragEvent", "Drag ended")
                    true
                }

                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        val zonaMascota = findViewById<FrameLayout>(R.id.zonaMascota)

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val x = e.x
                val y = e.y

                setupParticulas()
                particula.lanzarAnimacion(x, y, 1000)
                audioManager.play(R.raw.sonido_doble_tap)

                val (anchoMascota, altoMascota) = mascota.obtenerTamanioMascota()
                val escala = mascota.obtenerEscala()
                val centroX = x - (anchoMascota * escala / 2)
                val centroY = y - (altoMascota * escala / 2)
                mascota.seguir(centroX, centroY)
                return true
            }
        })

        zonaMascota.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupCariciaDetector() {
        val layoutMascota = findViewById<LinearLayout>(R.id.mascota)
        layoutMascota.setOnClickListener { mascota.cambiarEstado(EstadoMascota.FELIZ) }
    }
}

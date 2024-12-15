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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.DragEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.speech.RecognizerIntent
import android.view.ScaleGestureDetector
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

@Suppress("DEPRECATION")
class MascotaActivity : AppCompatActivity(), DetectorSacudida.EnSacudirOyente {
    private lateinit var mascota: MascotaEntity
    private lateinit var gestureDetector: GestureDetector
    private lateinit var fondo: ImageView
    private lateinit var fondoSprite: Sprite
    private lateinit var particula: Particula
    private lateinit var audioManager: AudioManager

    private lateinit var usuarioActual: String
    private var monedasTotales: Int = 0

    private lateinit var resultTextView:TextView
    private val SPEECH_REQUEST_CODE=1

    private lateinit var detectorSacudida: DetectorSacudida
    private lateinit var imagenMascota: ImageView
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascota)

        usuarioActual = intent.getStringExtra("usuario")!!
        monedasTotales = intent.getIntExtra("monedas", 0)
        resultTextView=findViewById(R.id.resultTextView)
        audioManager = AudioManager(this)

        resultTextView.visibility = TextView.INVISIBLE

        setupMenu()
        setupFondo()
        setupMascota()
        setupParticulas()
        setupZonaMascota()
        setupGestureDetector()
        setupCariciaDetector()

        detectorSacudida = DetectorSacudida(this, this)

        imagenMascota = findViewById(R.id.imagenMascota)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        imagenMascota.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

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

        // Obtenemos la referencia al botón central
        val botonCentral = findViewById<FloatingActionButton>(R.id.fab)

        // Se añade un listener para el botón central
        botonCentral.setOnLongClickListener{
            iniciarReconocimientoVoz()
            true
        }

        botonCentral.setOnClickListener {
            val inventarioDialog = InventarioDialogFragment()
            inventarioDialog.show(supportFragmentManager, "InventarioDialog")
        }

    }

    private fun iniciarReconocimientoVoz(){
        // Se crea un intent para el reconocimiento de voz
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
            // se establece el modelo de lenguaje
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // se establece el idioma
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
            // se establece el mensaje que se mostrará al usuario
            putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora para transcribir tu voz")
        }
        try{
            // se inicia la actividad de reconocimiento de voz
            startActivityForResult(intent,SPEECH_REQUEST_CODE)
        }catch(e:Exception){
            // si no se puede iniciar el reconocimiento de voz se muestra un mensaje
            Toast.makeText(this,"El reconocimiento de voz no está disponible",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Se ejecuta cuando se recibe el resultado del reconocimiento de voz
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Se comprueba que el resultado es correcto
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {
                // Se muestra el texto reconocido encima del botón
                resultTextView.text = it[0]

                // Si el texto reconocido contiene la palabra "comer"
                if (resultTextView.text.toString().contains("comer")) {
                    // Se muestra el texto "¡COMIENDO!" durante 3 segundos
                    resultTextView.visibility = TextView.VISIBLE

                    // Se muestra la comida en la sala
                    var comedero: ImageView = findViewById(R.id.comedero)
                    comedero.visibility = ImageView.VISIBLE

                    // Se cambia de sala
                    cambiarFondo(1, 0)

                    // Se resta 100 monedas al usuario
                    monedasTotales -= 100

                    // La mascota cambiará de estado a COMER
                    mascota.cambiarEstado(EstadoMascota.COMER)

                    // La mascota comerá durante 6 segundos
                    Handler(Looper.getMainLooper()).postDelayed({
                        // La comida se hace invisible
                        comedero.visibility = ImageView.INVISIBLE
                        // Se cambia de sala
                        cambiarFondo(0, 0)
                    }, 6000)
                }

                // Ocultar después de 3 segundos el texto reconocido
                Handler(Looper.getMainLooper()).postDelayed({
                    resultTextView.visibility = TextView.INVISIBLE
                }, 3000)
            }
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

        // Se obtiene la zona de la mascota
        val zonaMascota = findViewById<FrameLayout>(R.id.zonaMascota)

        // Se crea un detector de gestos
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            // Se detecta un doble click
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Se obtienen las coordenadas donde se ha hecho doble click
                val x = e.x
                val y = e.y

                // Se lanza la animación de partículas
                setupParticulas()
                particula.lanzarAnimacion(x, y, 1000)

                // Se reproduce un sonido
                audioManager.play(R.raw.sonido_doble_tap)

                // Se obtiene el tamaño de la mascota y se calcula el centro de la mascota
                val (anchoMascota, altoMascota) = mascota.obtenerTamanioMascota()
                val escala = mascota.obtenerEscala()
                val centroX = x - (anchoMascota * escala / 2)
                val centroY = y - (altoMascota * escala / 2)

                // Se mueve la mascota al centro de donde se ha hecho doble click
                mascota.seguir(centroX, centroY)

                // Se devuelve true para indicar que se ha detectado el doble click
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

    override fun onResume() {
        super.onResume()
        detectorSacudida.iniciar()
    }

    override fun onPause() {
        super.onPause()
        detectorSacudida.detener()
    }

    // Se ejecuta cuando detecta que el móvil ha sido sacudido
    override fun enSacudir() {
        // La mascota pasa al estado de MAREADO y ejecuta la lógica correspondiente
        mascota.cambiarEstado(EstadoMascota.MAREADO)

        // Hace visible el texto "¡MAREADO!" durante 5 segundos
        resultTextView.visibility = TextView.VISIBLE
        resultTextView.text = "¡MAREADO!"

        // Se oculta después de 5 segundos el texto "¡MAREADO!"
        Handler(Looper.getMainLooper()).postDelayed({
            resultTextView.visibility = TextView.INVISIBLE
        }, 5000)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
            imagenMascota.scaleX = scaleFactor
            imagenMascota.scaleY = scaleFactor
            return true
        }
    }
}



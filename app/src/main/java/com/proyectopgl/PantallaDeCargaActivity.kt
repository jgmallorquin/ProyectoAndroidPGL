package com.proyectopgl

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.proyectopgl.utils.Sprite

class PantallaDeCargaActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var spriteImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sprite: Sprite
    private var frameIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_de_carga)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBar)
        spriteImageView = findViewById(R.id.sprite)

        // Crear un objeto Sprite
        sprite = Sprite(resources, R.drawable.img_carga, 3, 1, 4f)

        iniciarCarga()
    }

    private fun iniciarCarga() {
        val usuario = intent.getStringExtra("usuario") ?: ""
        val monedas = intent.getIntExtra("monedas", 0)

        Thread {
            for (i in 0..100) {
                Thread.sleep(30)
                handler.post {
                    progressBar.progress = i
                    if (i % 5 == 0) {
                        actualizarSprite()
                    }
                    if (i == 100) {
                        val intent = Intent(this, TareasActivity::class.java)
                        intent.putExtra("usuario", usuario)
                        intent.putExtra("monedas", monedas)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }.start()
    }

    private fun actualizarSprite() {
        val bitmap = sprite.obtenerSprite(0, frameIndex)
        spriteImageView.setImageBitmap(bitmap)
        frameIndex = (frameIndex + 1) % sprite.obtenerNumColumnas()
    }
}

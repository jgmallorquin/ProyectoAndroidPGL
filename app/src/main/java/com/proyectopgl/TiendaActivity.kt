package com.proyectopgl

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyectopgl.utils.Sprite

class TiendaActivity : AppCompatActivity() {

    private lateinit var usuarioActual: String
    private var monedasTotales: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupMenu()
        setupRecyclerView()

        // Obtener la cantidad de monedas del usuario y actualizar el TextView
        val dbHelper = DBHelper(this)
        usuarioActual = intent.getStringExtra("usuario")!!
        monedasTotales = intent.getIntExtra("monedas", 0)
        val monedasTextView = findViewById<TextView>(R.id.monedas_cantidad)
        monedasTextView.text = monedasTotales.toString()
    }


    private fun setupMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tienda
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tareas -> {
                    val intent = Intent(this, TareasActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(intent)
                    true
                }
                R.id.mascota -> {
                    val intent = Intent(this, MascotaActivity::class.java)
                    intent.putExtra("usuario", usuarioActual)
                    intent.putExtra("monedas", monedasTotales)
                    startActivity(intent)
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
    }


    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.listaArticulos)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columnas en la cuadrícula

        val sprite = Sprite(resources, R.drawable.img_articulos, numColumnas = 5, numFilas = 5, escala = 2f)

        val articulos = listOf(
            Articulo("Burguesa", "200", 0, 0, sprite),
            Articulo("Cocacola", "350", 0, 1, sprite),
            Articulo("Bocadillo", "200", 0, 2, sprite),
            Articulo("Taco", "600", 0, 3, sprite),
            Articulo("Helado", "300", 0, 4, sprite),

            Articulo("???", "1500", 1, 0, sprite),
            Articulo("Experiencia", "10000", 1, 1, sprite),
            Articulo("Hambre", "4000", 1, 2, sprite),
            Articulo("Felicidad", "4500", 1, 3, sprite),
            Articulo("Vida", "6000", 1, 4, sprite),

            Articulo("Huesito", "500", 2, 0, sprite),
            Articulo("Pelota", "800", 2, 1, sprite),
            Articulo("Pelota", "800", 2, 2, sprite),
            Articulo("Consola", "6000", 2, 3, sprite),
            Articulo("Peluche", "900", 2, 4, sprite),

            Articulo("Peluche", "500", 3, 0, sprite),
            Articulo("Peluche", "900", 3, 1, sprite),
            Articulo("Pez", "1200", 3, 2, sprite),
            Articulo("Peluche", "900", 3, 3, sprite),
            Articulo("Cactus", "99999", 3, 4, sprite)
        )
        recyclerView.adapter = TiendaAdapter(articulos, sprite, true)
    }
}

package com.proyectopgl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var botonLogin: Button
    private lateinit var botonRegistrarse: Button
    private lateinit var usuarioEditText: EditText
    private lateinit var claveEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usuarioEditText = findViewById(R.id.usuario)
        claveEditText = findViewById(R.id.clave)

        botonLogin = findViewById(R.id.botonLogin)
        botonRegistrarse = findViewById(R.id.botonRegistrarse)

        botonLogin.setOnClickListener {
            val usuario = usuarioEditText.text.toString()
            val clave = claveEditText.text.toString()

            val dbHelper = DBHelper(this)
            if (dbHelper.existeUsuario(usuario, clave)) {
                Toast.makeText(this, "Datos correctos", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PantallaDeCargaActivity::class.java)
                intent.putExtra("usuario", usuario)
                intent.putExtra("monedas", dbHelper.obtenerMonedasUsuario(usuario))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        botonRegistrarse.setOnClickListener {
            val username = usuarioEditText.text.toString()
            val password = claveEditText.text.toString()

            val dbHelper = DBHelper(this)
            if (dbHelper.existeUsuario(username, password)) {
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
            } else {
                val result = dbHelper.insertarUsuario(username, password)
                if (result != -1L) {
                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

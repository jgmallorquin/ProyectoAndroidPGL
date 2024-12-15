package com.proyectopgl

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var botonLogin: Button
    private lateinit var botonRegistrarse: Button
    private lateinit var usuarioEditText: EditText
    private lateinit var claveEditText: EditText

    private lateinit var resultTextView:TextView
    private val SPEECH_REQUEST_CODE=1

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

        val botonIdioma: FloatingActionButton = findViewById(R.id.botonIdioma)
        botonIdioma.setOnClickListener{
            iniciarReconocimientoVoz()
            true
        }
    }

    private fun iniciarReconocimientoVoz(){
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT,"Indique el idioma que desea utilizar")
        }
        try{
            startActivityForResult(intent,SPEECH_REQUEST_CODE)
        }catch(e:Exception){
            Toast.makeText(this,"El reconocimiento de voz no está disponible",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Se ejecuta cuando se recibe un resultado del reconocimiento de voz
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Se comprueba que el resultado es correcto
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.let {

                // Se obtienen las referencias
                var logo: ImageView = findViewById(R.id.logo)
                var usuario: TextView = findViewById(R.id.usuario)
                var clave: TextView = findViewById(R.id.clave)
                var botonLogin: Button = findViewById(R.id.botonLogin)
                var botonRegistro: Button = findViewById(R.id.botonRegistrarse)
                var idiomaSeleccioandoImg: ImageView = findViewById(R.id.idiomaSeleccionadoImg)
                var textoCambiarIdioma: TextView = findViewById(R.id.textoCambiarIdioma)

                // Se obtiene el texto reconocido
                val textoReconocido =  it[0].toString().lowercase(Locale.getDefault())

                // Se cambian los textos e imágenes según el idioma reconocido
                when {
                    textoReconocido.contains("español") -> {
                        logo.setImageResource(R.drawable.img_logo)
                        usuario.hint = "Usuario"
                        clave.hint = "Clave"
                        botonLogin.text = "Iniciar sesión"
                        botonRegistro.text = "Registrarse"
                        idiomaSeleccioandoImg.setImageResource(R.drawable.img_es)
                        textoCambiarIdioma.text = "Cambiar idioma"
                    }
                    textoReconocido.contains("inglés") -> {
                        logo.setImageResource(R.drawable.img_logo_en)
                        usuario.hint = "User"
                        clave.hint = "Password"
                        botonLogin.text = "Log in"
                        botonRegistro.text = "Sign up"
                        idiomaSeleccioandoImg.setImageResource(R.drawable.img_en)
                        textoCambiarIdioma.text = "Change language"
                    }
                    textoReconocido.contains("francés") -> {
                        logo.setImageResource(R.drawable.img_logo_fr)
                        usuario.hint = "Utilisateur"
                        clave.hint = "Clé"
                        botonLogin.text = "S'identifier"
                        botonRegistro.text = "S'inscrire"
                        idiomaSeleccioandoImg.setImageResource(R.drawable.img_fr)
                        textoCambiarIdioma.text = "Changer de langue"
                    }
                    textoReconocido.contains("alemán") -> {
                        logo.setImageResource(R.drawable.img_logo_de)
                        usuario.hint = "Benutzer"
                        clave.hint = "Schlüssel"
                        botonLogin.text = "Anmelden"
                        botonRegistro.text = "Registrieren"
                        idiomaSeleccioandoImg.setImageResource(R.drawable.img_de)
                        textoCambiarIdioma.text = "Sprache ändern"
                    }
                    else -> {
                        logo.setImageResource(R.drawable.img_logo)
                        usuario.hint = "Usuario"
                        clave.text = "Clave"
                        botonLogin.text = "Iniciar sesión"
                        botonRegistro.text = "Registrarse"
                        idiomaSeleccioandoImg.setImageResource(R.drawable.img_es)
                        textoCambiarIdioma.text = "Cambiar idioma"
                    }
                }
            }
        }
    }
}

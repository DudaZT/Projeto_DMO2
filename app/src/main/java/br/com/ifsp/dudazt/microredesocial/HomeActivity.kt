package br.com.ifsp.dudazt.microredesocial

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarDadosUsuario()

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun carregarDadosUsuario() {
        val email = firebaseAuth.currentUser?.email
        if (email == null) {
            // Usuário não está logado, volta para login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        db.collection("usuarios").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val imageString = document.data?.get("fotoPerfil")?.toString()
                        val username = document.data?.get("username")?.toString() ?: "Username"
                        val nomecompleto = document.data?.get("nomecompleto")?.toString() ?: "Nome Completo"

                        binding.txtUsername.text = username
                        binding.txtNomeCompleto.text = nomecompleto

                        if (!imageString.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.imgProfile.setImageBitmap(bitmap)
                        }
                    }
                }
            }
    }
}
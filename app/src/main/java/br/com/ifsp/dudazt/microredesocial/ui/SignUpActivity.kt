package br.com.ifsp.dudazt.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnCriarConta.setOnClickListener { cadastrarUsuario() }
        binding.btnVoltar.setOnClickListener { finish() }
    }

    private fun cadastrarUsuario() {
        val nomeCompleto = binding.edtNomeCompleto.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtSenha.text.toString()
        val confirmar = binding.edtConfirmarSenha.text.toString()

        if (nomeCompleto.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmar) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_LONG).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnCriarConta.isEnabled = false

        // cria o usuário no firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cria documento do usuário no Firestore
                    val userData = hashMapOf(
                        "nomecompleto" to nomeCompleto,
                        "username" to email.substringBefore("@"),
                        "email" to email,
                        "fotoPerfil" to ""
                    )

                    // salva os dados do perfil no Firestore
                    Firebase.firestore.collection("usuarios")
                        .document(email)
                        .set(userData)
                        .addOnSuccessListener {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Conta criada, mas erro ao salvar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                } else {
                    binding.btnCriarConta.isEnabled = true
                    Toast.makeText(this, task.exception?.message ?: "Erro ao criar conta", Toast.LENGTH_LONG).show()
                }
            }
    }
}
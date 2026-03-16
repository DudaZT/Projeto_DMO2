package br.com.ifsp.dudazt.microredesocial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnCriarConta.setOnClickListener {
            cadastrarUsuario()
        }
    }

    private fun cadastrarUsuario() {

        val email = binding.edtEmail.text.toString()
        val password = binding.edtSenha.text.toString()
        val confirmar = binding.edtConfirmarSenha.text.toString()

        // validações
        if (email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmar) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_LONG).show()
            return
        }

        // criação do usuário
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()

                } else {

                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

                }

            }
    }
}
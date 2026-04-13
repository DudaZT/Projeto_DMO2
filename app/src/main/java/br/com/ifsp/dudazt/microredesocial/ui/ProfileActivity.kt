package br.com.ifsp.dudazt.microredesocial.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.dudazt.microredesocial.ui.HomeActivity
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        val db = Firebase.firestore

        // =========================
        // CARREGAR PERFIL (EDITAR)
        // =========================
        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {
                    binding.edtUsername.setText(doc.getString("username"))
                    binding.edtFullName.setText(doc.getString("nomecompleto"))
                }
            }

        // =========================
        // SALVAR / ATUALIZAR PERFIL
        // =========================
        binding.btnSave.setOnClickListener {

            val username = binding.edtUsername.text.toString()
            val nome = binding.edtFullName.text.toString()

            val foto = Base64Converter.drawableToString(binding.imgProfile.drawable)

            val dados = hashMapOf(
                "username" to username,
                "nomecompleto" to nome,
                "fotoPerfil" to foto
            )

            db.collection("usuarios")
                .document(email)
                .set(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil salvo!", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}
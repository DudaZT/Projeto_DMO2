package br.com.ifsp.dudazt.microredesocial.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.dudazt.microredesocial.ui.home.HomeActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->

        if (uri != null) {
            binding.imgProfile.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // botão alterar foto
        binding.btnChangePhoto.setOnClickListener {

            galeria.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )

        }

        // botão salvar
        binding.btnSave.setOnClickListener {

            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth.currentUser != null){

                val email = firebaseAuth.currentUser!!.email.toString()
                val username = binding.edtUsername.text.toString()
                val nomecompleto = binding.edtFullName.text.toString()

                val fotoPerfilString = Base64Converter.drawableToString(binding.imgProfile.drawable)

                val db = Firebase.firestore

                val dados = hashMapOf(
                    "nomecompleto" to nomecompleto,
                    "username" to username,
                    "fotoPerfil" to fotoPerfilString
                )

                db.collection("usuarios")
                    .document(email)
                    .set(dados)
                    .addOnSuccessListener {

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()

                    }
            }

        }
    }
}
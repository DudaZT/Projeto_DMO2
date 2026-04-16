package br.com.ifsp.dudazt.microredesocial.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imagemUri: Uri? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->

        uri?.let {
            imagemUri = it
            binding.imgProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = Firebase.firestore

        binding.imgProfile.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // carregar dados
        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {
                    binding.edtUsername.setText(doc.getString("username"))
                    binding.edtFullName.setText(doc.getString("nomecompleto"))

                    val foto = doc.getString("fotoPerfil")
                    if (foto != null) {
                        val bitmap = Base64Converter.stringToBitmap(foto)
                        binding.imgProfile.setImageBitmap(bitmap)
                    }
                }
            }

        binding.btnSave.setOnClickListener {

            val username = binding.edtUsername.text.toString()
            val nome = binding.edtFullName.text.toString()

            var fotoBase64 = ""

            try {
                if (imagemUri != null) {
                    val inputStream = contentResolver.openInputStream(imagemUri!!)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    fotoBase64 = Base64Converter.bitmapToString(bitmap)
                } else {
                    // caso não tenha escolhido nova imagem
                    fotoBase64 = Base64Converter.drawableToString(binding.imgProfile.drawable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dados = hashMapOf(
                "username" to username,
                "nomecompleto" to nome,
                "fotoPerfil" to fotoBase64
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
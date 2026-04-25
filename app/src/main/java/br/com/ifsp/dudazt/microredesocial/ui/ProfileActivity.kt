package br.com.ifsp.dudazt.microredesocial.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

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

        val email = FirebaseAuth.getInstance().currentUser?.email ?: run {
            finish()
            return
        }
        val db = Firebase.firestore

        binding.btnChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Carregar dados do Firestore
        db.collection("usuarios").document(email).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.edtUsername.setText(doc.getString("username"))
                    binding.edtFullName.setText(doc.getString("nomecompleto"))

                    val foto = doc.getString("fotoPerfil")
                    if (!foto.isNullOrBlank()) {
                        try {
                            binding.imgProfile.setImageBitmap(Base64Converter.stringToBitmap(foto))
                        } catch (e: Exception) { /* mantém placeholder */ }
                    }
                }
            }

        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            salvarPerfil(email, db)
        }
    }

    private fun salvarPerfil(email: String, db: com.google.firebase.firestore.FirebaseFirestore) {
        val username = binding.edtUsername.text.toString().trim()
        val nome = binding.edtFullName.text.toString().trim()
        val novaSenha = binding.edtNovaSenha.text.toString()

        if (username.isEmpty() || nome.isEmpty()) {
            Toast.makeText(this, "Nome de usuário e nome completo são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        // Atualizar senha se preenchida
        if (novaSenha.isNotBlank()) {
            if (novaSenha.length < 6) {
                Toast.makeText(this, "A nova senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }

            // atualiza a senha direto no firebase auth
            FirebaseAuth.getInstance().currentUser?.updatePassword(novaSenha)
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        var fotoBase64 = ""
        try {
            fotoBase64 = if (imagemUri != null) {
                val inputStream = contentResolver.openInputStream(imagemUri!!)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                Base64Converter.bitmapToString(bitmap)
            } else {
                Base64Converter.drawableToString(binding.imgProfile.drawable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dados = hashMapOf(
            "username" to username,
            "nomecompleto" to nome,
            "fotoPerfil" to fotoBase64,
            "email" to email
        )

        binding.btnSave.isEnabled = false

        db.collection("usuarios").document(email)
            .set(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil salvo!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
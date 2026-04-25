package br.com.ifsp.dudazt.microredesocial.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityAddPostBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import br.com.ifsp.dudazt.microredesocial.util.LocalizacaoHelper
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AddPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {

    private lateinit var binding: ActivityAddPostBinding
    private val LOCATION_CODE = 1001

    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.imgPost.setImageURI(uri)
            binding.txtEscolhaFoto.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgPost.setOnClickListener { abrirGaleria() }
        binding.btnChangePhoto.setOnClickListener { abrirGaleria() }
        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            solicitarLocalizacao()
        }
    }

    // abre galeria pra escolher imagem
    private fun abrirGaleria() {
        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // solicita permissão de localização
    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_CODE
            )
        } else {
            binding.btnSave.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            LocalizacaoHelper(this).obterLocalizacaoAtual(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.btnSave.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    LocalizacaoHelper(this).obterLocalizacaoAtual(this)
                }
            } else {
                // Permissão negada: salva sem cidade
                salvarPost("desconhecida")
            }
        }
    }

    // callback quando localização é obtida
    override fun onLocalizacaoRecebida(endereco: Address, latitude: Double, longitude: Double) {
        val cidade = listOfNotNull(endereco.locality, endereco.subAdminArea, endereco.adminArea)
            .firstOrNull() ?: "desconhecida"
        salvarPost(cidade.trim().lowercase())
    }

    override fun onErro(mensagem: String) {
        salvarPost("desconhecida")
    }

    // salva o post no Firestore
    private fun salvarPost(cidade: String) {
        val descricao = binding.edtDescricao.text.toString().trim()

        if (descricao.isBlank()) {
            binding.btnSave.isEnabled = true
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val drawable = binding.imgPost.drawable
        if (drawable == null) {
            binding.btnSave.isEnabled = true
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val imagem = try {
            Base64Converter.drawableToString(drawable)
        } catch (e: Exception) {
            binding.btnSave.isEnabled = true
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            return
        }

        // Busca o username antes de salvar o post
        Firebase.firestore.collection("usuarios")
            .document(user.email ?: "")
            .get()
            .addOnSuccessListener { doc ->
                val authorName = doc.getString("username") ?: user.email ?: ""

                val dados = hashMapOf(
                    "descricao" to descricao,
                    "imageString" to imagem,
                    "city" to cidade,
                    "authorId" to user.uid,
                    "authorName" to authorName,
                    "data" to Timestamp.now()
                )

                FirebaseFirestore.getInstance().collection("posts")
                    .add(dados)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post criado!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnSave.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                // Fallback sem username
                val dados = hashMapOf(
                    "descricao" to descricao,
                    "imageString" to imagem,
                    "city" to cidade,
                    "authorId" to user.uid,
                    "authorName" to (user.email ?: ""),
                    "data" to Timestamp.now()
                )
                FirebaseFirestore.getInstance().collection("posts").add(dados)
                    .addOnSuccessListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
    }
}
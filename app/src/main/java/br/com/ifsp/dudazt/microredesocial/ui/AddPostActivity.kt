package br.com.ifsp.dudazt.microredesocial.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityAddPostBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import br.com.ifsp.dudazt.microredesocial.util.LocalizacaoHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AddPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {

    private lateinit var binding: ActivityAddPostBinding
    private val LOCATION_CODE = 1001

    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) binding.imgPost.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChangePhoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnSave.setOnClickListener {
            solicitarLocalizacao()
        }
    }

    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_CODE
            )

        } else {
            val helper = LocalizacaoHelper(this)
            helper.obterLocalizacaoAtual(this)
        }
    }

    override fun onLocalizacaoRecebida(
        endereco: Address,
        latitude: Double,
        longitude: Double
    ) {
        val cidadeFormatada = listOfNotNull(
            endereco.locality,
            endereco.subAdminArea,
            endereco.adminArea
        ).firstOrNull() ?: "desconhecida"

        salvarPost(cidadeFormatada.trim().lowercase())
    }

    override fun onErro(mensagem: String) {
        Toast.makeText(this, "Erro localização: $mensagem", Toast.LENGTH_SHORT).show()

        salvarPost("desconhecida")
    }

    private fun salvarPost(cidade: String) {

        val descricao = binding.edtDescricao.text.toString()

        if (descricao.isBlank()) {
            Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val drawable = binding.imgPost.drawable
        if (drawable == null) {
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val imagem = Base64Converter.drawableToString(drawable)

        val dados = hashMapOf(
            "descricao" to descricao,
            "imageString" to imagem,
            "city" to cidade,
            "authorId" to userId,
            "data" to Timestamp.now()
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .add(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Post criado!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
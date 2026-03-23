package br.com.ifsp.dudazt.microredesocial.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import br.com.ifsp.dudazt.microredesocial.adapter.PostAdapter
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.ui.main.MainActivity
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import br.com.ifsp.dudazt.microredesocial.ui.post.PostActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.ifsp.dudazt.microredesocial.R
import br.com.ifsp.dudazt.microredesocial.ui.addpost.AddPostActivity


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private lateinit var adapter: PostAdapter
    private lateinit var posts: ArrayList<Post>

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

        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        binding.btnCarregarFeed.setOnClickListener {

            db.collection("posts").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        posts = ArrayList()

                        for (document in task.result.documents) {

                            val imageString = document.data?.get("imageString")?.toString()

                            val bitmap = if (!imageString.isNullOrEmpty()) {
                                Base64Converter.stringToBitmap(imageString)
                            } else {
                                // Se não tiver imagem, usar drawable de placeholder
                                BitmapFactory.decodeResource(resources, R.drawable.empty_profile)
                            }

                            val descricao = document.data?.get("descricao")?.toString() ?: ""

                            posts.add(Post(descricao, bitmap))
                        }

                        adapter = PostAdapter(posts.toTypedArray())

                        binding.recyclerView.layoutManager = LinearLayoutManager(this)
                        binding.recyclerView.adapter = adapter
                    }
                }
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
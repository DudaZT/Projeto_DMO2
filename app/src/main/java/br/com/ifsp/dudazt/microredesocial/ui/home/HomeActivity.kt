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
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.ifsp.dudazt.microredesocial.R
import br.com.ifsp.dudazt.microredesocial.ui.addpost.AddPostActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private lateinit var adapter: PostAdapter
    private lateinit var posts: ArrayList<Post>

    private var ultimoTimestamp: Timestamp? = null
    private val limite = 5

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

        // BOTÃO COM PAGINAÇÃO
        binding.btnCarregarFeed.setOnClickListener {
            carregarPosts()
        }
    }

    private fun carregarDadosUsuario() {
        val email = firebaseAuth.currentUser?.email
        if (email == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        db.collection("usuarios").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {

                        val imageString = document.getString("fotoPerfil")
                        val username = document.getString("username") ?: "Username"
                        val nomecompleto = document.getString("nomecompleto") ?: "Nome Completo"

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

    fun carregarPosts() {

        var query = db.collection("posts")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(limite.toLong())

        if (ultimoTimestamp != null) {
            query = query.startAfter(ultimoTimestamp!!)
        }

        query.get()
            .addOnSuccessListener { documentos ->

                if (!documentos.isEmpty) {

                    ultimoTimestamp = documentos.documents.last().getTimestamp("data")

                    val novosPosts = ArrayList<Post>()

                    for (document in documentos) {

                        val imageString = document.getString("imageString")

                        val bitmap = if (!imageString.isNullOrEmpty()) {
                            Base64Converter.stringToBitmap(imageString)
                        } else {
                            BitmapFactory.decodeResource(resources, R.drawable.empty_profile)
                        }

                        val descricao = document.getString("descricao") ?: ""

                        val data = document.getTimestamp("data") ?: Timestamp.now()

                        novosPosts.add(Post(descricao, bitmap, data))
                    }

                    adicionarPostsNoAdapter(novosPosts)
                }
            }
    }

    fun adicionarPostsNoAdapter(novosPosts: List<Post>) {

        if (!::adapter.isInitialized) {
            posts = ArrayList()
            adapter = PostAdapter(posts)

            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
        }

        adapter.adicionarPosts(novosPosts)
    }
}
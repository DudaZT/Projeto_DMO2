package br.com.ifsp.dudazt.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.dudazt.microredesocial.R
import br.com.ifsp.dudazt.microredesocial.adapter.PostAdapter
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityHomeBinding
import br.com.ifsp.dudazt.microredesocial.ui.ProfileActivity
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val db = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()

    private var ultimoDocumento: DocumentSnapshot? = null
    private val limite = 5

    private var isLoading = false
    private var acabouLista = false

    private var modoBusca = false
    private var cidadeBusca = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter(posts)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        carregarPerfilUsuario()
        carregarPosts()

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (modoBusca) buscarMaisPorCidade()
                    else carregarPosts()
                }
            }
        })

        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.edtSearchCity.setOnEditorActionListener { _, _, _ ->
            val text = binding.edtSearchCity.text.toString()

            if (text.isBlank()) resetarFeed()
            else buscarPorCidade(text)

            true
        }
    }

    override fun onResume() {
        super.onResume()
        carregarPerfilUsuario()
    }

    private fun carregarPerfilUsuario() {

        val email = firebaseAuth.currentUser?.email ?: return

        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val username = doc.getString("username") ?: ""
                    val nome = doc.getString("nomecompleto") ?: ""
                    val foto = doc.getString("fotoPerfil")

                    binding.txtUsername.text = "@$username"
                    binding.txtNome.text = nome

                    try {
                        if (foto != null) {
                            val bitmap = Base64Converter.stringToBitmap(foto)
                            binding.imgUser.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        binding.imgUser.setImageResource(R.drawable.empty_profile)
                    }
                }
            }
    }

    private fun carregarPosts() {

        if (isLoading || acabouLista || modoBusca) return

        isLoading = true

        var query = db.collection("posts")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(limite.toLong())

        if (ultimoDocumento != null) {
            query = query.startAfter(ultimoDocumento!!)
        }

        query.get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    acabouLista = true
                    isLoading = false
                    return@addOnSuccessListener
                }

                val novos = result.toObjects(Post::class.java)

                ultimoDocumento = result.documents.last()

                posts.addAll(novos)
                adapter.notifyDataSetChanged()

                isLoading = false
            }
    }

    private fun buscarPorCidade(cidade: String) {

        modoBusca = true
        cidadeBusca = cidade.trim().lowercase()

        posts.clear()
        adapter.notifyDataSetChanged()

        ultimoDocumento = null
        acabouLista = false

        db.collection("posts")
            .whereEqualTo("city", cidadeBusca)
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(limite.toLong())
            .get()
            .addOnSuccessListener { result ->

                val novos = result.toObjects(Post::class.java)

                if (result.documents.isNotEmpty()) {
                    ultimoDocumento = result.documents.last()
                }

                posts.addAll(novos)
                adapter.notifyDataSetChanged()

                isLoading = false
            }
    }

    private fun buscarMaisPorCidade() {

        if (isLoading || acabouLista) return

        isLoading = true

        var query = db.collection("posts")
            .whereEqualTo("city", cidadeBusca)
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(limite.toLong())

        if (ultimoDocumento != null) {
            query = query.startAfter(ultimoDocumento!!)
        }

        query.get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    acabouLista = true
                    isLoading = false
                    return@addOnSuccessListener
                }

                val novos = result.toObjects(Post::class.java)

                ultimoDocumento = result.documents.last()

                posts.addAll(novos)
                adapter.notifyDataSetChanged()

                isLoading = false
            }
    }

    private fun resetarFeed() {
        modoBusca = false
        cidadeBusca = ""
        ultimoDocumento = null
        acabouLista = false

        posts.clear()
        adapter.notifyDataSetChanged()

        carregarPosts()
    }
}
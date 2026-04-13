package br.com.ifsp.dudazt.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.dudazt.microredesocial.adapter.PostAdapter
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

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

        carregarPosts()

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

            if (text.isBlank()) {
                resetarFeed()
            } else {
                buscarPorCidade(text)
            }

            true
        }
    }

    // =========================
    // FEED NORMAL
    // =========================

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

                if (novos.isNotEmpty()) {
                    ultimoDocumento = result.documents.last()
                    posts.addAll(novos)
                    adapter.notifyDataSetChanged()
                }

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    // =========================
    // BUSCA
    // =========================

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

                posts.clear()
                posts.addAll(result.toObjects(Post::class.java))
                adapter.notifyDataSetChanged()

                isLoading = false
            }
            .addOnFailureListener {
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
            .addOnFailureListener {
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
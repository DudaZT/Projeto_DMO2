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
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter
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
    private lateinit var layoutManager: LinearLayoutManager
    private val posts = mutableListOf<Post>()

    // guardo o último documento pra continuar a busca depois
    private var ultimoDocumento: DocumentSnapshot? = null
    // carrega 5 posts por vez
    private val LIMITE = 5

    private var isLoading = false
    private var acabouLista = false

    private var modoBusca = false
    private var cidadeBusca = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter(posts)
        layoutManager = LinearLayoutManager(this)

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        carregarPerfilUsuario()
        carregarPosts()

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // scroll infinito, se chega no último item ele carrega mais posts automaticamente
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Só pagina rolando para baixo, com itens carregados, sem carregamento ativo
                if (dy <= 0 || isLoading || acabouLista) return

                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                // FIX: só carrega mais quando o ÚLTIMO item ficou visível
                // E só se já temos pelo menos LIMITE itens (senão é a primeira carga ainda)
                if (totalItems >= LIMITE && lastVisible == totalItems - 1) {
                    if (modoBusca) buscarMaisPorCidade()
                    else carregarPosts()
                }
            }
        })

        binding.edtSearchCity.setOnEditorActionListener { _, _, _ ->
            val text = binding.edtSearchCity.text.toString().trim()
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

        db.collection("usuarios").document(email).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.txtUsername.text = "@${doc.getString("username") ?: ""}"
                    binding.txtNome.text = doc.getString("nomecompleto") ?: ""

                    val foto = doc.getString("fotoPerfil")
                    if (!foto.isNullOrBlank()) {
                        try {
                            binding.imgUser.setImageBitmap(Base64Converter.stringToBitmap(foto))
                        } catch (e: Exception) {
                            binding.imgUser.setImageResource(R.drawable.empty_profile)
                        }
                    }
                }
            }
    }

    private fun carregarPosts() {
        if (isLoading || acabouLista || modoBusca) return
        isLoading = true

        android.util.Log.d("PAGINACAO", "Carregando página... posts atuais: ${posts.size}")

        var query = db.collection("posts")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(LIMITE.toLong())

        if (ultimoDocumento != null) {
            // faz a paginação no Firebase
            query = query.startAfter(ultimoDocumento!!)
        }

        query.get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    acabouLista = true
                    android.util.Log.d("PAGINACAO", "Fim da lista!")
                } else {
                    val inicio = posts.size
                    val novos = result.toObjects(Post::class.java)
                    ultimoDocumento = result.documents.last()
                    posts.addAll(novos)
                    adapter.notifyItemRangeInserted(inicio, novos.size)
                    if (novos.size < LIMITE) acabouLista = true

                    android.util.Log.d("PAGINACAO", "Recebidos: ${novos.size} posts | Total na tela: ${posts.size}")
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    private fun buscarPorCidade(cidade: String) {
        if (isLoading) return
        isLoading = true

        modoBusca = true
        cidadeBusca = cidade.lowercase()

        posts.clear()
        adapter.notifyDataSetChanged()
        ultimoDocumento = null
        acabouLista = false

        db.collection("posts")
            // filtra os posts pela cidade digitada
            .whereEqualTo("city", cidadeBusca)
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(LIMITE.toLong())
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    acabouLista = true
                } else {
                    val novos = result.toObjects(Post::class.java)
                    ultimoDocumento = result.documents.last()
                    posts.addAll(novos)
                    adapter.notifyItemRangeInserted(0, novos.size)

                    if (novos.size < LIMITE) acabouLista = true
                }
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
            .limit(LIMITE.toLong())

        if (ultimoDocumento != null) {
            query = query.startAfter(ultimoDocumento!!)
        }

        query.get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    acabouLista = true
                } else {
                    // FIX: guarda o índice de início ANTES do addAll
                    val inicio = posts.size
                    val novos = result.toObjects(Post::class.java)
                    ultimoDocumento = result.documents.last()
                    posts.addAll(novos)
                    adapter.notifyItemRangeInserted(inicio, novos.size)

                    if (novos.size < LIMITE) acabouLista = true
                }
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
        isLoading = false

        posts.clear()
        adapter.notifyDataSetChanged()

        carregarPosts()
    }
}
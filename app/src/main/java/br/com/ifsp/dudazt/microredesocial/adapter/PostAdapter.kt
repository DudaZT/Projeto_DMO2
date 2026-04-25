package br.com.ifsp.dudazt.microredesocial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.dudazt.microredesocial.R
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.databinding.PostItemBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter

// Adapter: responsável por exibir a lista de posts no RecyclerView
// pega os dados do Firebase e transforma em itens visuais no feed
class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // ViewHolder representa cada item da lista (cada post)
    class PostViewHolder(val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    // cria o layout de cada item (post_item.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    // retorna a quantidade de posts
    override fun getItemCount() = posts.size

    // faz o bind dos dados com a tela (preenche cada post)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // converte imagem Base64 para bitmap
        try {
            val bitmap = Base64Converter.stringToBitmap(post.imageString)
            holder.binding.imgPost.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // se der erro, mostra imagem padrão
            holder.binding.imgPost.setImageResource(R.drawable.empty_profile)
        }

        // preenche descrição e autor
        holder.binding.txtDescricao.text = post.descricao
        holder.binding.txtAutor.text = if (post.authorName.isNotBlank()) "@${post.authorName}" else post.authorId

        // mostra cidade
        if (post.city.isNotBlank() && post.city != "desconhecida") {
            holder.binding.txtCidade.visibility = View.VISIBLE
            holder.binding.txtCidade.text = "📍 ${post.city.replaceFirstChar { it.uppercase() }}"
        } else {
            holder.binding.txtCidade.visibility = View.GONE
        }
    }
}
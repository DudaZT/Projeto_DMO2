package br.com.ifsp.dudazt.microredesocial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.dudazt.microredesocial.R
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.databinding.PostItemBinding
import br.com.ifsp.dudazt.microredesocial.util.Base64Converter

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        try {
            val bitmap = Base64Converter.stringToBitmap(post.imageString)
            holder.binding.imgPost.setImageBitmap(bitmap)
        } catch (e: Exception) {
            holder.binding.imgPost.setImageResource(R.drawable.empty_profile)
        }

        holder.binding.txtDescricao.text = post.descricao
        holder.binding.txtCidade.text = post.city
        holder.binding.txtAutor.text = post.authorId
    }

    fun adicionarPosts(novosPosts: List<Post>) {
        val inicio = posts.size
        posts.addAll(novosPosts)
        notifyItemRangeInserted(inicio, novosPosts.size)
    }
}
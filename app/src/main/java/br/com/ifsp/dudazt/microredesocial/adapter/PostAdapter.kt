package br.com.ifsp.dudazt.microredesocial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.dudazt.microredesocial.data.model.Post
import br.com.ifsp.dudazt.microredesocial.databinding.PostItemBinding

class PostAdapter(private val posts: Array<Post>) :
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

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.binding.txtDescricao.text = post.descricao
        holder.binding.imgPost.setImageBitmap(post.imagem)
    }

    override fun getItemCount(): Int = posts.size
}
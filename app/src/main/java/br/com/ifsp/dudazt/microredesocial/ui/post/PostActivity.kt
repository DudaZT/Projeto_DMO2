package br.com.ifsp.dudazt.microredesocial.ui.post

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import br.com.ifsp.dudazt.microredesocial.databinding.ActivityPostBinding

import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val queue = Volley.newRequestQueue(this)

        // "localhost" IP
        val url = "http://localhost:8080/posts/1"

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->

                // pega descrição
                binding.txtDescricao.text = response.getString("descricao")

                // pega nome da imagem
                val nomeImagem = response.getString("foto")

                // "localhost" IP
                val urlImage = "http://localhost:8080/images/$nomeImagem"

                val imageRequest = ImageRequest(
                    urlImage,
                    { bitmap ->
                        binding.imgPost.setImageBitmap(bitmap)
                    },
                    0, 0,
                    ImageView.ScaleType.CENTER_CROP,
                    Bitmap.Config.RGB_565,
                    { error ->
                        error.printStackTrace()
                    }
                )

                queue.add(imageRequest)
            },
            { error ->
                error.printStackTrace()
            }
        )

        queue.add(jsonRequest)
    }
}
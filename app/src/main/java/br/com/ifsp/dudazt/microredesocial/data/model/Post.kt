package br.com.ifsp.dudazt.microredesocial.data.model

import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Post(
    var descricao: String = "",
    var imageString: String = "",
    var city: String = "",
    var authorId: String = "",
    var data: Timestamp? = null
)
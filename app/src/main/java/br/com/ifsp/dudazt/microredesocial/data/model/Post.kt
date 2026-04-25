package br.com.ifsp.dudazt.microredesocial.data.model

import com.google.firebase.Timestamp

// classe que representa um post no sistema (modelo de dados)
// essa classe é o formato que o Firebase usa pra mapear os dados
data class Post(
    var descricao: String = "",
    var imageString: String = "",
    var city: String = "",
    var authorId: String = "",
    var authorName: String = "",
    var data: Timestamp? = null
)
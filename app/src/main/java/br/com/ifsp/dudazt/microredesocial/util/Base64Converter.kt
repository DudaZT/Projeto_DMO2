package br.com.ifsp.dudazt.microredesocial.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

// a imagem é convertida pra Base64 pra salvar no banco
class Base64Converter {

    companion object {

        fun drawableToString(drawable: Drawable): String {
            val bitmap = (drawable as BitmapDrawable).bitmap
            return bitmapToString(bitmap)
        }

        fun bitmapToString(bitmap: Bitmap): String {
            val resized = bitmap.scale(150, 150)

            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            val bytes = outputStream.toByteArray()
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        fun stringToBitmap(imageString: String): Bitmap {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}
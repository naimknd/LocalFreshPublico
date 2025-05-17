package com.example.localfresh.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.example.localfresh.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    // Metodo para iniciar el recorte de imágenes con diseño personalizado (para Activities)
    @JvmStatic
    fun startCrop(context: Context, sourceUri: Uri, maxWidth: Int, maxHeight: Int) {
        val uniqueName = "cropped_image_" + System.currentTimeMillis() + ".jpg"
        val destinationUri = Uri.fromFile(File(context.cacheDir, uniqueName))

        val options = getUCropOptions(context)

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .start(context as AppCompatActivity)
    }

    // Metodo para obtener el intent de crop (para usar con ActivityResultLauncher en Fragments)
    @JvmStatic
    fun getCropIntent(context: Context, sourceUri: Uri, maxWidth: Int, maxHeight: Int): android.content.Intent {
        val uniqueName = "cropped_image_" + System.currentTimeMillis() + ".jpg"
        val destinationUri = Uri.fromFile(File(context.cacheDir, uniqueName))

        val options = getUCropOptions(context)

        return UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .getIntent(context)
    }

    // Opciones de uCrop solo con los parámetros solicitados
    private fun getUCropOptions(context: Context): UCrop.Options {
        val options = UCrop.Options()
        val resources = context.resources
        options.setToolbarTitle("Recorta la imagen")
        options.setToolbarColor(resources.getColor(R.color.green, null))
        options.setStatusBarColor(resources.getColor(R.color.green, null))
        options.setActiveControlsWidgetColor(resources.getColor(R.color.green, null))
        return options
    }

    // Metodo para comprimir imágenes
    @JvmStatic
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int
    ): Uri? {
        try {
            val fileName = "compressed_" + System.currentTimeMillis() + ".jpg"
            val compressedFile = File(context.cacheDir, fileName)

            var input = context.contentResolver.openInputStream(imageUri)
            var options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, options)
            input!!.close()

            var scale = 1
            while (options.outWidth / scale / 2 >= maxWidth &&
                options.outHeight / scale / 2 >= maxHeight
            ) {
                scale *= 2
            }

            options = BitmapFactory.Options()
            options.inSampleSize = scale
            input = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(input, null, options)
            input!!.close()

            val out = FileOutputStream(compressedFile)
            originalBitmap!!.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.close()

            originalBitmap.recycle()

            return Uri.fromFile(compressedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return imageUri
        }
    }
}
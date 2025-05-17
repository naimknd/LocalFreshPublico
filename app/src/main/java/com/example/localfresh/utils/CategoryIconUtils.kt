package com.example.localfresh.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.localfresh.R

object CategoryIconUtils {
    // Nombres de categorías
    val categoryNames: Array<String> = arrayOf(
        "Lácteos", "Carnes", "Huevos", "Granos y cereales",
        "Legumbres", "Panadería y repostería", "Bebidas",
        "Snacks y botanas", "Congelados", "Comida para mascotas"
    )

    // Íconos correspondientes a las categorías
    private val CATEGORY_ICONS = intArrayOf(
        R.drawable.ic_dairy, R.drawable.ic_meat, R.drawable.ic_eggs,
        R.drawable.ic_cereal, R.drawable.ic_legumes, R.drawable.ic_bakery,
        R.drawable.ic_drinks, R.drawable.ic_snacks, R.drawable.ic_frozen,
        R.drawable.ic_pets
    )

    fun getCategoryIcon(category: String): Int {
        for (i in categoryNames.indices) {
            if (categoryNames[i] == category) {
                return CATEGORY_ICONS[i]
            }
        }
        return -1 // No se encontró la categoría
    }

    fun setCategoryIcon(context: Context, textView: TextView, category: String, iconColor: Int) {
        val iconRes = getCategoryIcon(category)
        if (iconRes != -1) {
            val drawable = ContextCompat.getDrawable(context, iconRes)
            if (drawable != null) {
                // Cambiar el tamaño del ícono
                val sizeInPixels = (24 * context.resources.displayMetrics.density).toInt()
                drawable.setBounds(0, 0, sizeInPixels, sizeInPixels)

                // Cambiar el color del ícono usando DrawableCompat
                val wrappedDrawable: Drawable = DrawableCompat.wrap(drawable)
                DrawableCompat.setTint(wrappedDrawable, iconColor) // Establecer el color

                textView.setCompoundDrawables(wrappedDrawable, null, null, null)
                textView.text = category
            }
        }
    }
}

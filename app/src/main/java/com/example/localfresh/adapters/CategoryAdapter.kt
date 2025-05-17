package com.example.localfresh.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.example.localfresh.R
import com.example.localfresh.utils.CategoryIconUtils

class CategoryAdapter(context: Context, private val categories: Array<String>) :
    ArrayAdapter<String>(context, R.layout.category_item, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)

        val categoryIcon = view.findViewById<ImageView>(R.id.category_icon)
        val categoryText = view.findViewById<TextView>(R.id.category_text)

        val category = categories[position]

        categoryText.text = category
        val iconRes = CategoryIconUtils.getCategoryIcon(category)
        if (iconRes != -1) {
            categoryIcon.setImageResource(iconRes)
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = categories
                results.count = categories.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}
